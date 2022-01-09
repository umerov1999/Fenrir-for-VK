package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.isLandscape;

import android.annotation.SuppressLint;
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
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.adapter.WallAdapter;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalStoryAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.WallSearchCriteria;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.mvp.presenter.AbsWallPresenter;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideosListView;
import dev.ragnarok.fenrir.mvp.view.IVkPhotosView;
import dev.ragnarok.fenrir.mvp.view.IWallView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceUtil;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.FindAttachmentType;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public abstract class AbsWallFragment<V extends IWallView, P extends AbsWallPresenter<V>>
        extends PlaceSupportMvpFragment<P, V> implements IWallView, WallAdapter.ClickListener, WallAdapter.NonPublishedPostActionListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private WallAdapter mWallAdapter;
    private LoadMoreFooterHelper mLoadMoreFooterHelper;
    private HorizontalStoryAdapter mStoryAdapter;
    private FloatingActionButton fabCreate;
    private boolean isCreatePost = true;
    private final RecyclerView.OnScrollListener mFabScrollListener = new RecyclerView.OnScrollListener() {
        int scrollMinOffset;

        @Override
        public void onScrolled(@NonNull RecyclerView view, int dx, int dy) {
            if (scrollMinOffset == 0) {
                // one-time-init
                scrollMinOffset = (int) Utils.dpToPx(2, view.getContext());
            }

            if (dy > scrollMinOffset && fabCreate.isShown()) {
                fabCreate.hide();
            }

            if (dy < -scrollMinOffset && !fabCreate.isShown()) {
                fabCreate.show();
                if (view.getLayoutManager() instanceof LinearLayoutManager) {
                    LinearLayoutManager myLayoutManager = (LinearLayoutManager) view.getLayoutManager();
                    ToggleFab(myLayoutManager.findFirstVisibleItemPosition() > 7);
                }
            }
        }
    };

    public static Bundle buildArgs(int accountId, int ownerId, @Nullable Owner owner) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putParcelable(Extra.OWNER, new ParcelableOwnerWrapper(owner));
        return args;
    }

    public static Fragment newInstance(Bundle args) {
        Fragment fragment;
        if (args.getInt(Extra.OWNER_ID) > 0) {
            fragment = new UserWallFragment();
        } else {
            fragment = new GroupWallFragment();
        }

        fragment.setArguments(args);
        return fragment;
    }

    protected static void setupCounter(TextView view, int count) {
        view.setText((count > 0 ? (AppTextUtils.getCounterWithK(count)) : "-"));
        view.setEnabled(count > 0);
    }

    protected static void setupCounterFlow(TextView view, ViewGroup container, int count) {
        view.setText((count > 0 ? (AppTextUtils.getCounterWithK(count)) : "-"));
        view.setEnabled(count > 0);
        container.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
    }

    @SuppressLint("SetTextI18n")
    protected static void setupCounterWith(TextView view, int count, int with) {
        view.setText((count > 0 ? (AppTextUtils.getCounterWithK(count)) : "-") + (with > 0 ? "/" + (AppTextUtils.getCounterWithK(with)) : ""));
        view.setEnabled(count > 0);
    }

    protected void setupPaganContent(@NonNull View Runes, @NonNull RLottieImageView paganSymbol) {
        Runes.setVisibility(Settings.get().other().isRunes_show() ? View.VISIBLE : View.GONE);
        int symbol = ThemesController.INSTANCE.paganSymbol();
        paganSymbol.setVisibility(symbol != 0 ? View.VISIBLE : View.GONE);
        if (symbol == 0) {
            return;
        }
        switch (symbol) {
            case 2:
                paganSymbol.setImageResource(R.drawable.ic_igdr);
                break;
            case 3:
                paganSymbol.setImageResource(R.drawable.valknut);
                break;
            case 4:
                paganSymbol.setImageResource(R.drawable.ic_mjolnir);
                break;
            case 5:
                paganSymbol.setImageResource(R.drawable.ic_vegvisir);
                break;
            case 6:
                paganSymbol.setImageResource(R.drawable.ic_vegvisir2);
                break;
            case 7:
                paganSymbol.setImageResource(R.drawable.ic_celtic_knot);
                break;
            case 8:
                paganSymbol.setImageResource(R.drawable.ic_celtic_flower);
                break;
            case 9:
                paganSymbol.setImageResource(R.drawable.ic_slepnir);
                break;
            case 10:
                if (FenrirNative.isNativeLoaded()) {
                    paganSymbol.fromRes(R.raw.fenrir, Utils.dp(140), Utils.dp(140), new int[]{
                            0x333333,
                            CurrentTheme.getColorPrimary(requireActivity()),
                            0x777777,
                            CurrentTheme.getColorSecondary(requireActivity())
                    });
                    paganSymbol.playAnimation();
                } else {
                    paganSymbol.setImageResource(R.drawable.ic_igdr);
                }
                break;
            case 11:
                paganSymbol.setImageResource(R.drawable.ic_triskel);
                break;
            case 12:
                paganSymbol.setImageResource(R.drawable.ic_hell);
                break;
            case 13:
                paganSymbol.setImageResource(R.drawable.ic_odin);
                break;
            case 14:
                paganSymbol.setImageResource(R.drawable.ic_viking);
                break;
            case 15:
                paganSymbol.setImageResource(R.drawable.ic_raven);
                break;
            case 16:
                paganSymbol.setImageResource(R.drawable.ic_fire);
                break;
            default:
                paganSymbol.setImageResource(R.drawable.ic_cat);
                break;
        }
    }

    private void ToggleFab(boolean isUp) {
        if (isCreatePost == isUp) {
            isCreatePost = !isUp;
            fabCreate.setImageResource(isCreatePost ? R.drawable.pencil : R.drawable.ic_outline_keyboard_arrow_up);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wall, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(AbsWallPresenter::fireRefresh));

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView.LayoutManager manager;
        if (Utils.is600dp(requireActivity())) {
            boolean land = isLandscape(requireActivity());
            manager = new StaggeredGridLayoutManager(land ? 2 : 1, StaggeredGridLayoutManager.VERTICAL);
        } else {
            manager = new LinearLayoutManager(requireActivity());
        }

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(mFabScrollListener);
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(AbsWallPresenter::fireScrollToEnd);
            }
        });

        View headerView = inflater.inflate(headerLayout(), recyclerView, false);
        onHeaderInflated(headerView);

        View footerView = inflater.inflate(R.layout.footer_load_more, recyclerView, false);
        mLoadMoreFooterHelper = LoadMoreFooterHelper.createFrom(footerView, () -> callPresenter(AbsWallPresenter::fireLoadMoreClick));

        fabCreate = root.findViewById(R.id.fragment_user_profile_fab);
        fabCreate.setOnClickListener(v -> {
            if (isCreatePost) {
                callPresenter(AbsWallPresenter::fireCreateClick);
            } else {
                recyclerView.scrollToPosition(0);
                ToggleFab(false);
            }
        });


        View headerStory = inflater.inflate(R.layout.header_story, recyclerView, false);
        RecyclerView headerStoryRecyclerView = headerStory.findViewById(R.id.header_story);
        headerStoryRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        mStoryAdapter = new HorizontalStoryAdapter(Collections.emptyList());
        mStoryAdapter.setListener((item, pos) -> openHistoryVideo(Settings.get().accounts().getCurrent(), new ArrayList<>(callPresenter(AbsWallPresenter::getStories, Collections.emptyList())), pos));
        headerStoryRecyclerView.setAdapter(mStoryAdapter);

        mWallAdapter = new WallAdapter(requireActivity(), Collections.emptyList(), this, this);
        mWallAdapter.addHeader(headerView);
        mWallAdapter.addHeader(headerStoryRecyclerView);
        mWallAdapter.addFooter(footerView);
        mWallAdapter.setNonPublishedPostActionListener(this);

        recyclerView.setAdapter(mWallAdapter);
        return root;
    }

    @Override
    public void onAvatarClick(int ownerId) {
        super.onOwnerClick(ownerId);
    }

    @Override
    public void showSnackbar(int res, boolean isLong) {
        if (nonNull(getView())) {
            Snackbar.make(getView(), res, isLong ? BaseTransientBottomBar.LENGTH_LONG : BaseTransientBottomBar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void openPhotoAlbum(int accountId, int ownerId, int albumId, ArrayList<Photo> photos, int position) {
        PlaceFactory.getPhotoAlbumGalleryPlace(accountId, albumId, ownerId, photos, position, false, Settings.get().other().isInvertPhotoRev())
                .tryOpenWith(requireActivity());
    }

    @Override
    public void goToWallSearch(int accountId, int ownerId) {
        WallSearchCriteria criteria = new WallSearchCriteria("", ownerId);
        PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.WALL, criteria).tryOpenWith(requireActivity());
    }

    @Override
    public void goToConversationAttachments(int accountId, int ownerId) {
        String[] types = {FindAttachmentType.TYPE_PHOTO, FindAttachmentType.TYPE_VIDEO, FindAttachmentType.TYPE_DOC, FindAttachmentType.TYPE_AUDIO,
                FindAttachmentType.TYPE_LINK, FindAttachmentType.TYPE_ALBUM, FindAttachmentType.TYPE_POST_WITH_COMMENT, FindAttachmentType.TYPE_POST_WITH_QUERY};

        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        menus.add(new OptionRequest(0, getString(R.string.photos), R.drawable.photo_album, true));
        menus.add(new OptionRequest(1, getString(R.string.videos), R.drawable.video, true));
        menus.add(new OptionRequest(2, getString(R.string.documents), R.drawable.book, true));
        menus.add(new OptionRequest(3, getString(R.string.music), R.drawable.song, true));
        menus.add(new OptionRequest(4, getString(R.string.links), R.drawable.web, true));
        menus.add(new OptionRequest(5, getString(R.string.photo_album), R.drawable.album_photo, false));
        menus.add(new OptionRequest(6, getString(R.string.posts_with_comment), R.drawable.comment, false));
        menus.add(new OptionRequest(7, getString(R.string.posts_with_query), R.drawable.magnify, false));

        menus.show(getChildFragmentManager(), "attachments_select", option -> PlaceFactory.getWallAttachmentsPlace(accountId, ownerId, types[option.getId()]).tryOpenWith(requireActivity()));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            callPresenter(AbsWallPresenter::fireRefresh);
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            callPresenter(p -> p.fireEdit(requireActivity()));
            return true;
        } else if (item.getItemId() == R.id.action_copy_url) {
            callPresenter(AbsWallPresenter::fireCopyUrlClick);
            return true;
        } else if (item.getItemId() == R.id.action_copy_id) {
            callPresenter(AbsWallPresenter::fireCopyIdClick);
            return true;
        } else if (item.getItemId() == R.id.action_add_to_shortcut) {
            callPresenter(AbsWallPresenter::fireAddToShortcutClick);
            return true;
        } else if (item.getItemId() == R.id.action_search) {
            callPresenter(AbsWallPresenter::fireSearchClick);
            return true;
        } else if (item.getItemId() == R.id.wall_attachments) {
            callPresenter(AbsWallPresenter::openConversationAttachments);
            return true;
        } else if (item.getItemId() == R.id.search_stories) {
            ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
            menus.add(new OptionRequest(R.id.button_ok, getString(R.string.by_name), R.drawable.pencil, false));
            menus.add(new OptionRequest(R.id.button_cancel, getString(R.string.by_owner), R.drawable.person, false));
            menus.show(requireActivity().getSupportFragmentManager(), "search_story_options", option -> {
                if (item.getItemId() == R.id.button_ok) {
                    callPresenter(p -> p.searchStory(true));
                } else if (item.getItemId() == R.id.button_cancel) {
                    callPresenter(p -> p.searchStory(false));
                }
            });
            return true;
        } else if (item.getItemId() == R.id.action_open_url) {
            ClipboardManager clipBoard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipBoard != null && clipBoard.getPrimaryClip() != null && clipBoard.getPrimaryClip().getItemCount() > 0) {
                String temp = clipBoard.getPrimaryClip().getItemAt(0).getText().toString();
                LinkHelper.openUrl(getActivity(), callPresenter(AccountDependencyPresenter::getAccountId, Settings.get().accounts().getCurrent()), temp);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_wall, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionView view = new OptionView();
        callPresenter(p -> p.fireOptionViewCreated(view));

        boolean isDebug = Settings.get().other().isDeveloper_mode();

        menu.findItem(R.id.action_open_url).setVisible(view.isMy);
        menu.findItem(R.id.search_stories).setVisible(isDebug);
        menu.findItem(R.id.action_edit).setVisible(view.isMy);
        menu.findItem(R.id.action_add_to_shortcut).setVisible(!view.isMy);
    }

    @Override
    public void copyToClipboard(String label, String body) {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, body);
        clipboard.setPrimaryClip(clip);

        getCustomToast().showToast(R.string.copied);
    }

    @Override
    public void goToPostCreation(int accountId, int ownerId, @EditingPostType int postType) {
        PlaceUtil.goToPostCreation(requireActivity(), accountId, ownerId, postType, null);
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @LayoutRes
    protected abstract int headerLayout();

    protected abstract void onHeaderInflated(View headerRootView);

    @Override
    public void displayWallData(List<Post> data) {
        if (nonNull(mWallAdapter)) {
            mWallAdapter.setItems(data);
        }
    }

    @Override
    public void notifyWallDataSetChanged() {
        if (nonNull(mWallAdapter)) {
            mWallAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void updateStory(List<Story> stories) {
        if (nonNull(mStoryAdapter) && !isEmpty(stories)) {
            mStoryAdapter.setItems(stories);
            mStoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyWallItemChanged(int position) {
        if (nonNull(mWallAdapter)) {
            mWallAdapter.notifyItemChanged(position + mWallAdapter.getHeadersCount());
        }
    }

    @Override
    public void notifyWallDataAdded(int position, int count) {
        if (nonNull(mWallAdapter)) {
            mWallAdapter.notifyItemRangeInserted(position + mWallAdapter.getHeadersCount(), count);
        }
    }

    @Override
    public void notifyWallItemRemoved(int index) {
        if (nonNull(mWallAdapter)) {
            mWallAdapter.notifyItemRemoved(index + mWallAdapter.getHeadersCount());
        }
    }

    @Override
    public void onOwnerClick(int ownerId) {
        onOpenOwner(ownerId);
    }

    @Override
    public void onShareClick(Post post) {
        callPresenter(p -> p.fireShareClick(post));
    }

    @Override
    public void onPostClick(Post post) {
        callPresenter(p -> p.firePostBodyClick(post));
    }

    @Override
    public void onRestoreClick(Post post) {
        callPresenter(p -> p.firePostRestoreClick(post));
    }

    @Override
    public void onCommentsClick(Post post) {
        callPresenter(p -> p.fireCommentsClick(post));
    }

    @Override
    public void onLikeLongClick(Post post) {
        callPresenter(p -> p.fireLikeLongClick(post));
    }

    @Override
    public void onShareLongClick(Post post) {
        callPresenter(p -> p.fireShareLongClick(post));
    }

    @Override
    public void onLikeClick(Post post) {
        callPresenter(p -> p.fireLikeClick(post));
    }

    @Override
    public void openPostEditor(int accountId, Post post) {
        PlaceUtil.goToPostEditor(requireActivity(), accountId, post);
    }

    @Override
    public void setupLoadMoreFooter(@LoadMoreState int state) {
        if (nonNull(mLoadMoreFooterHelper)) {
            mLoadMoreFooterHelper.switchToState(state);
        }
    }

    @Override
    public void openPhotoAlbums(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getVKPhotoAlbumsPlace(accountId, ownerId, IVkPhotosView.ACTION_SHOW_PHOTOS, ParcelableOwnerWrapper.wrap(owner))
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openVideosLibrary(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getVideosPlace(accountId, ownerId, IVideosListView.ACTION_SHOW)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openAudios(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getAudiosPlace(accountId, ownerId)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openArticles(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getOwnerArticles(accountId, ownerId)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
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
    public void onButtonRemoveClick(Post post) {
        callPresenter(p -> p.fireButtonRemoveClick(post));
    }

    protected static final class OptionView implements IOptionView {

        boolean isMy;

        boolean isBlacklistedByMe;

        boolean isFavorite;

        boolean isSubscribed;

        @Override
        public void setIsBlacklistedByMe(boolean blocked) {
            isBlacklistedByMe = blocked;
        }

        @Override
        public void setIsMy(boolean my) {
            isMy = my;
        }

        @Override
        public void setIsFavorite(boolean favorite) {
            isFavorite = favorite;
        }

        @Override
        public void setIsSubscribed(boolean subscribed) {
            isSubscribed = subscribed;
        }
    }
}
