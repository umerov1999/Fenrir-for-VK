package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.LoginActivity;
import dev.ragnarok.fenrir.adapter.horizontal.HorizontalOptionsAdapter;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.PeopleSearchCriteria;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.CommunityDetails;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ParcelableOwnerWrapper;
import dev.ragnarok.fenrir.model.PostFilter;
import dev.ragnarok.fenrir.model.Token;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.DocsListPresenter;
import dev.ragnarok.fenrir.mvp.presenter.GroupWallPresenter;
import dev.ragnarok.fenrir.mvp.view.IGroupWallView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.BlurTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class GroupWallFragment extends AbsWallFragment<IGroupWallView, GroupWallPresenter> implements IGroupWallView {

    private final ActivityResultLauncher<Intent> requestCommunity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    ArrayList<Token> tokens = LoginActivity.extractGroupTokens(result.getData());
                    callPresenter(p -> p.fireGroupTokensReceived(tokens));
                }
            });
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(p -> p.fireShowQR(requireActivity())));
    private final OwnerLinkSpanFactory.ActionListener ownerLinkAdapter = new LinkActionAdapter() {
        @Override
        public void onOwnerClick(int ownerId) {
            callPresenter(p -> p.fireOwnerClick(ownerId));
        }
    };
    private GroupHeaderHolder mHeaderHolder;

    @Override
    public void displayBaseCommunityData(Community community, CommunityDetails details) {
        if (isNull(mHeaderHolder)) return;

        mHeaderHolder.tvName.setText(community.getFullName());

        if (details.getCover() != null && !Utils.isEmpty(details.getCover().getImages())) {
            int def = 0;
            String url = null;
            for (CommunityDetails.CoverImage i : details.getCover().getImages()) {
                if (i.getWidth() * i.getHeight() > def) {
                    def = i.getWidth() * i.getHeight();
                    url = i.getUrl();
                }
            }
            displayCommunityCover(url);
        } else {
            displayCommunityCover(community.getMaxSquareAvatar());
        }

        String statusText;
        if (nonNull(details.getStatusAudio())) {
            statusText = details.getStatusAudio().getArtistAndTitle();
        } else {
            statusText = details.getStatus();
        }

        mHeaderHolder.tvStatus.setText(statusText);
        mHeaderHolder.tvAudioStatus.setVisibility(nonNull(details.getStatusAudio()) ? View.VISIBLE : View.GONE);

        String screenName = nonEmpty(community.getScreenName()) ? "@" + community.getScreenName() : null;
        mHeaderHolder.tvScreenName.setText(screenName);
        mHeaderHolder.tvName.setTextColor(Utils.getVerifiedColor(requireActivity(), community.isVerified()));
        mHeaderHolder.tvScreenName.setTextColor(Utils.getVerifiedColor(requireActivity(), community.isVerified()));

        int donate_anim = Settings.get().other().getDonate_anim_set();
        if (donate_anim > 0 && community.isDonated()) {
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
        mHeaderHolder.ivVerified.setVisibility(community.isVerified() ? View.VISIBLE : View.GONE);

        if (!details.isCanMessage())
            mHeaderHolder.fabMessage.setImageResource(R.drawable.close);
        else
            mHeaderHolder.fabMessage.setImageResource(R.drawable.email);

        String photoUrl = community.getMaxSquareAvatar();
        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl).transform(CurrentTheme.createTransformationForAvatar())
                    .tag(Constants.PICASSO_TAG)
                    .into(mHeaderHolder.ivAvatar);
        }
        mHeaderHolder.ivAvatar.setOnClickListener(v -> {
            Community cmt = callPresenter(GroupWallPresenter::getCommunity, null);
            if (cmt == null) {
                return;
            }
            PlaceFactory.getSingleURLPhotoPlace(cmt.getOriginalAvatar(), cmt.getFullName(), "club" + Math.abs(cmt.getId())).tryOpenWith(requireActivity());
        });
        mHeaderHolder.ivAvatar.setOnLongClickListener(v -> {
            callPresenter(GroupWallPresenter::fireMentions);
            return true;
        });
    }

    private void displayCommunityCover(String resource) {
        if (!Settings.get().other().isShow_wall_cover())
            return;
        if (!Utils.isEmpty(resource)) {
            PicassoInstance.with()
                    .load(resource)
                    .transform(new BlurTransformation(6f, requireActivity()))
                    .into(mHeaderHolder.vgCover);
        }
    }

    @Override
    public void InvalidateOptionsMenu() {
        requireActivity().invalidateOptionsMenu();
    }

    @NonNull
    @Override
    public IPresenterFactory<GroupWallPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            ParcelableOwnerWrapper wrapper = requireArguments().getParcelable(Extra.OWNER);
            AssertUtils.requireNonNull(wrapper);

            return new GroupWallPresenter(accountId, ownerId, (Community) wrapper.get(), requireActivity(), saveInstanceState);
        };
    }

    @Override
    protected int headerLayout() {
        return R.layout.header_group;
    }

    @Override
    protected void onHeaderInflated(View headerRootView) {
        mHeaderHolder = new GroupHeaderHolder(headerRootView);
        setupPaganContent(mHeaderHolder.Runes, mHeaderHolder.paganSymbol);
    }

    @Override
    public void setupPrimaryButton(@StringRes Integer title) {
        if (nonNull(mHeaderHolder)) {
            if (nonNull(title)) {
                mHeaderHolder.primaryActionButton.setText(title);
                mHeaderHolder.primaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.primaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setupSecondaryButton(@StringRes Integer title) {
        if (nonNull(mHeaderHolder)) {
            if (nonNull(title)) {
                mHeaderHolder.secondaryActionButton.setText(title);
                mHeaderHolder.secondaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.secondaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void openTopics(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getTopicsPlace(accoundId, ownerId)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openCommunityMembers(int accoundId, int groupId) {
        PeopleSearchCriteria criteria = new PeopleSearchCriteria("")
                .setGroupId(groupId);

        PlaceFactory.getSingleTabSearchPlace(accoundId, SearchContentType.PEOPLE, criteria).tryOpenWith(requireActivity());
    }

    @Override
    public void openDocuments(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getDocumentsPlace(accoundId, ownerId, DocsListPresenter.ACTION_SHOW)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void displayWallFilters(List<PostFilter> filters) {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mFiltersAdapter.setItems(filters);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_community_wall, menu);
        OptionMenuView optionMenuView = new OptionMenuView();
        callPresenter(p -> p.fireOptionMenuViewCreated(optionMenuView));

        menu.add(R.string.mutual_friends).setOnMenuItemClickListener(item -> {
            callPresenter(GroupWallPresenter::fireMutualFriends);
            return true;
        });

        if (!optionMenuView.isSubscribed) {
            menu.add(R.string.notify_wall_added).setOnMenuItemClickListener(item -> {
                callPresenter(GroupWallPresenter::fireSubscribe);
                return true;
            });
        } else {
            menu.add(R.string.unnotify_wall_added).setOnMenuItemClickListener(item -> {
                callPresenter(GroupWallPresenter::fireUnSubscribe);
                return true;
            });
        }

        if (!optionMenuView.isFavorite) {
            menu.add(R.string.add_to_bookmarks).setOnMenuItemClickListener(item -> {
                callPresenter(GroupWallPresenter::fireAddToBookmarksClick);
                return true;
            });
        } else {
            menu.add(R.string.remove_from_bookmarks).setOnMenuItemClickListener(item -> {
                callPresenter(GroupWallPresenter::fireRemoveFromBookmarks);
                return true;
            });
        }
        menu.add(R.string.mentions).setOnMenuItemClickListener(item -> {
            callPresenter(GroupWallPresenter::fireMentions);
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_community_control) {
            callPresenter(GroupWallPresenter::fireCommunityControlClick);
            return true;
        }

        if (item.getItemId() == R.id.action_community_messages) {
            callPresenter(GroupWallPresenter::fireCommunityMessagesClick);
            return true;
        }

        if (item.getItemId() == R.id.action_show_qr) {
            if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
                requestWritePermission.launch();
            } else {
                callPresenter(p -> p.fireShowQR(requireActivity()));
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyWallFiltersChanged() {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mFiltersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.community);
        ActivityUtils.setToolbarSubtitle(this, null);
    }

    @Override
    public void goToCommunityControl(int accountId, Community community, GroupSettings settings) {
        PlaceFactory.getCommunityControlPlace(accountId, community, settings).tryOpenWith(requireActivity());
    }

    @Override
    public void goToShowCommunityInfo(int accountId, Community community) {
        PlaceFactory.getShowComunityInfoPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void goToShowCommunityLinksInfo(int accountId, Community community) {
        PlaceFactory.getShowComunityLinksInfoPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void goToShowCommunityAboutInfo(int accountId, CommunityDetails details) {
        if (Utils.isEmpty(details.getDescription())) {
            return;
        }
        View root = View.inflate(requireActivity(), R.layout.dialog_selectable_text, null);
        MaterialTextView tvText = root.findViewById(R.id.selectable_text);
        if (nonNull(tvText)) {
            Spannable subtitle = OwnerLinkSpanFactory.withSpans(details.getDescription(), true, false, ownerLinkAdapter);

            tvText.setText(subtitle, TextView.BufferType.SPANNABLE);
            tvText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.description_hint)
                .setView(root)
                .setPositiveButton(R.string.button_ok, null)
                .show();
    }

    @Override
    public void goToGroupChats(int accountId, Community community) {
        PlaceFactory.getGroupChatsPlace(accountId, Math.abs(community.getId())).tryOpenWith(requireActivity());
    }

    @Override
    public void goToMutualFriends(int accountId, Community community) {
        CommunityFriendsFragment.newInstance(accountId, community.getId()).show(getChildFragmentManager(), "community_friends");
    }

    @Override
    public void startLoginCommunityActivity(int groupId) {
        Intent intent = LoginActivity.createIntent(requireActivity(), "2685278", "messages,photos,docs,manage", Collections.singletonList(groupId));
        requestCommunity.launch(intent);
    }

    @Override
    public void openCommunityDialogs(int accountId, int groupId, String subtitle) {
        PlaceFactory.getDialogsPlace(accountId, -groupId, subtitle).tryOpenWith(requireActivity());
    }

    @Override
    public void displayCounters(int members, int topics, int docs, int photos, int audio, int video, int articles, int products, int chats) {
        if (isNull(mHeaderHolder)) return;
        setupCounter(mHeaderHolder.bTopics, topics);
        setupCounter(mHeaderHolder.bMembers, members);
        setupCounter(mHeaderHolder.bDocuments, docs);
        setupCounter(mHeaderHolder.bPhotos, photos);
        setupCounter(mHeaderHolder.bAudios, audio);
        setupCounter(mHeaderHolder.bVideos, video);
        setupCounter(mHeaderHolder.bArticles, articles);
        setupCounter(mHeaderHolder.bProducts, products);
        setupCounterFlow(mHeaderHolder.bChats, mHeaderHolder.bChatsContainer, chats);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionMenuView optionMenuView = new OptionMenuView();
        callPresenter(p -> p.fireOptionMenuViewCreated(optionMenuView));
        menu.findItem(R.id.action_community_control).setVisible(optionMenuView.controlVisible);
    }

    @Override
    public void openProducts(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getMarketAlbumPlace(accountId, ownerId).tryOpenWith(requireActivity());
    }

    private static final class OptionMenuView implements IOptionMenuView {

        boolean controlVisible;

        boolean isFavorite;

        boolean isSubscribed;

        @Override
        public void setControlVisible(boolean visible) {
            controlVisible = visible;
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

    private class GroupHeaderHolder {
        final ImageView vgCover;
        final ImageView ivAvatar;
        final ImageView ivVerified;
        final RLottieImageView bDonate;
        final TextView tvName;
        final TextView tvStatus;
        final ImageView tvAudioStatus;
        final TextView tvScreenName;

        final TextView bTopics;
        final TextView bArticles;
        final TextView bChats;
        final ViewGroup bChatsContainer;
        final TextView bProducts;
        final TextView bMembers;
        final TextView bDocuments;
        final TextView bPhotos;
        final TextView bAudios;
        final TextView bVideos;
        final MaterialButton primaryActionButton;
        final MaterialButton secondaryActionButton;

        final FloatingActionButton fabMessage;
        final HorizontalOptionsAdapter<PostFilter> mFiltersAdapter;

        final RLottieImageView paganSymbol;
        final View Runes;

        GroupHeaderHolder(@NonNull View root) {
            vgCover = root.findViewById(R.id.cover);
            ivAvatar = root.findViewById(R.id.header_group_avatar);
            tvName = root.findViewById(R.id.header_group_name);
            tvStatus = root.findViewById(R.id.header_group_status);
            tvAudioStatus = root.findViewById(R.id.fragment_group_audio);
            tvScreenName = root.findViewById(R.id.header_group_id);
            bTopics = root.findViewById(R.id.header_group_btopics);
            bMembers = root.findViewById(R.id.header_group_bmembers);
            bDocuments = root.findViewById(R.id.header_group_bdocuments);
            bPhotos = root.findViewById(R.id.header_group_bphotos);
            bAudios = root.findViewById(R.id.header_group_baudios);
            bVideos = root.findViewById(R.id.header_group_bvideos);
            bArticles = root.findViewById(R.id.header_group_barticles);
            bChats = root.findViewById(R.id.header_group_bchats);
            bChatsContainer = root.findViewById(R.id.header_group_chats_container);
            bProducts = root.findViewById(R.id.header_group_bproducts);
            primaryActionButton = root.findViewById(R.id.header_group_primary_button);
            secondaryActionButton = root.findViewById(R.id.header_group_secondary_button);
            fabMessage = root.findViewById(R.id.header_group_fab_message);

            paganSymbol = root.findViewById(R.id.pagan_symbol);
            Runes = root.findViewById(R.id.runes_container);
            ivVerified = root.findViewById(R.id.item_verified);
            bDonate = root.findViewById(R.id.donated_anim);

            RecyclerView filterList = root.findViewById(R.id.post_filter_recyclerview);
            filterList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
            mFiltersAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
            mFiltersAdapter.setListener(entry -> callPresenter(p -> p.fireFilterEntryClick(entry)));

            filterList.setAdapter(mFiltersAdapter);

            tvStatus.setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderStatusClick));
            fabMessage.setOnClickListener(v -> callPresenter(GroupWallPresenter::fireChatClick));
            secondaryActionButton.setOnClickListener(v -> callPresenter(GroupWallPresenter::fireSecondaryButtonClick));
            primaryActionButton.setOnClickListener(v -> callPresenter(GroupWallPresenter::firePrimaryButtonClick));

            root.findViewById(R.id.header_group_photos_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderPhotosClick));
            root.findViewById(R.id.header_group_videos_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderVideosClick));
            root.findViewById(R.id.header_group_members_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderMembersClick));
            root.findViewById(R.id.horiz_scroll)
                    .setClipToOutline(true);
            root.findViewById(R.id.header_group_topics_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderTopicsClick));
            root.findViewById(R.id.header_group_documents_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderDocsClick));
            root.findViewById(R.id.header_group_audios_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderAudiosClick));
            root.findViewById(R.id.header_group_articles_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderArticlesClick));
            root.findViewById(R.id.header_group_products_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireHeaderProductsClick));
            root.findViewById(R.id.header_group_contacts_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireShowCommunityInfoClick));
            root.findViewById(R.id.header_group_links_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireShowCommunityLinksInfoClick));
            root.findViewById(R.id.header_group_about_container)
                    .setOnClickListener(v -> callPresenter(GroupWallPresenter::fireShowCommunityAboutInfoClick));
            bChatsContainer.setOnClickListener(v -> callPresenter(GroupWallPresenter::fireGroupChatsClick));
        }
    }
}
