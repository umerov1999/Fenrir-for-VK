package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.DualTabPhotoActivity;
import dev.ragnarok.fenrir.activity.PhotosActivity;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalOptionsAdapter;
import dev.ragnarok.fenrir.model.FriendsCounters;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.LocalVideo;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.PostFilter;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserDetails;
import dev.ragnarok.fenrir.model.selection.FileManagerSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalGallerySelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalPhotosSelectableSource;
import dev.ragnarok.fenrir.model.selection.LocalVideosSelectableSource;
import dev.ragnarok.fenrir.model.selection.Sources;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.UserWallPresenter;
import dev.ragnarok.fenrir.mvp.view.IUserWallView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.BlurTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.InputTextDialog;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import me.minetsh.imaging.IMGEditActivity;

public class UserWallFragment extends AbsWallFragment<IUserWallView, UserWallPresenter>
        implements IUserWallView {

    private final ActivityResultLauncher<Intent> openRequestResizeAvatar = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            callPresenter(p -> p.fireNewAvatarPhotoSelected(UCrop.getOutput(result.getData()).getPath()));
        } else if (result.getResultCode() == UCrop.RESULT_ERROR) {
            showThrowable(UCrop.getError(result.getData()));
        }
    });
    private final ActivityResultLauncher<Intent> openRequestSelectAvatar = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            ArrayList<LocalPhoto> photos = result.getData().getParcelableArrayListExtra(Extra.PHOTOS);
            if (nonEmpty(photos)) {
                Uri to_up = photos.get(0).getFullImageUri();
                if (new File(to_up.getPath()).isFile()) {
                    to_up = Uri.fromFile(new File(to_up.getPath()));
                }
                openRequestResizeAvatar.launch(UCrop.of(to_up, Uri.fromFile(new File(requireActivity().getExternalCacheDir() + File.separator + "scale.jpg")))
                        .withAspectRatio(1, 1)
                        .getIntent(requireActivity()));
            }
        }
    });
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(p -> p.fireShowQR(requireActivity())));
    private final ActivityResultLauncher<Intent> openRequestPhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getData() != null && result.getResultCode() == Activity.RESULT_OK) {
            ArrayList<LocalPhoto> localPhotos = result.getData().getParcelableArrayListExtra(Extra.PHOTOS);
            String file = result.getData().getStringExtra(FileManagerFragment.returnFileParameter);
            LocalVideo video = result.getData().getParcelableExtra(Extra.VIDEO);
            callPresenter(p -> p.firePhotosSelected(localPhotos, file, video));
        }
    });

    private final ActivityResultLauncher<Intent> openRequestResizePhoto = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            assert result.getData() != null;
            callPresenter(p -> p.doUploadFile(result.getData().getStringExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH), Upload.IMAGE_SIZE_FULL, false));
        }
    });
    private UserHeaderHolder mHeaderHolder;

    @SuppressLint("SetTextI18n")
    @Override
    public void displayBaseUserInfo(User user) {
        if (isNull(mHeaderHolder)) return;

        if (Utils.isEmpty(user.getMaiden_name())) {
            mHeaderHolder.tvName.setText(user.getFullName());
        } else {
            mHeaderHolder.tvName.setText(firstNonEmptyString(user.getFullName(), " ") + " (" + user.getMaiden_name() + ")");
        }
        mHeaderHolder.tvLastSeen.setText(UserInfoResolveUtil.getUserActivityLine(getContext(), user, true));

        if (!user.getCanWritePrivateMessage())
            mHeaderHolder.fabMessage.setImageResource(R.drawable.close);
        else
            mHeaderHolder.fabMessage.setImageResource(R.drawable.email);

        String screenName = nonEmpty(user.getDomain()) ? "@" + user.getDomain() : null;
        mHeaderHolder.tvScreenName.setText(screenName);
        mHeaderHolder.tvName.setTextColor(Utils.getVerifiedColor(requireActivity(), user.isVerified()));
        mHeaderHolder.tvScreenName.setTextColor(Utils.getVerifiedColor(requireActivity(), user.isVerified()));

        String photoUrl = user.getMaxSquareAvatar();

        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .transform(CurrentTheme.createTransformationForAvatar())
                    .into(mHeaderHolder.ivAvatar);

            if (Settings.get().other().isShow_wall_cover()) {
                PicassoInstance.with()
                        .load(photoUrl)
                        .transform(new BlurTransformation(6f, requireActivity()))
                        .into(mHeaderHolder.vgCover);
            }
        }
        int donate_anim = Settings.get().other().getDonate_anim_set();
        if (donate_anim > 0 && user.isDonated()) {
            mHeaderHolder.bDonate.setVisibility(View.VISIBLE);
            mHeaderHolder.bDonate.setAutoRepeat(true);
            if (donate_anim == 2) {
                String cur = Settings.get().ui().getMainThemeKey();
                if ("fire".equals(cur) || "yellow_violet".equals(cur)) {
                    mHeaderHolder.tvName.setTextColor(Color.parseColor("#df9d00"));
                    mHeaderHolder.tvScreenName.setTextColor(Color.parseColor("#df9d00"));
                    Utils.setBackgroundTint(mHeaderHolder.ivVerified, Color.parseColor("#df9d00"));
                    mHeaderHolder.bDonate.fromRes(R.raw.donater_fire, Utils.dp(100), Utils.dp(100));
                } else {
                    mHeaderHolder.tvName.setTextColor(CurrentTheme.getColorPrimary(requireActivity()));
                    mHeaderHolder.tvScreenName.setTextColor(CurrentTheme.getColorPrimary(requireActivity()));
                    Utils.setBackgroundTint(mHeaderHolder.ivVerified, CurrentTheme.getColorPrimary(requireActivity()));
                    mHeaderHolder.bDonate.fromRes(R.raw.donater_fire, Utils.dp(100), Utils.dp(100), new int[]{0xFF812E, CurrentTheme.getColorPrimary(requireActivity())}, true);
                }
            } else {
                mHeaderHolder.bDonate.fromRes(R.raw.donater, Utils.dp(100), Utils.dp(100), new int[]{0xffffff, CurrentTheme.getColorPrimary(requireActivity()), 0x777777, CurrentTheme.getColorSecondary(requireActivity())});
            }
            mHeaderHolder.bDonate.playAnimation();
        } else {
            mHeaderHolder.bDonate.setImageDrawable(null);
            mHeaderHolder.bDonate.setVisibility(View.GONE);
        }
        mHeaderHolder.ivVerified.setVisibility(user.isVerified() ? View.VISIBLE : View.GONE);

        Integer onlineIcon = ViewUtils.getOnlineIcon(true, user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if (!user.isOnline())
            mHeaderHolder.ivOnline.setCircleColor(CurrentTheme.getColorFromAttrs(R.attr.icon_color_inactive, requireContext(), "#000000"));
        else
            mHeaderHolder.ivOnline.setCircleColor(CurrentTheme.getColorFromAttrs(R.attr.icon_color_active, requireContext(), "#000000"));

        if (onlineIcon != null) {
            mHeaderHolder.ivOnline.setIcon(onlineIcon);
        }
        if (user.getBlacklisted()) {
            Utils.ColoredSnack(requireView(), R.string.blacklisted, BaseTransientBottomBar.LENGTH_LONG, Color.parseColor("#ccc9a200")).show();
        }
    }

    @Override
    public void openUserDetails(int accountId, @NonNull User user, @NonNull UserDetails details) {
        PlaceFactory.getUserDetailsPlace(accountId, user, details).tryOpenWith(requireActivity());
    }

    /*@Override
    public void displayOwnerData(User user) {
        if (isNull(mHeaderHolder)) return;

        mHeaderHolder.tvName.setText(user.getFullName());
        mHeaderHolder.tvLastSeen.setText(UserInfoResolveUtil.getUserActivityLine(requireActivity(), user));
        mHeaderHolder.tvLastSeen.setAllCaps(false);

        String screenName = "@" + user.screen_name;
        mHeaderHolder.tvScreenName.setText(screenName);

        if (isNull(user.status_audio)) {
            String status = "\"" + user.status + "\"";
            mHeaderHolder.tvStatus.setText(status);
        } else {
            String status = user.status_audio.artist + '-' + user.status_audio.title;
            mHeaderHolder.tvStatus.setText(status);
        }

        mHeaderHolder.tvStatus.setVisibility(isEmpty(user.status) && user.status_audio == null ? View.GONE : View.VISIBLE);

        String photoUrl = user.getMaxSquareAvatar();

        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .transform(new RoundTransformation())
                    .into(mHeaderHolder.ivAvatar);
        }

        Integer onlineIcon = ViewUtils.getOnlineIcon(user.online, user.online_mobile, user.platform, user.online_app);
        mHeaderHolder.ivOnline.setVisibility(user.online ? View.VISIBLE : View.GONE);

        if (onlineIcon != null) {
            mHeaderHolder.ivOnline.setIcon(onlineIcon);
        }

        *//*View mainUserInfoView = mHeaderHolder.infoSections.findViewById(R.id.section_contact_info);
        UserInfoResolveUtil.fillMainUserInfo(requireActivity(), mainUserInfoView, user, new LinkActionAdapter() {
            @Override
            public void onOwnerClick(int ownerId) {
                onOpenOwner(ownerId);
            }
        });

        UserInfoResolveUtil.fill(requireActivity(), mHeaderHolder.infoSections.findViewById(R.id.section_beliefs), user);
        UserInfoResolveUtil.fillPersonalInfo(requireActivity(), mHeaderHolder.infoSections.findViewById(R.id.section_personal), user);*//*

        SelectionUtils.addSelectionProfileSupport(getContext(), mHeaderHolder.avatarRoot, user);
    }*/

    @Override
    public void showAvatarUploadedMessage(int accountId, Post post) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.success)
                .setMessage(R.string.avatar_was_changed_successfully)
                .setPositiveButton(R.string.button_show, (dialog, which) -> PlaceFactory.getPostPreviewPlace(accountId, post.getVkid(), post.getOwnerId(), post).tryOpenWith(requireActivity()))
                .setNegativeButton(R.string.button_ok, null)
                .show();
    }

    @Override
    public void doEditPhoto(@NonNull Uri uri) {
        try {
            openRequestResizePhoto.launch(new Intent(requireContext(), IMGEditActivity.class)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_URI, uri)
                    .putExtra(IMGEditActivity.EXTRA_IMAGE_SAVE_PATH, new File(requireActivity().getExternalCacheDir() + File.separator + "scale.jpg").getAbsolutePath()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void displayCounters(int friends, int mutual, int followers, int groups, int photos, int audios, int videos, int articles, int products, int gifts) {
        if (nonNull(mHeaderHolder)) {
            if (Settings.get().other().isShow_mutual_count()) {
                setupCounterWith(mHeaderHolder.bFriends, friends, mutual);
            } else {
                setupCounter(mHeaderHolder.bFriends, friends);
            }
            setupCounter(mHeaderHolder.bGroups, groups);
            setupCounter(mHeaderHolder.bPhotos, photos);
            setupCounter(mHeaderHolder.bAudios, audios);
            setupCounter(mHeaderHolder.bVideos, videos);
            setupCounter(mHeaderHolder.bArticles, articles);
            setupCounter(mHeaderHolder.bProducts, products);
            setupCounter(mHeaderHolder.bGifts, gifts);
        }
    }

    @Override
    public void displayUserStatus(String statusText, boolean swAudioIcon) {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.tvStatus.setText(statusText);
            mHeaderHolder.tvAudioStatus.setVisibility(swAudioIcon ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected int headerLayout() {
        return R.layout.header_user_profile;
    }

    @Override
    protected void onHeaderInflated(View headerRootView) {
        mHeaderHolder = new UserHeaderHolder(headerRootView);
        mHeaderHolder.ivAvatar.setOnClickListener(v -> callPresenter(UserWallPresenter::fireAvatarClick));
        mHeaderHolder.ivAvatar.setOnLongClickListener(v -> {
            callPresenter(UserWallPresenter::fireAvatarLongClick);
            return true;
        });
        setupPaganContent(mHeaderHolder.Runes, mHeaderHolder.paganSymbol);
    }

    @NonNull
    @Override
    public IPresenterFactory<UserWallPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            requireArguments();

            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            ParcelableOwnerWrapper wrapper = requireArguments().getParcelable(Extra.OWNER);
            AssertUtils.requireNonNull(wrapper);

            return new UserWallPresenter(accountId, ownerId, (User) wrapper.get(), requireActivity(), saveInstanceState);
        };
    }

    @Override
    public void displayWallFilters(List<PostFilter> filters) {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mPostFilterAdapter.setItems(filters);
        }
    }

    @Override
    public void notifyWallFiltersChanged() {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mPostFilterAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupPrimaryActionButton(@StringRes Integer resourceId) {
        if (nonNull(mHeaderHolder) && nonNull(resourceId)) {
            mHeaderHolder.bPrimaryAction.setText(resourceId);
        }
    }

    @Override
    public void openFriends(int accountId, int userId, int tab, FriendsCounters counters) {
        PlaceFactory.getFriendsFollowersPlace(accountId, userId, tab, counters).tryOpenWith(requireActivity());
    }

    @Override
    public void openGroups(int accountId, int userId, @Nullable User user) {
        PlaceFactory.getCommunitiesPlace(accountId, userId)
                .withParcelableExtra(Extra.USER, user)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openProducts(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getMarketPlace(accountId, ownerId, 0).tryOpenWith(requireActivity());
    }

    @Override
    public void openGifts(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getGiftsPlace(accountId, ownerId).tryOpenWith(requireActivity());
    }

    @Override
    public void showEditStatusDialog(String initialValue) {
        new InputTextDialog.Builder(requireActivity())
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setTitleRes(R.string.edit_status)
                .setHint(R.string.enter_your_status)
                .setValue(initialValue)
                .setAllowEmpty(true)
                .setCallback(newValue -> callPresenter(p -> p.fireNewStatusEntered(newValue)))
                .show();
    }

    @Override
    public void showAddToFriendsMessageDialog() {
        new InputTextDialog.Builder(requireActivity())
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setTitleRes(R.string.add_to_friends)
                .setHint(R.string.attach_message)
                .setAllowEmpty(true)
                .setCallback(newValue -> callPresenter(p -> p.fireAddToFrindsClick(newValue)))
                .show();
    }

    @Override
    public void showDeleteFromFriendsMessageDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.delete_from_friends)
                .setPositiveButton(R.string.button_yes, (dialogInterface, i) -> callPresenter(UserWallPresenter::fireDeleteFromFriends))
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    @Override
    public void showUnbanMessageDialog() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.is_to_blacklist)
                .setPositiveButton(R.string.button_yes, (dialogInterface, i) -> callPresenter(UserWallPresenter::fireRemoveBlacklistClick))
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    @Override
    public void showAvatarContextMenu(boolean canUploadAvatar) {
        String[] items;
        if (canUploadAvatar) {
            items = new String[]{getString(R.string.open_photo), getString(R.string.open_avatar), getString(R.string.upload_new_photo), getString(R.string.upload_new_story)};
        } else {
            items = new String[]{getString(R.string.open_photo), getString(R.string.open_avatar)};
        }

        new MaterialAlertDialogBuilder(requireActivity()).setItems(items, (dialogInterface, i) -> {
            switch (i) {
                case 0:
                    callPresenter(UserWallPresenter::fireOpenAvatarsPhotoAlbum);
                    break;
                case 1:
                    User usr = callPresenter(UserWallPresenter::getUser, null);
                    if (usr == null) {
                        break;
                    }
                    PlaceFactory.getSingleURLPhotoPlace(usr.getOriginalAvatar(), usr.getFullName(), "id" + usr.getId()).tryOpenWith(requireActivity());
                    break;
                case 2:
                    Intent attachPhotoIntent = new Intent(requireActivity(), PhotosActivity.class);
                    attachPhotoIntent.putExtra(PhotosActivity.EXTRA_MAX_SELECTION_COUNT, 1);
                    openRequestSelectAvatar.launch(attachPhotoIntent);
                    break;
                case 3:
                    Sources sources = new Sources()
                            .with(new LocalPhotosSelectableSource())
                            .with(new LocalGallerySelectableSource())
                            .with(new LocalVideosSelectableSource())
                            .with(new FileManagerSelectableSource());

                    Intent intent = DualTabPhotoActivity.createIntent(requireActivity(), 1, sources);
                    openRequestPhoto.launch(intent);
                    break;
            }
        }).setCancelable(true).show();
    }

    @Override
    public void showMention(int accountId, int ownerId) {
        PlaceFactory.getMentionsPlace(accountId, ownerId).tryOpenWith(requireActivity());
    }

    @Override
    public void InvalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.profile);
        ActivityUtils.setToolbarSubtitle(this, null);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        OptionView view = new OptionView();
        callPresenter(p -> p.fireOptionViewCreated(view));
        menu.add(R.string.registration_date).setOnMenuItemClickListener(item -> {
            callPresenter(UserWallPresenter::fireGetRegistrationDate);
            return true;
        });
        menu.add(R.string.rename).setOnMenuItemClickListener(item -> {
            new InputTextDialog.Builder(requireActivity())
                    .setTitleRes(R.string.rename_local)
                    .setAllowEmpty(true)
                    .setInputType(InputType.TYPE_CLASS_TEXT)
                    .setCallback(newValue -> callPresenter(p -> p.renameLocal(newValue)))
                    .show();
            return true;
        });
        if (!view.isMy) {
            menu.add(R.string.report).setOnMenuItemClickListener(item -> {
                callPresenter(UserWallPresenter::fireReport);
                return true;
            });
            if (!view.isBlacklistedByMe) {
                menu.add(R.string.add_to_blacklist).setOnMenuItemClickListener(item -> {
                    callPresenter(UserWallPresenter::fireAddToBlacklistClick);
                    return true;
                });
            }
            if (!view.isSubscribed) {
                menu.add(R.string.notify_wall_added).setOnMenuItemClickListener(item -> {
                    callPresenter(UserWallPresenter::fireSubscribe);
                    return true;
                });
            } else {
                menu.add(R.string.unnotify_wall_added).setOnMenuItemClickListener(item -> {
                    callPresenter(UserWallPresenter::fireUnSubscribe);
                    return true;
                });
            }
            if (!view.isFavorite) {
                menu.add(R.string.add_to_bookmarks).setOnMenuItemClickListener(item -> {
                    callPresenter(UserWallPresenter::fireAddToBookmarks);
                    return true;
                });
            } else {
                menu.add(R.string.remove_from_bookmarks).setOnMenuItemClickListener(item -> {
                    callPresenter(UserWallPresenter::fireRemoveFromBookmarks);
                    return true;
                });
            }
        }
        menu.add(R.string.show_qr).setOnMenuItemClickListener(item -> {
            if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                requestWritePermission.launch();
            } else {
                callPresenter(p -> p.fireShowQR(requireActivity()));
            }
            return true;
        });
        menu.add(R.string.mentions).setOnMenuItemClickListener(item -> {
            callPresenter(UserWallPresenter::fireMentions);
            return true;
        });
    }

    private class UserHeaderHolder {
        final ImageView vgCover;
        final ViewGroup avatarRoot;
        final ImageView ivAvatar;
        final ImageView ivVerified;
        final TextView tvName;
        final TextView tvScreenName;
        final TextView tvStatus;
        final ImageView tvAudioStatus;
        final TextView tvLastSeen;
        final OnlineView ivOnline;

        final TextView bFriends;
        final TextView bGroups;
        final TextView bPhotos;
        final TextView bVideos;
        final TextView bAudios;
        final TextView bArticles;
        final TextView bProducts;
        final TextView bGifts;

        final FloatingActionButton fabMessage;
        final FloatingActionButton fabMoreInfo;
        final MaterialButton bPrimaryAction;
        final RLottieImageView bDonate;

        final RLottieImageView paganSymbol;
        final View Runes;

        final HorizontalOptionsAdapter<PostFilter> mPostFilterAdapter;

        UserHeaderHolder(@NonNull View root) {
            vgCover = root.findViewById(R.id.cover);
            tvStatus = root.findViewById(R.id.fragment_user_profile_status);
            tvAudioStatus = root.findViewById(R.id.fragment_user_profile_audio);
            tvName = root.findViewById(R.id.fragment_user_profile_name);
            tvScreenName = root.findViewById(R.id.fragment_user_profile_id);
            tvLastSeen = root.findViewById(R.id.fragment_user_profile_activity);
            avatarRoot = root.findViewById(R.id.fragment_user_profile_avatar_container);
            ivAvatar = root.findViewById(R.id.avatar);
            ivOnline = root.findViewById(R.id.header_navi_menu_online);
            bFriends = root.findViewById(R.id.fragment_user_profile_bfriends);
            bGroups = root.findViewById(R.id.fragment_user_profile_bgroups);
            bPhotos = root.findViewById(R.id.fragment_user_profile_bphotos);
            bVideos = root.findViewById(R.id.fragment_user_profile_bvideos);
            bAudios = root.findViewById(R.id.fragment_user_profile_baudios);
            bArticles = root.findViewById(R.id.fragment_user_profile_barticles);
            bProducts = root.findViewById(R.id.fragment_user_profile_bproducts);
            bGifts = root.findViewById(R.id.fragment_user_profile_bgifts);
            fabMessage = root.findViewById(R.id.header_user_profile_fab_message);
            fabMoreInfo = root.findViewById(R.id.info_btn);
            bPrimaryAction = root.findViewById(R.id.subscribe_btn);
            paganSymbol = root.findViewById(R.id.pagan_symbol);
            Runes = root.findViewById(R.id.runes_container);
            ivVerified = root.findViewById(R.id.item_verified);
            bDonate = root.findViewById(R.id.donated_anim);

            RecyclerView filtersList = root.findViewById(R.id.post_filter_recyclerview);
            filtersList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

            mPostFilterAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
            mPostFilterAdapter.setListener(entry -> callPresenter(p -> p.fireFilterClick(entry)));
            filtersList.setAdapter(mPostFilterAdapter);

            tvStatus.setOnClickListener(v -> callPresenter(UserWallPresenter::fireStatusClick));

            fabMoreInfo.setOnClickListener(v -> callPresenter(UserWallPresenter::fireMoreInfoClick));
            bPrimaryAction.setOnClickListener(v -> callPresenter(UserWallPresenter::firePrimaryActionsClick));
            fabMessage.setOnClickListener(v -> callPresenter(UserWallPresenter::fireChatClick));

            root.findViewById(R.id.horiz_scroll).setClipToOutline(true);
            root.findViewById(R.id.header_user_profile_photos_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderPhotosClick));
            root.findViewById(R.id.header_user_profile_friends_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderFriendsClick));
            root.findViewById(R.id.header_user_profile_audios_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderAudiosClick));
            root.findViewById(R.id.header_user_profile_articles_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderArticlesClick));
            root.findViewById(R.id.header_user_profile_products_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderProductsClick));
            root.findViewById(R.id.header_user_profile_groups_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderGroupsClick));
            root.findViewById(R.id.header_user_profile_videos_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderVideosClick));
            root.findViewById(R.id.header_user_profile_gifts_container).setOnClickListener(v -> callPresenter(UserWallPresenter::fireHeaderGiftsClick));
        }
    }
}
