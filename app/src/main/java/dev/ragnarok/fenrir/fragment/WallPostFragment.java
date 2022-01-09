package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso3.Transformation;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.adapter.AttachmentsHolder;
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.NewsFeedCriteria;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.WallPostPresenter;
import dev.ragnarok.fenrir.mvp.view.IWallPostView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceUtil;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.CircleCounterButton;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;

public class WallPostFragment extends PlaceSupportMvpFragment<WallPostPresenter, IWallPostView>
        implements EmojiconTextView.OnHashTagClickListener, IWallPostView {

    private TextView mSignerNameText;
    private View mSignerRootView;
    private ImageView mSignerAvatar;

    private CircleCounterButton mShareButton;
    private CircleCounterButton mCommentsButton;
    private CircleCounterButton mLikeButton;

    private EmojiconTextView mText;
    private AttachmentsViewBinder attachmentsViewBinder;
    private Transformation transformation;
    private ViewGroup root;
    private AttachmentsHolder mAttachmentsViews;
    private boolean mTextSelectionAllowed;

    public static WallPostFragment newInstance(Bundle args) {
        WallPostFragment fragment = new WallPostFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle buildArgs(int accountId, int postId, int ownerId, Post post) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putInt(Extra.POST_ID, postId);
        bundle.putInt(Extra.OWNER_ID, ownerId);
        bundle.putParcelable(Extra.POST, post);
        return bundle;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        attachmentsViewBinder = new AttachmentsViewBinder(requireActivity(), this);
        attachmentsViewBinder.setOnHashTagClickListener(this);
        transformation = CurrentTheme.createTransformationForAvatar();
    }

    @Override
    public void displayPinComplete(boolean pinned) {
        Toast.makeText(requireActivity(), pinned ? R.string.pin_result : R.string.unpin_result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void displayDeleteOrRestoreComplete(boolean deleted) {
        Toast.makeText(requireActivity(), deleted ? R.string.delete_result : R.string.restore_result, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.fragment_post, container, false);
        mAttachmentsViews = AttachmentsHolder.forPost(root);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mShareButton = root.findViewById(R.id.share_button);
        mCommentsButton = root.findViewById(R.id.comments_button);
        mLikeButton = root.findViewById(R.id.like_button);

        mText = root.findViewById(R.id.fragment_post_text);
        mText.setMovementMethod(LinkMovementMethod.getInstance());
        mText.setOnHashTagClickListener(this);

        mSignerRootView = root.findViewById(R.id.item_post_signer_root);
        mSignerAvatar = root.findViewById(R.id.item_post_signer_icon);
        mSignerNameText = root.findViewById(R.id.item_post_signer_name);

        mLikeButton.setOnClickListener(view -> callPresenter(WallPostPresenter::fireLikeClick));

        mLikeButton.setOnLongClickListener(view -> {
            callPresenter(WallPostPresenter::fireLikeLongClick);
            return true;
        });

        mShareButton.setOnClickListener(view -> callPresenter(WallPostPresenter::fireShareClick));
        mShareButton.setOnLongClickListener(view -> {
            callPresenter(WallPostPresenter::fireRepostLongClick);
            return true;
        });

        root.findViewById(R.id.try_again_button).setOnClickListener(v -> callPresenter(WallPostPresenter::fireTryLoadAgainClick));

        mCommentsButton.setOnClickListener(view -> callPresenter(WallPostPresenter::fireCommentClick));
        resolveTextSelection();
        return root;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_post) {
            callPresenter(WallPostPresenter::fireDeleteClick);
            return true;
        } else if (item.getItemId() == R.id.restore_post) {
            callPresenter(WallPostPresenter::fireRestoreClick);
            return true;
        } else if (item.getItemId() == R.id.pin_post) {
            callPresenter(WallPostPresenter::firePinClick);
            return true;
        } else if (item.getItemId() == R.id.unpin_post) {
            callPresenter(WallPostPresenter::fireUnpinClick);
            return true;
        } else if (item.getItemId() == R.id.goto_user_post) {
            callPresenter(WallPostPresenter::fireGoToOwnerClick);
            return true;
        } else if (item.getItemId() == R.id.copy_url_post) {
            callPresenter(WallPostPresenter::fireCopyLinkClink);
            return true;
        } else if (item.getItemId() == R.id.report) {
            callPresenter(WallPostPresenter::fireReport);
            return true;
        } else if (item.getItemId() == R.id.copy_text) {
            callPresenter(WallPostPresenter::fireCopyTextClick);
            return true;
        } else if (item.getItemId() == R.id.action_allow_text_selection) {
            mTextSelectionAllowed = true;
            resolveTextSelection();
            return true;
        } else if (item.getItemId() == R.id.add_to_bookmarks) {
            callPresenter(WallPostPresenter::fireAddBookmark);
            return true;
        } else if (item.getItemId() == R.id.edit_post) {
            callPresenter(WallPostPresenter::firePostEditClick);
            return true;
        } else if (item.getItemId() == R.id.refresh) {
            callPresenter(WallPostPresenter::fireRefresh);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showSuccessToast() {
        Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void copyLinkToClipboard(String link) {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.link), link);
        clipboard.setPrimaryClip(clip);

        getCustomToast().showToast(R.string.copied_url);
    }

    @Override
    public void copyTextToClipboard(String text) {
        ClipboardManager manager = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText(getString(R.string.post_text), text);
        manager.setPrimaryClip(clipData);

        getCustomToast().showToast(R.string.copied_text);
    }

    private void resolveTextSelection() {
        if (nonNull(mText)) {
            mText.setTextIsSelectable(mTextSelectionAllowed);
        }

        ViewGroup copiesRoot = mAttachmentsViews.getVgPosts();

        for (int i = 0; i < copiesRoot.getChildCount(); i++) {
            ViewGroup copyRoot = (ViewGroup) copiesRoot.getChildAt(i);
            TextView textView = copyRoot.findViewById(R.id.item_post_copy_text);
            if (nonNull(textView)) {
                textView.setTextIsSelectable(mTextSelectionAllowed);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionView optionView = new OptionView();
        callPresenter(p -> p.fireOptionViewCreated(optionView));

        menu.findItem(R.id.edit_post).setVisible(optionView.canEdit);
        menu.findItem(R.id.unpin_post).setVisible(optionView.canUnpin);
        menu.findItem(R.id.pin_post).setVisible(optionView.canPin);
        menu.findItem(R.id.delete_post).setVisible(optionView.canDelete);
        menu.findItem(R.id.restore_post).setVisible(optionView.canRestore);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.single_post_menu, menu);
    }

    /*private boolean canEdit() {
        return post.isCanEdit();

        boolean canEditAsAdmin = false;

        if(nonNull(owner) && owner.admin_level >= VKApiCommunity.AdminLevel.EDITOR){
            if(owner.type == VKApiCommunity.Type.GROUP){
                // нельзя редактировать чужие посты в GROUP
                canEditAsAdmin = post.getCreatorId() == getAccountId() && post.getSignerId() == getAccountId();
            }

            if(owner.type == VKApiCommunity.Type.PAGE){
                canEditAsAdmin = true;
            }
        }

        boolean canEdit = post.getAuthorId() == getAccountId() || canEditAsAdmin;

        if (!canEdit) {
            return false;
        }

        long currentUnixtime = System.currentTimeMillis() / 1000;
        return (currentUnixtime - post.getDate()) < Constants.HOURS_24_IN_SECONDS;
    }*/

    @Override
    public void displayDefaultToolbaTitle() {
        setToolbarTitle(getString(R.string.wall_post));
    }

    @Override
    public void displayToolbarTitle(String title) {
        setToolbarTitle(title);
    }

    @Override
    public void displayToolbatSubtitle(int subtitleType, long datetime) {
        String formattedDate = AppTextUtils.getDateFromUnixTime(requireActivity(), datetime);

        switch (subtitleType) {
            case SUBTITLE_NORMAL:
                setToolbarSubtitle(formattedDate);
                break;

            case SUBTITLE_STATUS_UPDATE:
                setToolbarSubtitle(getString(R.string.updated_status_at, formattedDate));
                break;

            case SUBTITLE_PHOTO_UPDATE:
                setToolbarSubtitle(getString(R.string.updated_profile_photo_at, formattedDate));
                break;
        }
    }

    @Override
    public void displayDefaultToolbaSubitle() {
        setToolbarSubtitle(null);
    }

    @Override
    public void displayPostInfo(Post post) {
        if (isNull(root)) {
            return;
        }

        if (post.isDeleted()) {
            root.findViewById(R.id.fragment_post_deleted).setVisibility(View.VISIBLE);
            root.findViewById(R.id.post_content).setVisibility(View.GONE);
            root.findViewById(R.id.post_loading_root).setVisibility(View.GONE);
            return;
        }

        root.findViewById(R.id.fragment_post_deleted).setVisibility(View.GONE);
        root.findViewById(R.id.post_content).setVisibility(View.VISIBLE);
        root.findViewById(R.id.post_loading_root).setVisibility(View.GONE);

        mText.setVisibility(post.hasText() ? View.VISIBLE : View.GONE);

        Spannable spannableText = OwnerLinkSpanFactory.withSpans(post.getText(), true, false, new LinkActionAdapter() {
            @Override
            public void onOwnerClick(int ownerId) {
                onOpenOwner(ownerId);
            }

            @Override
            public void onOtherClick(String URL) {
                LinkHelper.openUrl(requireActivity(), Settings.get().accounts().getCurrent(), URL);
            }
        });

        mText.setText(spannableText, TextView.BufferType.SPANNABLE);

        boolean displaySigner = post.getSignerId() > 0 && nonNull(post.getCreator());
        mSignerRootView.setVisibility(displaySigner ? View.VISIBLE : View.GONE);

        if (displaySigner) {
            User creator = post.getCreator();
            mSignerNameText.setText(creator.getFullName());

            ViewUtils.displayAvatar(mSignerAvatar, transformation, creator.get100photoOrSmaller(), Constants.PICASSO_TAG);
            mSignerRootView.setOnClickListener(v -> onOpenOwner(post.getSignerId()));
        }

        attachmentsViewBinder.displayAttachments(post.getAttachments(), mAttachmentsViews, false, null);
        attachmentsViewBinder.displayCopyHistory(post.getCopyHierarchy(), mAttachmentsViews.getVgPosts(),
                false, R.layout.item_copy_history_post);
    }

    @Override
    public void displayLoading() {
        if (nonNull(root)) {
            root.findViewById(R.id.fragment_post_deleted).setVisibility(View.GONE);
            root.findViewById(R.id.post_content).setVisibility(View.GONE);
            root.findViewById(R.id.post_loading_root).setVisibility(View.VISIBLE);

            root.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            root.findViewById(R.id.post_loading_text).setVisibility(View.VISIBLE);
            root.findViewById(R.id.try_again_button).setVisibility(View.GONE);
        }
    }

    @Override
    public void displayLoadingFail() {
        if (nonNull(root)) {
            root.findViewById(R.id.fragment_post_deleted).setVisibility(View.GONE);
            root.findViewById(R.id.post_content).setVisibility(View.GONE);
            root.findViewById(R.id.post_loading_root).setVisibility(View.VISIBLE);

            root.findViewById(R.id.progressBar).setVisibility(View.GONE);
            root.findViewById(R.id.post_loading_text).setVisibility(View.GONE);
            root.findViewById(R.id.try_again_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void displayLikes(int count, boolean userLikes) {
        if (nonNull(mLikeButton)) {
            mLikeButton.setActive(userLikes);
            mLikeButton.setCount(count);
            mLikeButton.setIcon(userLikes ? R.drawable.heart_filled : R.drawable.heart);
        }
    }

    @Override
    public void setCommentButtonVisible(boolean visible) {
        if (nonNull(mCommentsButton)) {
            mCommentsButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void displayCommentCount(int count) {
        if (nonNull(mCommentsButton)) {
            mCommentsButton.setCount(count);
        }
    }

    @Override
    public void displayReposts(int count, boolean userReposted) {
        if (nonNull(mShareButton)) {
            mShareButton.setCount(count);
            mShareButton.setActive(userReposted);
        }
    }

    @Override
    public void goToPostEditing(int accountId, @NonNull Post post) {
        PlaceUtil.goToPostEditor(requireActivity(), accountId, post);
    }

    @Override
    public void showPostNotReadyToast() {
        Toast.makeText(requireActivity(), R.string.wall_post_is_not_yet_initialized, Toast.LENGTH_LONG).show();
    }

    @NonNull
    @Override
    public IPresenterFactory<WallPostPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            ParcelableOwnerWrapper wrapper = requireArguments().getParcelable(Extra.OWNER);
            return new WallPostPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.POST_ID),
                    requireArguments().getInt(Extra.OWNER_ID),
                    requireArguments().getParcelable(Extra.POST),
                    nonNull(wrapper) ? wrapper.get() : null,
                    requireActivity(),
                    saveInstanceState
            );
        };
    }

    @Override
    public void goToNewsSearch(int accountId, String hashTag) {
        NewsFeedCriteria criteria = new NewsFeedCriteria(hashTag);
        PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.NEWS, criteria).tryOpenWith(requireActivity());
    }

    @Override
    public void onHashTagClicked(String hashTag) {
        callPresenter(p -> p.fireHasgTagClick(hashTag));
    }

    private static final class OptionView implements IWallPostView.IOptionView {

        boolean canDelete;
        boolean canRestore;
        boolean canPin;
        boolean canUnpin;
        boolean canEdit;

        @Override
        public void setCanDelete(boolean can) {
            canDelete = can;
        }

        @Override
        public void setCanRestore(boolean can) {
            canRestore = can;
        }

        @Override
        public void setCanPin(boolean can) {
            canPin = can;
        }

        @Override
        public void setCanUnpin(boolean can) {
            canUnpin = can;
        }

        @Override
        public void setCanEdit(boolean can) {
            canEdit = can;
        }
    }
}
