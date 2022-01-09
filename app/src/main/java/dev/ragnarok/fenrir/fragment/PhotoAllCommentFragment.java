package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.CommentsAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.menu.options.CommentsPhotoOption;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.PhotoAllCommentPresenter;
import dev.ragnarok.fenrir.mvp.view.IPhotoAllCommentView;
import dev.ragnarok.fenrir.spots.SpotsDialog;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;

public class PhotoAllCommentFragment extends PlaceSupportMvpFragment<PhotoAllCommentPresenter, IPhotoAllCommentView>
        implements IPhotoAllCommentView, SwipeRefreshLayout.OnRefreshListener, CommentsAdapter.OnCommentActionListener, EmojiconTextView.OnHashTagClickListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CommentsAdapter mAdapter;
    private TextView mEmpty;
    private AlertDialog mDeepLookingProgressDialog;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    public static PhotoAllCommentFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        PhotoAllCommentFragment fragment = new PhotoAllCommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_all_comment, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        recyclerView = root.findViewById(android.R.id.list);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mEmpty = root.findViewById(R.id.empty);

        linearLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(PhotoAllCommentPresenter::fireScrollToEnd);
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new CommentsAdapter(requireActivity(), Collections.emptyList(), this);
        mAdapter.setListener(this);
        mAdapter.setOnHashTagClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onRefresh() {
        callPresenter(PhotoAllCommentPresenter::fireRefresh);
    }

    @Override
    public void displayData(List<Comment> PhotoAllComment) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(PhotoAllComment);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void dismissDeepLookingCommentProgress() {
        if (nonNull(mDeepLookingProgressDialog)) {
            mDeepLookingProgressDialog.dismiss();
        }
    }

    @Override
    public void displayDeepLookingCommentProgress() {
        mDeepLookingProgressDialog = new SpotsDialog.Builder().setContext(requireActivity()).setCancelable(true).setCancelListener(dialog -> callPresenter(PhotoAllCommentPresenter::fireDeepLookingCancelledByUser)).build();
        mDeepLookingProgressDialog.show();
    }

    @Override
    public void moveFocusTo(int index, boolean smooth) {
        if (isNull(mAdapter)) {
            return;
        }

        int adapterPosition = index + mAdapter.getHeadersCount();
        if (smooth) {
            if (nonNull(recyclerView)) {
                recyclerView.smoothScrollToPosition(adapterPosition);
            }
        } else {
            if (nonNull(linearLayoutManager)) {
                linearLayoutManager.scrollToPosition(adapterPosition);
            }
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
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index + mAdapter.getHeadersCount());
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<PhotoAllCommentPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new PhotoAllCommentPresenter(requireArguments().getInt(Extra.ACCOUNT_ID), requireArguments().getInt(Extra.OWNER_ID), saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.comments);
            actionBar.setSubtitle(null);
        }
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onReplyToOwnerClick(int ownerId, int commentId) {
        callPresenter(p -> p.fireReplyToOwnerClick(commentId));
    }

    @Override
    public void onRestoreComment(int commentId) {

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
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        menus.header(comment.getFullAuthorName(), R.drawable.comment, comment.getMaxAuthorAvaUrl());
        menus.columns(2);

        menus.add(new OptionRequest(CommentsPhotoOption.go_to_photo_item_comment, getString(R.string.photo), R.drawable.dir_photo, true));

        if (!Utils.isEmpty(comment.getText())) {
            menus.add(new OptionRequest(CommentsPhotoOption.copy_item_comment, getString(R.string.copy), R.drawable.content_copy, true));
        }

        menus.add(new OptionRequest(CommentsPhotoOption.report_item_comment, getString(R.string.report), R.drawable.report, true));

        if (!comment.isUserLikes()) {
            menus.add(new OptionRequest(CommentsPhotoOption.like_item_comment, getString(R.string.like), R.drawable.heart, false));
        } else {
            menus.add(new OptionRequest(CommentsPhotoOption.dislike_item_comment, getString(R.string.dislike), R.drawable.ic_no_heart, false));
        }

        menus.add(new OptionRequest(CommentsPhotoOption.who_like_item_comment, getString(R.string.who_likes), R.drawable.heart_filled, false));

        menus.add(new OptionRequest(CommentsPhotoOption.send_to_friend_item_comment, getString(R.string.send_to_friend), R.drawable.friends, false));
        menus.show(requireActivity().getSupportFragmentManager(), "comments_photo_options", option -> {
            switch (option.getId()) {
                case CommentsPhotoOption.go_to_photo_item_comment:
                    callPresenter(p -> p.fireGoPhotoClick(comment));
                    break;
                case CommentsPhotoOption.copy_item_comment:
                    ClipboardManager clipboard = (ClipboardManager) requireActivity()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("comment", comment.getText());
                    clipboard.setPrimaryClip(clip);
                    CustomToast.CreateCustomToast(requireActivity()).setDuration(Toast.LENGTH_LONG).showToast(R.string.copied_to_clipboard);
                    break;
                case CommentsPhotoOption.report_item_comment:
                    callPresenter(p -> p.fireReport(comment, requireActivity()));
                    break;
                case CommentsPhotoOption.like_item_comment:
                    callPresenter(p -> p.fireCommentLikeClick(comment, true));
                    break;
                case CommentsPhotoOption.dislike_item_comment:
                    callPresenter(p -> p.fireCommentLikeClick(comment, false));
                    break;
                case CommentsPhotoOption.who_like_item_comment:
                    callPresenter(p -> p.fireWhoLikesClick(comment));
                    break;
                case CommentsPhotoOption.send_to_friend_item_comment:
                    callPresenter(p -> p.fireReplyToChat(comment, requireActivity()));
                    break;
            }
        });
    }

    @Override
    public void onHashTagClicked(String hashTag) {
        callPresenter(p -> p.fireHashtagClick(hashTag));
    }
}
