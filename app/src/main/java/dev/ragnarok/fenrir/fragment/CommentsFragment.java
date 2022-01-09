package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.adapter.CommentsAdapter;
import dev.ragnarok.fenrir.adapter.OwnersListAdapter;
import dev.ragnarok.fenrir.fragment.attachments.CommentCreateFragment;
import dev.ragnarok.fenrir.fragment.attachments.CommentEditFragment;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.menu.options.CommentsOption;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommentsPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommentsView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.spots.SpotsDialog;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.MessagesReplyItemCallback;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.CommentsInputViewController;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelperComment;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;
import dev.ragnarok.fenrir.view.emoji.EmojiconsPopup;
import dev.ragnarok.fenrir.view.emoji.StickersKeyWordsAdapter;

public class CommentsFragment extends PlaceSupportMvpFragment<CommentsPresenter, ICommentsView> implements
        ICommentsView, EmojiconsPopup.OnStickerClickedListener, CommentsInputViewController.OnInputActionCallback,
        CommentsAdapter.OnCommentActionListener, EmojiconTextView.OnHashTagClickListener, BackPressCallback {

    private static final String EXTRA_AT_COMMENT_OBJECT = "at_comment_object";

    private static final String EXTRA_AT_COMMENT_THREAD = "at_comment_thread";

    private CommentsInputViewController mInputController;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ViewGroup mReplyView;
    private TextView mReplyText;
    private LoadMoreFooterHelperComment upHelper;
    private LoadMoreFooterHelperComment downhelper;
    private CommentsAdapter mAdapter;
    private ProgressBar mCenterProgressBar;
    private View mEmptyView;
    private ImageView mAuthorAvatar;
    private AlertDialog mDeepLookingProgressDialog;
    private RecyclerView stickersKeywordsView;
    private StickersKeyWordsAdapter stickersAdapter;
    private boolean mCanSendCommentAsAdmin;
    private boolean mTopicPollAvailable;
    private boolean mGotoSourceAvailable;
    @StringRes
    private Integer mGotoSourceText;

    public static CommentsFragment newInstance(@NonNull Place place) {
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(place.getArgs());
        return fragment;
    }

    public static Bundle buildArgs(int accountId, Commented commented, Integer focusToComment, Integer CommentThread) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putParcelable(Extra.COMMENTED, commented);
        if (focusToComment != null) {
            bundle.putInt(EXTRA_AT_COMMENT_OBJECT, focusToComment);
        }
        if (CommentThread != null)
            bundle.putInt(EXTRA_AT_COMMENT_THREAD, CommentThread);

        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_comments, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        stickersKeywordsView = root.findViewById(R.id.stickers);
        stickersAdapter = new StickersKeyWordsAdapter(requireActivity(), Collections.emptyList());
        stickersAdapter.setStickerClickedListener(stickerId -> {
            callPresenter(p -> p.fireStickerClick(stickerId));
            callPresenter(CommentsPresenter::resetDraftMessage);
        });
        stickersKeywordsView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        stickersKeywordsView.setAdapter(stickersAdapter);
        stickersKeywordsView.setVisibility(View.GONE);

        mAuthorAvatar = root.findViewById(R.id.author_avatar);

        mInputController = new CommentsInputViewController(requireActivity(), root, this);
        mInputController.setOnSickerClickListener(this);
        mInputController.setSendOnEnter(Settings.get().main().isSendByEnter());

        mLinearLayoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, true);

        mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mReplyView = root.findViewById(R.id.fragment_comments_reply_container);
        mReplyText = root.findViewById(R.id.fragment_comments_reply_user);

        root.findViewById(R.id.fragment_comments_delete_reply).setOnClickListener(v -> callPresenter(CommentsPresenter::fireReplyCancelClick));

        View loadUpView = inflater.inflate(R.layout.footer_load_more_comment, mRecyclerView, false);
        upHelper = LoadMoreFooterHelperComment.createFrom(loadUpView, () -> callPresenter(CommentsPresenter::fireUpLoadMoreClick));
        upHelper.setEndOfListText(" ");

        View loadDownView = inflater.inflate(R.layout.footer_load_more_comment, mRecyclerView, false);
        downhelper = LoadMoreFooterHelperComment.createFrom(loadDownView, () -> callPresenter(CommentsPresenter::fireDownLoadMoreClick));
        downhelper.setEndOfListTextRes(R.string.place_for_your_comment);

        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(CommentsPresenter::fireScrollToTop);
            }
        });

        mAdapter = new CommentsAdapter(requireActivity(), Collections.emptyList(), this);
        mAdapter.addHeader(loadDownView);
        mAdapter.addFooter(loadUpView);
        mAdapter.setListener(this);
        mAdapter.setOnHashTagClickListener(this);

        mRecyclerView.setAdapter(mAdapter);

        mCenterProgressBar = root.findViewById(R.id.progress_bar);
        mEmptyView = root.findViewById(R.id.empty_text);

        new ItemTouchHelper(new MessagesReplyItemCallback(o -> callPresenter(p -> p.fireReplyToCommentClick(mAdapter.getItemRawPosition(o))))).attachToRecyclerView(mRecyclerView);
        return root;
    }

    @Override
    public boolean onSendLongClick() {
        if (mCanSendCommentAsAdmin) {
            callPresenter(CommentsPresenter::fireSendLongClick);
            return true;
        }

        return false;
    }

    @NonNull
    @Override
    public IPresenterFactory<CommentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            Commented commented = requireArguments().getParcelable(Extra.COMMENTED);

            Integer focusTo = null;
            Integer ThreadComment = null;
            if (requireArguments().containsKey(EXTRA_AT_COMMENT_OBJECT)) {
                focusTo = requireArguments().getInt(EXTRA_AT_COMMENT_OBJECT);
                requireArguments().remove(EXTRA_AT_COMMENT_OBJECT);
            }

            if (requireArguments().containsKey(EXTRA_AT_COMMENT_THREAD)) {
                ThreadComment = requireArguments().getInt(EXTRA_AT_COMMENT_THREAD);
                requireArguments().remove(EXTRA_AT_COMMENT_THREAD);
            }

            return new CommentsPresenter(accountId, commented, focusTo, requireActivity(), ThreadComment, saveInstanceState);
        };
    }

    @Override
    public void displayData(List<Comment> data) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(data);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupLoadUpHeader(int state) {
        if (nonNull(upHelper)) {
            upHelper.switchToState(state);
        }
    }

    @Override
    public void setupLoadDownFooter(int state) {
        if (nonNull(downhelper)) {
            downhelper.switchToState(state);
        }
    }

    @Override
    public void notifyDataAddedToTop(int count) {
        if (nonNull(mAdapter)) {
            int startSize = mAdapter.getRealItemCount();
            mAdapter.notifyItemRangeInserted(startSize + mAdapter.getHeadersCount(), count);
        }
    }

    @Override
    public void notifyDataAddedToBottom(int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(0);
            mAdapter.notifyItemRangeInserted(0, count + 1);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index + mAdapter.getHeadersCount());
        }
    }

    @Override
    public void moveFocusTo(int index, boolean smooth) {
        if (isNull(mAdapter)) {
            return;
        }

        int adapterPosition = index + mAdapter.getHeadersCount();
        if (smooth) {
            if (nonNull(mRecyclerView)) {
                mRecyclerView.smoothScrollToPosition(adapterPosition);
            }
        } else {
            if (nonNull(mLinearLayoutManager)) {
                mLinearLayoutManager.scrollToPosition(adapterPosition);
            }
        }
    }

    @Override
    public void displayBody(String body) {
        if (nonNull(mInputController)) {
            mInputController.setTextQuietly(body);
            callPresenter(p -> p.fireTextEdited(body));
        }
    }

    @Override
    public void displayAttachmentsCount(int count) {
        if (nonNull(mInputController)) {
            mInputController.setAttachmentsCount(count);
        }
    }

    @Override
    public void setButtonSendAvailable(boolean available) {
        if (nonNull(mInputController)) {
            mInputController.setCanSendNormalMessage(available);
        }
    }

    @Override
    public void openAttachmentsManager(int accountId, Integer draftCommentId, int sourceOwnerId, String draftCommentBody) {
        PlaceFactory.getCommentCreatePlace(accountId, draftCommentId, sourceOwnerId, draftCommentBody)
                .setFragmentListener(CommentCreateFragment.REQUEST_CREATE_COMMENT, (requestKey, result) -> {
                    String body = result.getString(Extra.BODY);
                    postPresenterReceive(presenter -> presenter.fireEditBodyResult(body));
                })
                .tryOpenWith(requireActivity());
    }

    @Override
    public void setupReplyViews(String replyTo) {
        if (nonNull(mReplyView)) {
            mReplyView.setVisibility(nonNull(replyTo) ? View.VISIBLE : View.GONE);
        }

        if (nonNull(replyTo) && nonNull(mReplyText)) {
            mReplyText.setText(replyTo);
        }
    }

    @Override
    public void replaceBodySelectionTextTo(String replyText) {
        if (nonNull(mInputController)) {
            TextInputEditText edit = mInputController.getInputField();

            int selectionStart = edit.getSelectionStart();
            int selectionEnd = edit.getSelectionEnd();
            edit.getText().replace(selectionStart, selectionEnd, replyText);
        }
    }

    @Override
    public void goToCommentEdit(int accountId, Comment comment, Integer commemtId) {
        PlaceFactory.getEditCommentPlace(accountId, comment, commemtId)
                .setFragmentListener(CommentEditFragment.REQUEST_COMMENT_EDIT, (requestKey, result) -> {
                    Comment comment1 = result.getParcelable(Extra.COMMENT);

                    if (nonNull(comment1)) {
                        postPresenterReceive(presenter -> presenter.fireCommentEditResult(comment1));
                    }
                })
                .tryOpenWith(requireActivity());
    }

    @Override
    public void goToWallPost(int accountId, int postId, int postOwnerId) {
        PlaceFactory.getPostPreviewPlace(accountId, postId, postOwnerId).tryOpenWith(requireActivity());
    }

    @Override
    public void goToVideoPreview(int accountId, int videoId, int videoOwnerId) {
        PlaceFactory.getVideoPreviewPlace(accountId, videoOwnerId, videoId, null, null).tryOpenWith(requireActivity());
    }

    @Override
    public void banUser(int accountId, int groupId, User user) {
        PlaceFactory.getCommunityAddBanPlace(accountId, groupId, Utils.singletonArrayList(user)).tryOpenWith(requireActivity());
    }

    @Override
    public void displayAuthorAvatar(String url) {
        if (nonNull(mAuthorAvatar)) {
            if (nonEmpty(url)) {
                mAuthorAvatar.setVisibility(View.VISIBLE);

                PicassoInstance.with()
                        .load(url)
                        .transform(new RoundTransformation())
                        .into(mAuthorAvatar);
            } else {
                mAuthorAvatar.setVisibility(View.GONE);
                PicassoInstance.with()
                        .cancelRequest(mAuthorAvatar);
            }
        }
    }

    @Override
    public void scrollToPosition(int position) {
        if (nonNull(mLinearLayoutManager) && nonNull(mAdapter)) {
            mLinearLayoutManager.scrollToPosition(position + mAdapter.getHeadersCount());
        }
    }

    @Override
    public void showCommentSentToast() {
        showToast(R.string.toast_comment_sent, true);
    }

    @Override
    public void showAuthorSelectDialog(List<Owner> owners) {
        ArrayList<Owner> data = new ArrayList<>(owners);
        OwnersListAdapter adapter = new OwnersListAdapter(requireActivity(), data);
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_comment_author)
                .setAdapter(adapter, (dialog, which) -> callPresenter(p -> p.fireAuthorSelected(data.get(which))))
                .setNegativeButton(R.string.button_cancel, null)
                .show();

    }

    @Override
    public void setupOptionMenu(boolean topicPollAvailable, boolean gotoSourceAvailable, Integer gotoSourceText) {
        mTopicPollAvailable = topicPollAvailable;
        mGotoSourceAvailable = gotoSourceAvailable;
        mGotoSourceText = gotoSourceText;

        try {
            requireActivity().invalidateOptionsMenu();
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setEpmtyTextVisible(boolean visible) {
        if (nonNull(mEmptyView)) {
            mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setCenterProgressVisible(boolean visible) {
        if (nonNull(mCenterProgressBar)) {
            mCenterProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayDeepLookingCommentProgress() {
        mDeepLookingProgressDialog = new SpotsDialog.Builder().setContext(requireActivity()).setCancelable(true).setCancelListener(dialog -> callPresenter(CommentsPresenter::fireDeepLookingCancelledByUser)).build();
        mDeepLookingProgressDialog.show();
    }

    @Override
    public void dismissDeepLookingCommentProgress() {
        if (nonNull(mDeepLookingProgressDialog)) {
            mDeepLookingProgressDialog.dismiss();
        }
    }

    @Override
    public void setCanSendSelectAuthor(boolean can) {
        mCanSendCommentAsAdmin = can;
    }

    @Override
    public void updateStickers(List<Sticker> items) {
        if (Utils.isEmpty(items)) {
            stickersKeywordsView.setVisibility(View.GONE);
        } else {
            stickersKeywordsView.setVisibility(View.VISIBLE);
        }
        stickersAdapter.setData(items);
    }

    @Override
    public void onStickerClick(Sticker sticker) {
        callPresenter(p -> p.fireStickerClick(sticker));
    }

    @Override
    public void onInputTextChanged(String s) {
        callPresenter(p -> p.fireInputTextChanged(s));
        callPresenter(p -> p.fireTextEdited(s));
    }

    @Override
    public void onSendClicked() {
        callPresenter(CommentsPresenter::fireSendClick);
    }

    @Override
    public void onAttachClick() {
        callPresenter(CommentsPresenter::fireAttachClick);
    }

    @Override
    public void onReplyToOwnerClick(int ownerId, int commentId) {
        callPresenter(p -> p.fireReplyToOwnerClick(commentId));
    }

    @Override
    public void onRestoreComment(int commentId) {
        callPresenter(p -> p.fireCommentRestoreClick(commentId));
    }

    @Override
    public void onAvatarClick(int ownerId) {
        onOpenOwner(ownerId);
    }

    @Override
    public void onCommentLikeClick(Comment comment, boolean add) {
        callPresenter(p -> p.fireCommentLikeClick(comment, add));
    }

    @Override
    public void populateCommentContextMenu(Comment comment) {
        if (comment.getFromId() == 0) {
            return;
        }
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        menus.header(comment.getFullAuthorName(), R.drawable.comment, comment.getMaxAuthorAvaUrl());
        menus.columns(2);

        ContextView contextView = new ContextView();
        callPresenter(p -> p.fireCommentContextViewCreated(contextView, comment));

        if (!Utils.isEmpty(comment.getText())) {
            menus.add(new OptionRequest(CommentsOption.copy_item_comment, getString(R.string.copy), R.drawable.content_copy, true));
        }
        menus.add(new OptionRequest(CommentsOption.reply_item_comment, getString(R.string.reply), R.drawable.reply, true));
        menus.add(new OptionRequest(CommentsOption.report_item_comment, getString(R.string.report), R.drawable.report, true));

        if (contextView.canDelete) {
            menus.add(new OptionRequest(CommentsOption.delete_item_comment, getString(R.string.delete), R.drawable.ic_outline_delete, true));
        }

        if (contextView.canEdit) {
            menus.add(new OptionRequest(CommentsOption.edit_item_comment, getString(R.string.edit), R.drawable.pencil, true));
        }

        if (contextView.canBan) {
            menus.add(new OptionRequest(CommentsOption.block_author_item_comment, getString(R.string.ban_author), R.drawable.block_outline, false));
        }

        if (!comment.isUserLikes()) {
            menus.add(new OptionRequest(CommentsOption.like_item_comment, getString(R.string.like), R.drawable.heart, false));
        } else {
            menus.add(new OptionRequest(CommentsOption.dislike_item_comment, getString(R.string.dislike), R.drawable.ic_no_heart, false));
        }
        menus.add(new OptionRequest(CommentsOption.who_like_item_comment, getString(R.string.who_likes), R.drawable.heart_filled, false));
        menus.add(new OptionRequest(CommentsOption.send_to_friend_item_comment, getString(R.string.send_to_friend), R.drawable.friends, false));
        menus.show(requireActivity().getSupportFragmentManager(), "comments_options", option -> {
            switch (option.getId()) {
                case CommentsOption.copy_item_comment:
                    ClipboardManager clipboard = (ClipboardManager) requireActivity()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("comment", comment.getText());
                    clipboard.setPrimaryClip(clip);
                    CustomToast.CreateCustomToast(requireActivity()).setDuration(Toast.LENGTH_LONG).showToast(R.string.copied_to_clipboard);
                    break;
                case CommentsOption.reply_item_comment:
                    callPresenter(p -> p.fireReplyToCommentClick(comment));
                    break;
                case CommentsOption.report_item_comment:
                    callPresenter(p -> p.fireReport(comment));
                    break;
                case CommentsOption.delete_item_comment:
                    callPresenter(p -> p.fireCommentDeleteClick(comment));
                    break;
                case CommentsOption.edit_item_comment:
                    callPresenter(p -> p.fireCommentEditClick(comment));
                    break;
                case CommentsOption.block_author_item_comment:
                    callPresenter(p -> p.fireBanClick(comment));
                    break;
                case CommentsOption.like_item_comment:
                    callPresenter(p -> p.fireCommentLikeClick(comment, true));
                    break;
                case CommentsOption.dislike_item_comment:
                    callPresenter(p -> p.fireCommentLikeClick(comment, false));
                    break;
                case CommentsOption.who_like_item_comment:
                    callPresenter(p -> p.fireWhoLikesClick(comment));
                    break;
                case CommentsOption.send_to_friend_item_comment:
                    callPresenter(p -> p.fireReplyToChat(comment));
                    break;
            }
        });
    }

    @Override
    public void onHashTagClicked(String hashTag) {
        callPresenter(p -> p.fireHashtagClick(hashTag));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.comments_list_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.open_poll).setVisible(mTopicPollAvailable);

        MenuItem gotoSource = menu.findItem(R.id.to_commented);
        gotoSource.setVisible(mGotoSourceAvailable);

        if (mGotoSourceAvailable) {
            gotoSource.setTitle(mGotoSourceText);
        }

        boolean desc = Settings.get().other().isCommentsDesc();
        menu.findItem(R.id.direction).setIcon(getDirectionIcon(desc));
    }

    @DrawableRes
    private int getDirectionIcon(boolean desc) {
        return desc ? R.drawable.double_up : R.drawable.double_down;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.refresh) {
            callPresenter(CommentsPresenter::fireRefreshClick);
            return true;
        } else if (item.getItemId() == R.id.open_poll) {
            callPresenter(CommentsPresenter::fireTopicPollClick);
            return true;
        } else if (item.getItemId() == R.id.to_commented) {
            callPresenter(CommentsPresenter::fireGotoSourceClick);
            return true;
        } else if (item.getItemId() == R.id.direction) {
            boolean decs = Settings.get().other().toggleCommentsDirection();
            item.setIcon(getDirectionIcon(decs));
            callPresenter(CommentsPresenter::fireDirectionChanged);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onBackPressed() {
        return mInputController.onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mInputController.destroyView();
        mInputController = null;
    }

    private static final class ContextView implements ICommentContextView {

        boolean canEdit;

        boolean canDelete;

        boolean canBan;

        @Override
        public void setCanEdit(boolean can) {
            canEdit = can;
        }

        @Override
        public void setCanDelete(boolean can) {
            canDelete = can;
        }

        @Override
        public void setCanBan(boolean can) {
            canBan = can;
        }
    }
}
