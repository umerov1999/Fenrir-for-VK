package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.List;

import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.CommunityDetails;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.PostFilter;


public interface IGroupWallView extends IWallView {

    void displayBaseCommunityData(Community community, CommunityDetails details);

    void setupPrimaryButton(@StringRes Integer title);

    void setupSecondaryButton(@StringRes Integer title);

    void openTopics(int accountId, int ownerId, @Nullable Owner owner);

    void openCommunityMembers(int accountId, int groupId);

    void openDocuments(int accountId, int ownerId, @Nullable Owner owner);

    void openProducts(int accountId, int ownerId, @Nullable Owner owner);

    void displayWallFilters(List<PostFilter> filters);

    void notifyWallFiltersChanged();

    void goToCommunityControl(int accountId, Community community, GroupSettings settings);

    void goToShowCommunityInfo(int accountId, Community community);

    void goToShowCommunityLinksInfo(int accountId, Community community);

    void goToShowCommunityAboutInfo(int accountId, CommunityDetails details);

    void goToGroupChats(int accountId, Community community);

    void goToMutualFriends(int accountId, Community community);

    void startLoginCommunityActivity(int groupId);

    void openCommunityDialogs(int accountId, int groupId, String subtitle);

    void displayCounters(int members, int topics, int docs, int photos, int audio, int video, int articles, int products, int chats);

    void InvalidateOptionsMenu();

    interface IOptionMenuView {
        void setControlVisible(boolean visible);

        void setIsSubscribed(boolean subscribed);

        void setIsFavorite(boolean favorite);
    }
}