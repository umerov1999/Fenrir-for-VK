package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.joinNonEmptyStrings;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Career;
import dev.ragnarok.fenrir.model.Icon;
import dev.ragnarok.fenrir.model.Military;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.School;
import dev.ragnarok.fenrir.model.Sex;
import dev.ragnarok.fenrir.model.Text;
import dev.ragnarok.fenrir.model.University;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserDetails;
import dev.ragnarok.fenrir.model.menu.AdvancedItem;
import dev.ragnarok.fenrir.model.menu.Section;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IUserDetailsView;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class UserDetailsPresenter extends AccountDependencyPresenter<IUserDetailsView> {

    private final User user;
    private final UserDetails details;
    private List<Photo> photos_profile;
    private int current_select;

    public UserDetailsPresenter(int accountId, @NonNull User user, @NonNull UserDetails details, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.user = user;
        this.details = details;

        appendDisposable(InteractorFactory.createPhotosInteractor().get(accountId, user.getOwnerId(), -6, 50, 0, true)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::DisplayUserProfileAlbum, RxUtils.ignore()));
    }

    private static void addPersonalInfo(List<AdvancedItem> items, @DrawableRes int icon, int key, Section section, @StringRes int title, String v) {
        if (nonEmpty(v)) {
            items.add(new AdvancedItem(key, new Text(title))
                    .setIcon(icon)
                    .setSection(section)
                    .setSubtitle(new Text(v)));
        }
    }

    private static Integer getPolitivalViewRes(int political) {
        switch (political) {
            case 1:
                return R.string.political_views_communist;
            case 2:
                return R.string.political_views_socialist;
            case 3:
                return R.string.political_views_moderate;
            case 4:
                return R.string.political_views_liberal;
            case 5:
                return R.string.political_views_conservative;
            case 6:
                return R.string.political_views_monarchist;
            case 7:
                return R.string.political_views_ultraconservative;
            case 8:
                return R.string.political_views_apathetic;
            case 9:
                return R.string.political_views_libertian;
            default:
                return null;
        }
    }

    private static Integer getPeopleMainRes(int peopleMain) {
        switch (peopleMain) {
            case 1:
                return R.string.important_in_others_intellect_and_creativity;
            case 2:
                return R.string.important_in_others_kindness_and_honesty;
            case 3:
                return R.string.important_in_others_health_and_beauty;
            case 4:
                return R.string.important_in_others_wealth_and_power;
            case 5:
                return R.string.important_in_others_courage_and_persistance;
            case 6:
                return R.string.important_in_others_humor_and_love_for_life;
            default:
                return null;
        }
    }

    private static Integer getLifeMainRes(int lifeMain) {
        switch (lifeMain) {
            case 1:
                return R.string.personal_priority_family_and_children;
            case 2:
                return R.string.personal_priority_career_and_money;
            case 3:
                return R.string.personal_priority_entertainment_and_leisure;
            case 4:
                return R.string.personal_priority_science_and_research;
            case 5:
                return R.string.personal_priority_improving_the_world;
            case 6:
                return R.string.personal_priority_personal_development;
            case 7:
                return R.string.personal_priority_beauty_and_art;
            case 8:
                return R.string.personal_priority_fame_and_influence;
            default:
                return null;
        }
    }

    private static Integer getAlcoholOrSmokingViewRes(int value) {
        switch (value) {
            case 1:
                return R.string.views_very_negative;
            case 2:
                return R.string.views_negative;
            case 3:
                return R.string.views_neutral;
            case 4:
                return R.string.views_compromisable;
            case 5:
                return R.string.views_positive;
            default:
                return null;
        }
    }

    public void fireChatClick() {
        int accountId = getAccountId();
        Peer peer = new Peer(Peer.fromUserId(user.getId()))
                .setAvaUrl(user.getMaxSquareAvatar())
                .setTitle(user.getFullName());

        callView(view -> view.openChatWith(accountId, accountId, peer));
    }

    public void firePhotoClick() {
        if (isEmpty(photos_profile) || current_select < 0 || current_select > photos_profile.size() - 1) {
            callView(view -> view.openPhotoUser(user));
            return;
        }
        callView(view -> view.openPhotoAlbum(getAccountId(), user.getOwnerId(), -6, new ArrayList<>(photos_profile), current_select));
    }

    private void DisplayUserProfileAlbum(List<Photo> photos) {
        if (photos.isEmpty()) {
            return;
        }
        Integer currentAvatarPhotoId = nonNull(details) && nonNull(details.getPhotoId()) ? details.getPhotoId().getId() : null;
        Integer currentAvatarOwner_id = nonNull(details) && nonNull(details.getPhotoId()) ? details.getPhotoId().getOwnerId() : null;
        int sel = 0;
        if (currentAvatarPhotoId != null && currentAvatarOwner_id != null) {
            int ut = 0;
            for (Photo i : photos) {
                if (i.getOwnerId() == currentAvatarOwner_id && i.getId() == currentAvatarPhotoId) {
                    sel = ut;
                    break;
                }
                ut++;
            }
        }

        current_select = sel;
        photos_profile = photos;
        int finalSel = sel;
        callView(view -> view.onPhotosLoaded(photos.get(finalSel)));
    }

    private List<AdvancedItem> createData() {
        List<AdvancedItem> items = new ArrayList<>();

        Section mainSection = new Section(new Text(R.string.mail_information));

        String domain = nonEmpty(user.getDomain()) ? "@" + user.getDomain() : "@" + user.getId();
        items.add(new AdvancedItem(1, AdvancedItem.TYPE_COPY_DETAILS_ONLY, new Text(R.string.id))
                .setSubtitle(new Text(domain))
                .setIcon(Icon.fromResources(R.drawable.person))
                .setSection(mainSection));

        if (nonEmpty(details.getBdate())) {
            String formatted = AppTextUtils.getDateWithZeros(details.getBdate());
            items.add(new AdvancedItem(1, new Text(R.string.birthday))
                    .setSubtitle(new Text(formatted))
                    .setIcon(Icon.fromResources(R.drawable.cake))
                    .setSection(mainSection));
        }

        if (nonNull(details.getCity())) {
            items.add(new AdvancedItem(2, new Text(R.string.city))
                    .setSubtitle(new Text(details.getCity().getTitle()))
                    .setIcon(Icon.fromResources(R.drawable.ic_city))
                    .setSection(mainSection));
        }

        if (nonNull(details.getCountry())) {
            items.add(new AdvancedItem(3, new Text(R.string.country))
                    .setSubtitle(new Text(details.getCountry().getTitle()))
                    .setIcon(Icon.fromResources(R.drawable.ic_country))
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getHometown())) {
            items.add(new AdvancedItem(4, new Text(R.string.hometown))
                    .setSubtitle(new Text(details.getHometown()))
                    .setIcon(Icon.fromResources(R.drawable.ic_city))
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getPhone())) {
            items.add(new AdvancedItem(5, new Text(R.string.mobile_phone_number))
                    .setSubtitle(new Text(details.getPhone()))
                    .setIcon(R.drawable.cellphone)
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getHomePhone())) {
            items.add(new AdvancedItem(6, new Text(R.string.home_phone_number))
                    .setSubtitle(new Text(details.getHomePhone()))
                    .setIcon(R.drawable.cellphone)
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getSkype())) {
            items.add(new AdvancedItem(7, AdvancedItem.TYPE_COPY_DETAILS_ONLY, new Text(R.string.skype))
                    .setSubtitle(new Text(details.getSkype()))
                    .setIcon(R.drawable.ic_skype)
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getInstagram())) {
            items.add(new AdvancedItem(8, AdvancedItem.TYPE_OPEN_URL, new Text(R.string.instagram))
                    .setSubtitle(new Text(details.getInstagram()))
                    .setUrlPrefix("https://www.instagram.com")
                    .setIcon(R.drawable.instagram)
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getTwitter())) {
            items.add(new AdvancedItem(9, AdvancedItem.TYPE_OPEN_URL, new Text(R.string.twitter))
                    .setSubtitle(new Text(details.getTwitter()))
                    .setIcon(R.drawable.twitter)
                    .setUrlPrefix("https://mobile.twitter.com")
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getFacebook())) {
            items.add(new AdvancedItem(10, AdvancedItem.TYPE_OPEN_URL, new Text(R.string.facebook))
                    .setSubtitle(new Text(details.getFacebook()))
                    .setIcon(R.drawable.facebook)
                    .setUrlPrefix("https://m.facebook.com")
                    .setSection(mainSection));
        }

        if (nonEmpty(user.getStatus())) {
            items.add(new AdvancedItem(11, new Text(R.string.status))
                    .setSubtitle(new Text(user.getStatus()))
                    .setIcon(R.drawable.ic_profile_status)
                    .setSection(mainSection));
        }

        if (nonNull(details.getLanguages()) && details.getLanguages().length > 0) {
            items.add(new AdvancedItem(15, new Text(R.string.languages))
                    .setIcon(R.drawable.ic_language)
                    .setSubtitle(new Text(Utils.join(details.getLanguages(), ", ", orig -> orig)))
                    .setSection(mainSection));
        }

        if (nonEmpty(details.getSite())) {
            items.add(new AdvancedItem(23, new Text(R.string.website))
                    .setIcon(R.drawable.ic_site)
                    .setSection(mainSection)
                    .setSubtitle(new Text(details.getSite())));
        }

        Section pesonal = new Section(new Text(R.string.personal_information));
        addPersonalInfo(items, R.drawable.star, 24, pesonal, R.string.interests, details.getInterests());
        addPersonalInfo(items, R.drawable.star, 26, pesonal, R.string.activities, details.getActivities());
        addPersonalInfo(items, R.drawable.music, 25, pesonal, R.string.favorite_music, details.getMusic());
        addPersonalInfo(items, R.drawable.movie, 27, pesonal, R.string.favorite_movies, details.getMovies());
        addPersonalInfo(items, R.drawable.ic_favorite_tv, 28, pesonal, R.string.favorite_tv_shows, details.getTv());
        addPersonalInfo(items, R.drawable.ic_favorite_quotes, 29, pesonal, R.string.favorite_quotes, details.getQuotes());
        addPersonalInfo(items, R.drawable.ic_favorite_game, 30, pesonal, R.string.favorite_games, details.getGames());
        addPersonalInfo(items, R.drawable.ic_about_me, 31, pesonal, R.string.about_me, details.getAbout());
        addPersonalInfo(items, R.drawable.book, 32, pesonal, R.string.favorite_books, details.getBooks());

        Section beliefs = new Section(new Text(R.string.beliefs));

        if (nonNull(getPolitivalViewRes(details.getPolitical()))) {
            items.add(new AdvancedItem(16, new Text(R.string.political_views))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(new Text(getPolitivalViewRes(details.getPolitical()))));
        }

        if (nonNull(getLifeMainRes(details.getLifeMain()))) {
            items.add(new AdvancedItem(17, new Text(R.string.personal_priority))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(new Text(getLifeMainRes(details.getLifeMain()))));
        }

        if (nonNull(getPeopleMainRes(details.getPeopleMain()))) {
            items.add(new AdvancedItem(18, new Text(R.string.important_in_others))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(new Text(getPeopleMainRes(details.getPeopleMain()))));
        }

        if (nonNull(getAlcoholOrSmokingViewRes(details.getSmoking()))) {
            items.add(new AdvancedItem(19, new Text(R.string.views_on_smoking))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(new Text(getAlcoholOrSmokingViewRes(details.getSmoking()))));
        }

        if (nonNull(getAlcoholOrSmokingViewRes(details.getAlcohol()))) {
            items.add(new AdvancedItem(20, new Text(R.string.views_on_alcohol))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(new Text(getAlcoholOrSmokingViewRes(details.getAlcohol()))));
        }

        if (nonEmpty(details.getInspiredBy())) {
            items.add(new AdvancedItem(21, new Text(R.string.inspired_by))
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSection(beliefs)
                    .setSubtitle(new Text(details.getInspiredBy())));
        }

        if (nonEmpty(details.getReligion())) {
            items.add(new AdvancedItem(22, new Text(R.string.world_view))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(new Text(details.getReligion())));
        }

        if (nonEmpty(details.getCareers())) {
            Section career = new Section(new Text(R.string.career));

            for (Career c : details.getCareers()) {
                Icon icon = isNull(c.getGroup()) ? Icon.fromResources(R.drawable.ic_career) : Icon.fromUrl(c.getGroup().get100photoOrSmaller());
                String term = c.getFrom() + " - " + (c.getUntil() == 0 ? getString(R.string.activity_until_now) : String.valueOf(c.getUntil()));
                String company = isNull(c.getGroup()) ? c.getCompany() : c.getGroup().getFullName();
                String title = isEmpty(c.getPosition()) ? company : c.getPosition() + ", " + company;

                items.add(new AdvancedItem(9, new Text(title))
                        .setSubtitle(new Text(term))
                        .setIcon(icon)
                        .setSection(career)
                        .setTag(c.getGroup()));
            }
        }

        if (nonEmpty(details.getMilitaries())) {
            Section section = new Section(new Text(R.string.military_service));

            for (Military m : details.getMilitaries()) {
                String term = m.getFrom() + " - " + (m.getUntil() == 0 ? getString(R.string.activity_until_now) : String.valueOf(m.getUntil()));
                items.add(new AdvancedItem(10, new Text(m.getUnit()))
                        .setSubtitle(new Text(term))
                        .setIcon(R.drawable.ic_military)
                        .setSection(section));
            }
        }

        if (nonEmpty(details.getUniversities()) || nonEmpty(details.getSchools())) {
            Section section = new Section(new Text(R.string.education));

            if (nonEmpty(details.getUniversities())) {
                for (University u : details.getUniversities()) {
                    String title = u.getName();
                    String subtitle = joinNonEmptyStrings("\n", u.getFacultyName(), u.getChairName(), u.getForm(), u.getStatus());
                    items.add(new AdvancedItem(11, new Text(title))
                            .setSection(section)
                            .setSubtitle(isEmpty(subtitle) ? null : new Text(subtitle))
                            .setIcon(R.drawable.ic_university));
                }
            }

            if (nonEmpty(details.getSchools())) {
                for (School s : details.getSchools()) {
                    String title = joinNonEmptyStrings(", ", s.getName(), s.getClazz());

                    Text term;

                    if (s.getFrom() > 0) {
                        term = new Text(s.getFrom() + " - " + (s.getTo() == 0
                                ? getString(R.string.activity_until_now) : String.valueOf(s.getTo())));
                    } else {
                        term = null;
                    }

                    items.add(new AdvancedItem(12, new Text(title))
                            .setSection(section)
                            .setSubtitle(term)
                            .setIcon(R.drawable.ic_school));
                }
            }
        }

        if (details.getRelation() > 0 || nonEmpty(details.getRelatives()) || nonNull(details.getRelationPartner())) {
            Section section = new Section(new Text(R.string.family));

            if (details.getRelation() > 0 || nonNull(details.getRelationPartner())) {
                Icon icon;
                Text subtitle;

                @StringRes
                int relationRes = getRelationStringByType(details.getRelation());

                if (nonNull(details.getRelationPartner())) {
                    icon = Icon.fromUrl(details.getRelationPartner().get100photoOrSmaller());
                    subtitle = new Text(getString(relationRes) + "\n" + details.getRelationPartner().getFullName());
                } else {
                    subtitle = new Text(relationRes);
                    icon = Icon.fromResources(R.drawable.ic_relation);
                }

                items.add(new AdvancedItem(13, new Text(R.string.relationship))
                        .setSection(section)
                        .setSubtitle(subtitle)
                        .setIcon(icon)
                        .setTag(details.getRelationPartner()));
            }

            if (nonNull(details.getRelatives())) {
                for (UserDetails.Relative r : details.getRelatives()) {
                    Icon icon = isNull(r.getUser()) ? Icon.fromResources(R.drawable.ic_relative_user) : Icon.fromUrl(r.getUser().get100photoOrSmaller());
                    String subtitle = isNull(r.getUser()) ? r.getName() : r.getUser().getFullName();
                    items.add(new AdvancedItem(14, new Text(getRelativeStringByType(r.getType())))
                            .setIcon(icon)
                            .setSubtitle(new Text(subtitle))
                            .setSection(section)
                            .setTag(r.getUser()));
                }
            }
        }

        return items;
    }

    @StringRes
    private Integer getRelationStringByType(int relation) {
        switch (user.getSex()) {
            case Sex.MAN:
            case Sex.UNKNOWN:
                switch (relation) {
                    case VKApiUser.Relation.SINGLE:
                        return R.string.relationship_man_single;
                    case VKApiUser.Relation.RELATIONSHIP:
                        return R.string.relationship_man_in_relationship;
                    case VKApiUser.Relation.ENGAGED:
                        return R.string.relationship_man_engaged;
                    case VKApiUser.Relation.MARRIED:
                        return R.string.relationship_man_married;
                    case VKApiUser.Relation.COMPLICATED:
                        return R.string.relationship_man_its_complicated;
                    case VKApiUser.Relation.SEARCHING:
                        return R.string.relationship_man_activelly_searching;
                    case VKApiUser.Relation.IN_LOVE:
                        return R.string.relationship_man_in_love;
                    case VKApiUser.Relation.IN_A_CIVIL_UNION:
                        return R.string.in_a_civil_union;

                }
                break;
            case Sex.WOMAN:
                switch (relation) {
                    case VKApiUser.Relation.SINGLE:
                        return R.string.relationship_woman_single;
                    case VKApiUser.Relation.RELATIONSHIP:
                        return R.string.relationship_woman_in_relationship;
                    case VKApiUser.Relation.ENGAGED:
                        return R.string.relationship_woman_engaged;
                    case VKApiUser.Relation.MARRIED:
                        return R.string.relationship_woman_married;
                    case VKApiUser.Relation.COMPLICATED:
                        return R.string.relationship_woman_its_complicated;
                    case VKApiUser.Relation.SEARCHING:
                        return R.string.relationship_woman_activelly_searching;
                    case VKApiUser.Relation.IN_LOVE:
                        return R.string.relationship_woman_in_love;
                    case VKApiUser.Relation.IN_A_CIVIL_UNION:
                        return R.string.in_a_civil_union;
                }
                break;
        }

        return R.string.relatives_others;
    }

    @StringRes
    private int getRelativeStringByType(String type) {
        if (type == null) {
            return R.string.relatives_others;
        }

        switch (type) {
            case VKApiUser.RelativeType.CHILD:
                return R.string.relatives_children;
            case VKApiUser.RelativeType.GRANDCHILD:
                return R.string.relatives_grandchildren;
            case VKApiUser.RelativeType.PARENT:
                return R.string.relatives_parents;
            case VKApiUser.RelativeType.SUBLING:
                return R.string.relatives_siblings;
            default:
                return R.string.relatives_others;
        }
    }

    @Override
    public void onGuiCreated(@NonNull IUserDetailsView view) {
        super.onGuiCreated(view);
        view.displayToolbarTitle(user);
        view.displayData(createData());
        if (!isEmpty(photos_profile) && current_select >= 0 && current_select < photos_profile.size() - 1) {
            view.onPhotosLoaded(photos_profile.get(current_select));
        }
    }

    public void fireItemClick(AdvancedItem item) {
        Object tag = item.getTag();

        if (tag instanceof Owner) {
            callView(v -> v.openOwnerProfile(getAccountId(), ((Owner) tag).getOwnerId(), (Owner) tag));
        }
    }
}