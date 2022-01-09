package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.MenuAdapter;
import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.InternalVideoSize;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Text;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.menu.Item;
import dev.ragnarok.fenrir.model.menu.Section;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.VideoPreviewPresenter;
import dev.ragnarok.fenrir.mvp.view.IVideoPreviewView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceUtil;
import dev.ragnarok.fenrir.settings.AppPrefs;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.CircleCounterButton;

public class VideoPreviewFragment extends BaseMvpFragment<VideoPreviewPresenter, IVideoPreviewView> implements View.OnClickListener, View.OnLongClickListener, IVideoPreviewView {

    private static final String EXTRA_VIDEO_ID = "video_id";
    private static final Section SECTION_PLAY = new Section(new Text(R.string.section_play_title));
    private static final Section SECTION_OTHER = new Section(new Text(R.string.other));
    private final OwnerLinkSpanFactory.ActionListener ownerLinkAdapter = new LinkActionAdapter() {
        @Override
        public void onOwnerClick(int ownerId) {
            callPresenter(p -> p.fireOwnerClick(ownerId));
        }
    };
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));
    private View mRootView;
    private CircleCounterButton likeButton;
    private CircleCounterButton commentsButton;
    private TextView mTitleText;
    private TextView mSubtitleText;
    private ImageView mPreviewImage;

    private ImageView mOwnerAvatar;
    private TextView mOwnerText;
    private TextView mUploadDate;
    private TextView mAddedDate;
    private Transformation mTransformation;
    private ViewGroup mOwnerGroup;

    public static Bundle buildArgs(int accountId, int ownerId, int videoId, @Nullable String accessKey, @Nullable Video video) {
        Bundle bundle = new Bundle();

        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putInt(Extra.OWNER_ID, ownerId);
        bundle.putInt(EXTRA_VIDEO_ID, videoId);

        if (!isEmpty(accessKey)) {
            bundle.putString(Extra.ACCESS_KEY, accessKey);
        }

        if (nonNull(video)) {
            bundle.putParcelable(Extra.VIDEO, video);
        }

        return bundle;
    }

    public static VideoPreviewFragment newInstance(Bundle args) {
        VideoPreviewFragment fragment = new VideoPreviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull android.view.Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_video_preview, menu);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull android.view.Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionView view = new OptionView();
        callPresenter(p -> p.fireOptionViewCreated(view));

        menu.findItem(R.id.action_add_to_my_videos).setVisible(view.canAdd);
        menu.findItem(R.id.action_delete_from_my_videos).setVisible(view.isMy);
        menu.findItem(R.id.action_edit).setVisible(view.isMy);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_add_to_my_videos) {
            callPresenter(VideoPreviewPresenter::fireAddToMyClick);
            return true;
        } else if (item.getItemId() == R.id.action_copy_url) {
            callPresenter(p -> p.fireCopyUrlClick(requireActivity()));
            return true;
        } else if (item.getItemId() == R.id.action_delete_from_my_videos) {
            callPresenter(VideoPreviewPresenter::fireDeleteMyClick);
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            callPresenter(p -> p.fireEditVideo(requireActivity()));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_video, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(mRootView.findViewById(R.id.toolbar));

        mPreviewImage = mRootView.findViewById(R.id.fragment_video_preview_image);

        likeButton = mRootView.findViewById(R.id.like_button);
        CircleCounterButton shareButton = mRootView.findViewById(R.id.share_button);
        commentsButton = mRootView.findViewById(R.id.comments_button);

        commentsButton.setOnClickListener(this);
        shareButton.setOnClickListener(this);
        likeButton.setOnClickListener(this);
        likeButton.setOnLongClickListener(this);

        mTitleText = mRootView.findViewById(R.id.fragment_video_title);
        mSubtitleText = mRootView.findViewById(R.id.fragment_video_subtitle);

        mOwnerAvatar = mRootView.findViewById(R.id.item_owner_avatar);
        mOwnerText = mRootView.findViewById(R.id.item_owner_name);
        mUploadDate = mRootView.findViewById(R.id.item_upload_time);
        mAddedDate = mRootView.findViewById(R.id.item_added_time);

        mOwnerGroup = mRootView.findViewById(R.id.item_owner);
        mOwnerGroup.setOnClickListener(v -> callPresenter(VideoPreviewPresenter::fireOpenOwnerClicked));

        mTransformation = CurrentTheme.createTransformationForAvatar();

        if (Settings.get().other().isDo_auto_play_video()) {
            mRootView.findViewById(R.id.cover_cardview).setOnClickListener(v -> callPresenter(VideoPreviewPresenter::fireAutoPlayClick));
            mRootView.findViewById(R.id.cover_cardview).setOnLongClickListener(v -> {
                callPresenter(VideoPreviewPresenter::firePlayClick);
                return true;
            });
        } else {
            mRootView.findViewById(R.id.cover_cardview).setOnClickListener(v -> callPresenter(VideoPreviewPresenter::firePlayClick));
            mRootView.findViewById(R.id.cover_cardview).setOnLongClickListener(v -> {
                callPresenter(VideoPreviewPresenter::fireAutoPlayClick);
                return true;
            });
        }

        mRootView.findViewById(R.id.try_again_button).setOnClickListener(v -> callPresenter(VideoPreviewPresenter::fireTryAgainClick));

        return mRootView;
    }

    private void playWithExternalSoftware(String url) {
        if (isEmpty(url)) {
            Toast.makeText(requireActivity(), R.string.error_video_playback_is_not_possible, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));

        if (nonNull(requireActivity().getPackageManager().resolveActivity(intent, 0))) {
            startActivity(intent);
        } else {
            Toast.makeText(requireActivity(), R.string.no_compatible_software_installed, Toast.LENGTH_SHORT).show();
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<VideoPreviewPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        String documentAccessKey = null;
        if (requireArguments().containsKey(Extra.ACCESS_KEY)) {
            documentAccessKey = requireArguments().getString(Extra.ACCESS_KEY);
        }
        String finalDocumentAccessKey = documentAccessKey;
        return () -> new VideoPreviewPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(EXTRA_VIDEO_ID),
                requireArguments().getInt(Extra.OWNER_ID),
                finalDocumentAccessKey,
                requireArguments().getParcelable(Extra.VIDEO),
                saveInstanceState
        );
    }

    @Override
    public void displayLoading() {
        if (nonNull(mRootView)) {
            mRootView.findViewById(R.id.content).setVisibility(View.GONE);
            mRootView.findViewById(R.id.loading_root).setVisibility(View.VISIBLE);

            mRootView.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.post_loading_text).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.try_again_button).setVisibility(View.GONE);
        }
    }

    @Override
    public void displayLoadingError() {
        if (nonNull(mRootView)) {
            mRootView.findViewById(R.id.content).setVisibility(View.GONE);
            mRootView.findViewById(R.id.loading_root).setVisibility(View.VISIBLE);

            mRootView.findViewById(R.id.progressBar).setVisibility(View.GONE);
            mRootView.findViewById(R.id.post_loading_text).setVisibility(View.GONE);
            mRootView.findViewById(R.id.try_again_button).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void displayVideoInfo(Video video) {
        if (nonNull(mRootView)) {
            mRootView.findViewById(R.id.content).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.loading_root).setVisibility(View.GONE);
        }

        if (video.getDate() != 0 && nonNull(mUploadDate)) {
            mUploadDate.setVisibility(View.VISIBLE);
            mUploadDate.setText(requireActivity().getString(R.string.uploaded_video, AppTextUtils.getDateFromUnixTime(requireActivity(), video.getDate())));
        }

        if (video.getAddingDate() != 0 && nonNull(mAddedDate)) {
            mAddedDate.setVisibility(View.VISIBLE);
            mAddedDate.setText(requireActivity().getString(R.string.added_video, AppTextUtils.getDateFromUnixTime(requireActivity(), video.getAddingDate())));
        }

        safelySetText(mTitleText, video.getTitle());

        if (nonNull(mSubtitleText)) {
            Spannable subtitle = OwnerLinkSpanFactory.withSpans(video.getDescription(), true, false, ownerLinkAdapter);

            mSubtitleText.setText(subtitle, TextView.BufferType.SPANNABLE);
            mSubtitleText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        String imageUrl = video.getImage();

        if (nonEmpty(imageUrl) && nonNull(mPreviewImage)) {
            PicassoInstance.with()
                    .load(imageUrl)
                    .into(mPreviewImage);
        }
    }

    @Override
    public void displayLikes(int count, boolean userLikes) {
        if (nonNull(likeButton)) {
            likeButton.setIcon(userLikes ? R.drawable.heart_filled : R.drawable.heart);
            likeButton.setCount(count);
            likeButton.setActive(userLikes);
        }
    }

    @Override
    public void setCommentButtonVisible(boolean visible) {
        if (nonNull(commentsButton)) {
            commentsButton.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @Override
    public void displayCommentCount(int count) {
        if (nonNull(commentsButton)) {
            commentsButton.setCount(count);
        }
    }

    @Override
    public void showSuccessToast() {
        Toast.makeText(getContext(), R.string.success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showOwnerWall(int accountId, int ownerId) {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(requireActivity());
    }

    @Override
    public void showSubtitle(String subtitle) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void showComments(int accountId, Commented commented) {
        PlaceFactory.getCommentsPlace(accountId, commented, null).tryOpenWith(requireActivity());
    }

    @Override
    public void displayShareDialog(int accountId, Video video, boolean canPostToMyWall) {
        String[] items;
        if (canPostToMyWall) {
            if (!video.getPrivate()) {
                items = new String[]{getString(R.string.share_link), getString(R.string.repost_send_message), getString(R.string.repost_to_wall)};
            } else {
                items = new String[]{getString(R.string.repost_send_message), getString(R.string.repost_to_wall)};
            }
        } else {
            if (!video.getPrivate()) {
                items = new String[]{getString(R.string.share_link), getString(R.string.repost_send_message)};
            } else {
                items = new String[]{getString(R.string.repost_send_message)};
            }
        }

        new MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, (dialogInterface, i) -> {
                    if (video.getPrivate()) {
                        switch (i) {
                            case 0:
                                SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, video);
                                break;
                            case 1:
                                PlaceUtil.goToPostCreation(requireActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(video));
                                break;
                        }
                    } else {
                        switch (i) {
                            case 0:
                                Utils.shareLink(requireActivity(), "https://vk.com/video" + video.getOwnerId() + "_" + video.getId(), video.getTitle());
                                break;
                            case 1:
                                SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, video);
                                break;
                            case 2:
                                PlaceUtil.goToPostCreation(requireActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(video));
                                break;
                        }
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.repost_title)
                .show();
    }

    private List<Item> createDirectVkPlayItems(Video video, Section section, boolean isDownload) {
        List<Item> items = new ArrayList<>();
        if (nonEmpty(video.getHls()) && !isDownload) {
            items.add(new Item(Menu.HLS, new Text(R.string.play_hls))
                    .setIcon(R.drawable.video)
                    .setColor(Color.parseColor("#ff0000"))
                    .setSection(section));
        }

        if (nonEmpty(video.getLive()) && !isDownload) {
            items.add(new Item(Menu.LIVE, new Text(R.string.player_live))
                    .setSection(section)
                    .setColor(Color.parseColor("#ff0000"))
                    .setIcon(R.drawable.video));
        }

        if (nonEmpty(video.getMp4link240())) {
            items.add(new Item(Menu.P_240, new Text(R.string.play_240))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link360())) {
            items.add(new Item(Menu.P_360, new Text(R.string.play_360))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link480())) {
            items.add(new Item(Menu.P_480, new Text(R.string.play_480))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link720())) {
            items.add(new Item(Menu.P_720, new Text(R.string.play_720))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }

        if (nonEmpty(video.getMp4link1080())) {
            items.add(new Item(Menu.P_1080, new Text(R.string.play_1080))
                    .setIcon(R.drawable.video)
                    .setSection(section));
        }
        return items;
    }

    @Override
    public void showVideoPlayMenu(int accountId, Video video) {
        if (isNull(video)) {
            return;
        }

        List<Item> items = new ArrayList<>(createDirectVkPlayItems(video, SECTION_PLAY, false));

        String external = video.getExternalLink();

        if (nonEmpty(external)) {
            if (external.contains("youtube")) {
                boolean hasVanced = AppPrefs.isVancedYoutubeInstalled(requireActivity());
                if (hasVanced) {
                    items.add(new Item(Menu.YOUTUBE_VANCED, new Text(R.string.title_play_in_youtube_vanced))
                            .setIcon(R.drawable.ic_play_youtube)
                            .setSection(SECTION_PLAY));
                }
                items.add(new Item(Menu.NEW_PIPE, new Text(R.string.title_play_in_newpipe))
                        .setIcon(R.drawable.ic_new_pipe)
                        .setSection(SECTION_PLAY));
                if (!hasVanced) {
                    items.add(new Item(Menu.YOUTUBE, new Text(R.string.title_play_in_youtube))
                            .setIcon(R.drawable.ic_play_youtube)
                            .setSection(SECTION_PLAY));
                }

            } else if (external.contains("coub") && AppPrefs.isCoubInstalled(requireActivity())) {
                items.add(new Item(Menu.COUB, new Text(R.string.title_play_in_coub))
                        .setIcon(R.drawable.ic_play_coub)
                        .setSection(SECTION_PLAY));
            }

            items.add(new Item(Menu.PLAY_ANOTHER_SOFT, new Text(R.string.title_play_in_another_software))
                    .setSection(SECTION_OTHER)
                    .setIcon(R.drawable.ic_external));
        }

        if (nonEmpty(firstNonEmptyString(video.getMp4link240(),
                video.getMp4link360(),
                video.getMp4link480(),
                video.getMp4link720(),
                video.getMp4link1080(),
                video.getLive(),
                video.getHls()))) {

            // потом выбираем качество
            items.add(new Item(Menu.P_EXTERNAL_PLAYER, new Text(R.string.play_in_external_player))
                    .setIcon(R.drawable.ic_external)
                    .setSection(SECTION_OTHER));
        }

        items.add(new Item(Menu.PLAY_BROWSER, new Text(R.string.title_play_in_browser))
                .setIcon(R.drawable.ic_external)
                .setSection(SECTION_OTHER));

        if (nonEmpty(external)) {
            items.add(new Item(Menu.COPY_LINK, new Text(R.string.target_url))
                    .setIcon(R.drawable.content_copy)
                    .setSection(SECTION_OTHER));
        }

        items.add(new Item(Menu.ADD_TO_FAVE, new Text(R.string.add_to_bookmarks))
                .setIcon(R.drawable.star)
                .setSection(SECTION_OTHER));

        if (nonEmpty(firstNonEmptyString(video.getMp4link240(),
                video.getMp4link360(),
                video.getMp4link480(),
                video.getMp4link720(),
                video.getMp4link1080()))) {
            items.add(new Item(Menu.DOWNLOAD, new Text(R.string.download))
                    .setIcon(R.drawable.save)
                    .setSection(SECTION_OTHER));
        }

        MenuAdapter adapter = new MenuAdapter(requireActivity(), items, false);

        new MaterialAlertDialogBuilder(requireActivity())
                .setAdapter(adapter, (dialog, which) -> onPlayMenuItemClick(video, items.get(which)))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void doAutoPlayVideo(int accountId, Video video) {
        if (!isEmpty(video.getLive())) {
            openInternal(video, InternalVideoSize.SIZE_LIVE);
        } else if (!isEmpty(video.getHls())) {
            openInternal(video, InternalVideoSize.SIZE_HLS);
        } else if (!isEmpty(video.getMp4link1080())) {
            openInternal(video, InternalVideoSize.SIZE_1080);
        } else if (!isEmpty(video.getMp4link720())) {
            openInternal(video, InternalVideoSize.SIZE_720);
        } else if (!isEmpty(video.getMp4link480())) {
            openInternal(video, InternalVideoSize.SIZE_480);
        } else if (!isEmpty(video.getMp4link360())) {
            openInternal(video, InternalVideoSize.SIZE_360);
        } else if (!isEmpty(video.getMp4link240())) {
            openInternal(video, InternalVideoSize.SIZE_240);
        } else if (nonEmpty(video.getExternalLink())) {
            if (video.getExternalLink().contains("youtube")) {
                if (AppPrefs.isVancedYoutubeInstalled(requireActivity())) {
                    playWithYoutubeVanced(video);
                } else if (AppPrefs.isNewPipeInstalled(requireActivity())) {
                    playWithNewPipe(video);
                } else if (AppPrefs.isYoutubeInstalled(requireActivity())) {
                    playWithYoutube(video);
                } else {
                    playWithExternalSoftware(video.getExternalLink());
                }
            } else if (video.getExternalLink().contains("coub") && AppPrefs.isCoubInstalled(requireActivity())) {
                playWithCoub(video);
            } else {
                playWithExternalSoftware(video.getExternalLink());
            }
        } else if (!isEmpty(video.getPlayer())) {
            playWithExternalSoftware(video.getPlayer());
        } else {
            CustomToast.CreateCustomToast(requireActivity()).showToastError(R.string.video_not_have_link);
        }
    }

    @Override
    public void goToLikes(int accountId, String type, int ownerId, int id) {
        PlaceFactory.getLikesCopiesPlace(accountId, type, ownerId, id, ILikesInteractor.FILTER_LIKES)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void displayOwner(@NonNull Owner owner) {
        if (nonNull(mOwnerGroup)) {
            mOwnerGroup.setVisibility(View.VISIBLE);
        }
        if (nonNull(mOwnerAvatar)) {
            mOwnerAvatar.setVisibility(View.VISIBLE);
            ViewUtils.displayAvatar(mOwnerAvatar, mTransformation, owner.getMaxSquareAvatar(), Constants.PICASSO_TAG);
        }
        if (nonNull(mOwnerText)) {
            mOwnerText.setVisibility(View.VISIBLE);
            mOwnerText.setText(owner.getFullName());
        }
    }

    private void onPlayMenuItemClick(Video video, Item item) {
        switch (item.getKey()) {
            case Menu.P_240:
                openInternal(video, InternalVideoSize.SIZE_240);
                break;

            case Menu.P_360:
                openInternal(video, InternalVideoSize.SIZE_360);
                break;

            case Menu.P_480:
                openInternal(video, InternalVideoSize.SIZE_480);
                break;

            case Menu.P_720:
                openInternal(video, InternalVideoSize.SIZE_720);
                break;

            case Menu.P_1080:
                openInternal(video, InternalVideoSize.SIZE_1080);
                break;

            case Menu.LIVE:
                openInternal(video, InternalVideoSize.SIZE_LIVE);
                break;

            case Menu.HLS:
                openInternal(video, InternalVideoSize.SIZE_HLS);
                break;

            case Menu.P_EXTERNAL_PLAYER:
                showPlayExternalPlayerMenu(video);
                break;

            case Menu.NEW_PIPE:
                if (AppPrefs.isNewPipeInstalled(requireActivity())) {
                    playWithNewPipe(video);
                } else {
                    LinkHelper.openLinkInBrowser(requireActivity(), "https://github.com/TeamNewPipe/NewPipe/releases");
                }
                break;

            case Menu.YOUTUBE:
                playWithYoutube(video);
                break;

            case Menu.YOUTUBE_VANCED:
                playWithYoutubeVanced(video);
                break;

            case Menu.COUB:
                playWithCoub(video);
                break;

            case Menu.PLAY_ANOTHER_SOFT:
                playWithExternalSoftware(video.getExternalLink());
                break;

            case Menu.PLAY_BROWSER:
                playWithExternalSoftware(video.getPlayer());
                break;

            case Menu.DOWNLOAD:
                if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                    requestWritePermission.launch();
                } else {
                    showDownloadPlayerMenu(video);
                }
                break;
            case Menu.ADD_TO_FAVE:
                callPresenter(VideoPreviewPresenter::fireAddFaveVideo);
                break;
            case Menu.COPY_LINK:
                ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("response", video.getExternalLink());
                clipboard.setPrimaryClip(clip);
                CustomToast.CreateCustomToast(getContext()).showToast(R.string.copied);
                break;
        }
    }

    private void showPlayExternalPlayerMenu(Video video) {
        Section section = new Section(new Text(R.string.title_select_resolution));
        List<Item> items = createDirectVkPlayItems(video, section, false);
        MenuAdapter adapter = new MenuAdapter(requireActivity(), items, false);

        new MaterialAlertDialogBuilder(requireActivity())
                .setAdapter(adapter, (dialog, which) -> {
                    Item item = items.get(which);
                    switch (item.getKey()) {
                        case Menu.P_240:
                            playDirectVkLinkInExternalPlayer(video.getMp4link240());
                            break;
                        case Menu.P_360:
                            playDirectVkLinkInExternalPlayer(video.getMp4link360());
                            break;
                        case Menu.P_480:
                            playDirectVkLinkInExternalPlayer(video.getMp4link480());
                            break;
                        case Menu.P_720:
                            playDirectVkLinkInExternalPlayer(video.getMp4link720());
                            break;
                        case Menu.P_1080:
                            playDirectVkLinkInExternalPlayer(video.getMp4link1080());
                            break;
                        case Menu.LIVE:
                            playDirectVkLinkInExternalPlayer(video.getLive());
                            break;
                        case Menu.HLS:
                            playDirectVkLinkInExternalPlayer(video.getHls());
                            break;
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void showDownloadPlayerMenu(Video video) {
        Section section = new Section(new Text(R.string.download));
        List<Item> items = createDirectVkPlayItems(video, section, true);
        MenuAdapter adapter = new MenuAdapter(requireActivity(), items, false);

        new MaterialAlertDialogBuilder(requireActivity())
                .setAdapter(adapter, (dialog, which) -> {
                    Item item = items.get(which);
                    switch (item.getKey()) {
                        case Menu.P_240:
                            DownloadWorkUtils.doDownloadVideo(requireActivity(), video, video.getMp4link240(), "240");
                            break;
                        case Menu.P_360:
                            DownloadWorkUtils.doDownloadVideo(requireActivity(), video, video.getMp4link360(), "360");
                            break;
                        case Menu.P_480:
                            DownloadWorkUtils.doDownloadVideo(requireActivity(), video, video.getMp4link480(), "480");
                            break;
                        case Menu.P_720:
                            DownloadWorkUtils.doDownloadVideo(requireActivity(), video, video.getMp4link720(), "720");
                            break;
                        case Menu.P_1080:
                            DownloadWorkUtils.doDownloadVideo(requireActivity(), video, video.getMp4link1080(), "1080");
                            break;
                    }
                })
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void playDirectVkLinkInExternalPlayer(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), "video/mp4");

        if (nonNull(requireActivity().getPackageManager().resolveActivity(intent, 0))) {
            startActivity(intent);
        } else {
            Utils.showRedTopToast(requireActivity(), R.string.no_compatible_software_installed);
        }
    }

    private void openInternal(Video video, @InternalVideoSize int size) {
        PlaceFactory.getVkInternalPlayerPlace(video, size, false).tryOpenWith(requireActivity());
    }

    private void playWithCoub(Video video) {
        String outerLink = video.getExternalLink();

        Intent intent = new Intent();
        intent.setData(Uri.parse(outerLink));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName("com.coub.android", "com.coub.android.ui.ViewCoubActivity"));
        startActivity(intent);
    }

    private void playWithNewPipe(Video video) {
        String outerLink = video.getExternalLink();

        Intent intent = new Intent();
        intent.setData(Uri.parse(outerLink));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName("org.schabi.newpipe", "org.schabi.newpipe.RouterActivity"));
        startActivity(intent);
    }

    private void playWithYoutube(Video video) {
        String outerLink = video.getExternalLink();

        Intent intent = new Intent();
        intent.setData(Uri.parse(outerLink));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName("com.google.android.youtube", "com.google.android.apps.youtube.app.application.Shell$UrlActivity"));
        startActivity(intent);
    }

    private void playWithYoutubeVanced(Video video) {
        String outerLink = video.getExternalLink();

        Intent intent = new Intent();
        intent.setData(Uri.parse(outerLink));
        intent.setAction(Intent.ACTION_VIEW);
        intent.setComponent(new ComponentName("com.vanced.android.youtube", "com.google.android.apps.youtube.app.application.Shell$UrlActivity"));
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.video);
        }

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
    public void onClick(View v) {
        if (v.getId() == R.id.like_button) {
            callPresenter(VideoPreviewPresenter::fireLikeClick);
        } else if (v.getId() == R.id.comments_button) {
            callPresenter(VideoPreviewPresenter::fireCommentsClick);
        } else if (v.getId() == R.id.share_button) {
            callPresenter(VideoPreviewPresenter::fireShareClick);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getId() == R.id.like_button) {
            callPresenter(VideoPreviewPresenter::fireLikeLongClick);
            return true;
        }
        return false;
    }

    private static final class OptionView implements IVideoPreviewView.IOptionView {

        boolean canAdd;

        boolean isMy;

        @Override
        public void setCanAdd(boolean can) {
            canAdd = can;
        }

        @Override
        public void setIsMy(boolean my) {
            isMy = my;
        }
    }

    private static final class Menu {
        static final int P_240 = 240;
        static final int P_360 = 360;
        static final int P_480 = 480;
        static final int P_720 = 720;
        static final int P_1080 = 1080;
        static final int HLS = -1;
        static final int LIVE = -2;
        static final int P_EXTERNAL_PLAYER = -3;

        static final int YOUTUBE = -4;
        static final int YOUTUBE_VANCED = -5;
        static final int NEW_PIPE = -6;
        static final int COUB = -7;

        static final int PLAY_ANOTHER_SOFT = -8;
        static final int PLAY_BROWSER = -9;
        static final int DOWNLOAD = -10;
        static final int COPY_LINK = -11;

        static final int ADD_TO_FAVE = -12;
    }
}
