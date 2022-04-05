package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiUser
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.*
import dev.ragnarok.fenrir.model.menu.AdvancedItem
import dev.ragnarok.fenrir.model.menu.Section
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IUserDetailsView
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.util.AppTextUtils.getDateWithZeros
import dev.ragnarok.fenrir.util.RxUtils.ignore
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.Utils.join
import dev.ragnarok.fenrir.util.Utils.joinNonEmptyStrings

class UserDetailsPresenter(
    accountId: Int,
    private val user: User,
    details: UserDetails,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<IUserDetailsView>(accountId, savedInstanceState) {
    private var details: UserDetails = UserDetails()
    private var photos_profile: List<Photo> = ArrayList(1)
    private var current_select = 0
    fun fireChatClick() {
        val accountId = accountId
        val peer = Peer(
            Peer.fromUserId(
                user.id
            )
        )
            .setAvaUrl(user.maxSquareAvatar)
            .setTitle(user.fullName)
        view?.openChatWith(
            accountId,
            accountId,
            peer
        )
    }

    fun firePhotoClick() {
        if (photos_profile.isNullOrEmpty() || current_select < 0 || current_select > photos_profile.size - 1) {
            view?.openPhotoUser(
                user
            )
            return
        }
        view?.openPhotoAlbum(
            accountId,
            user.ownerId,
            -6,
            ArrayList(photos_profile),
            current_select
        )
    }

    private fun DisplayUserProfileAlbum(photos: List<Photo>) {
        if (photos.isEmpty()) {
            return
        }
        val currentAvatarPhotoId =
            if (details.photoId != null) details.photoId.getId() else null
        val currentAvatarOwner_id =
            if (details.photoId != null) details.photoId.getOwnerId() else null
        var sel = 0
        if (currentAvatarPhotoId != null && currentAvatarOwner_id != null) {
            var ut = 0
            for (i in photos) {
                if (i.ownerId == currentAvatarOwner_id && i.id == currentAvatarPhotoId) {
                    sel = ut
                    break
                }
                ut++
            }
        }
        current_select = sel
        photos_profile = photos
        val finalSel = sel
        view?.onPhotosLoaded(photos[finalSel])
    }

    private fun createData(): List<AdvancedItem> {
        val items: MutableList<AdvancedItem> = ArrayList()
        val mainSection = Section(Text(R.string.mail_information))
        val domain = if (user.domain.nonNullNoEmpty()) "@" + user.domain else "@" + user.id
        items.add(
            AdvancedItem(1, AdvancedItem.TYPE_COPY_DETAILS_ONLY, Text(R.string.id))
                .setSubtitle(Text(domain))
                .setIcon(Icon.fromResources(R.drawable.person))
                .setSection(mainSection)
        )
        if (details.bdate.nonNullNoEmpty()) {
            val formatted = getDateWithZeros(details.bdate)
            items.add(
                AdvancedItem(1, Text(R.string.birthday))
                    .setSubtitle(Text(formatted))
                    .setIcon(Icon.fromResources(R.drawable.cake))
                    .setSection(mainSection)
            )
        }
        if (details.city != null) {
            items.add(
                AdvancedItem(2, Text(R.string.city))
                    .setSubtitle(Text(details.city.title))
                    .setIcon(Icon.fromResources(R.drawable.ic_city))
                    .setSection(mainSection)
            )
        }
        if (details.country != null) {
            items.add(
                AdvancedItem(3, Text(R.string.country))
                    .setSubtitle(Text(details.country.title))
                    .setIcon(Icon.fromResources(R.drawable.ic_country))
                    .setSection(mainSection)
            )
        }
        if (details.hometown.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(4, Text(R.string.hometown))
                    .setSubtitle(Text(details.hometown))
                    .setIcon(Icon.fromResources(R.drawable.ic_city))
                    .setSection(mainSection)
            )
        }
        if (details.phone.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(5, Text(R.string.mobile_phone_number))
                    .setSubtitle(Text(details.phone))
                    .setIcon(R.drawable.cellphone)
                    .setSection(mainSection)
            )
        }
        if (details.homePhone.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(6, Text(R.string.home_phone_number))
                    .setSubtitle(Text(details.homePhone))
                    .setIcon(R.drawable.cellphone)
                    .setSection(mainSection)
            )
        }
        if (details.skype.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(7, AdvancedItem.TYPE_COPY_DETAILS_ONLY, Text(R.string.skype))
                    .setSubtitle(Text(details.skype))
                    .setIcon(R.drawable.ic_skype)
                    .setSection(mainSection)
            )
        }
        if (details.instagram.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(8, AdvancedItem.TYPE_OPEN_URL, Text(R.string.instagram))
                    .setSubtitle(Text(details.instagram))
                    .setUrlPrefix("https://www.instagram.com")
                    .setIcon(R.drawable.instagram)
                    .setSection(mainSection)
            )
        }
        if (details.twitter.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(9, AdvancedItem.TYPE_OPEN_URL, Text(R.string.twitter))
                    .setSubtitle(Text(details.twitter))
                    .setIcon(R.drawable.twitter)
                    .setUrlPrefix("https://mobile.twitter.com")
                    .setSection(mainSection)
            )
        }
        if (details.facebook.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(10, AdvancedItem.TYPE_OPEN_URL, Text(R.string.facebook))
                    .setSubtitle(Text(details.facebook))
                    .setIcon(R.drawable.facebook)
                    .setUrlPrefix("https://m.facebook.com")
                    .setSection(mainSection)
            )
        }
        if (user.status.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(11, Text(R.string.status))
                    .setSubtitle(Text(user.status))
                    .setIcon(R.drawable.ic_profile_status)
                    .setSection(mainSection)
            )
        }
        if (details.languages != null && details.languages.isNotEmpty()) {
            items.add(
                AdvancedItem(15, Text(R.string.languages))
                    .setIcon(R.drawable.ic_language)
                    .setSubtitle(
                        Text(
                            join(
                                details.languages,
                                ", ",
                                object : Utils.SimpleFunction<String, String?> {
                                    override fun apply(orig: String): String {
                                        return orig
                                    }
                                })
                        )
                    )
                    .setSection(mainSection)
            )
        }
        if (details.site.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(23, Text(R.string.website))
                    .setIcon(R.drawable.ic_site)
                    .setSection(mainSection)
                    .setSubtitle(Text(details.site))
            )
        }
        val pesonal = Section(Text(R.string.personal_information))
        addPersonalInfo(items, R.drawable.star, 24, pesonal, R.string.interests, details.interests)
        addPersonalInfo(
            items,
            R.drawable.star,
            26,
            pesonal,
            R.string.activities,
            details.activities
        )
        addPersonalInfo(
            items,
            R.drawable.music,
            25,
            pesonal,
            R.string.favorite_music,
            details.music
        )
        addPersonalInfo(
            items,
            R.drawable.movie,
            27,
            pesonal,
            R.string.favorite_movies,
            details.movies
        )
        addPersonalInfo(
            items,
            R.drawable.ic_favorite_tv,
            28,
            pesonal,
            R.string.favorite_tv_shows,
            details.tv
        )
        addPersonalInfo(
            items,
            R.drawable.ic_favorite_quotes,
            29,
            pesonal,
            R.string.favorite_quotes,
            details.quotes
        )
        addPersonalInfo(
            items,
            R.drawable.ic_favorite_game,
            30,
            pesonal,
            R.string.favorite_games,
            details.games
        )
        addPersonalInfo(
            items,
            R.drawable.ic_about_me,
            31,
            pesonal,
            R.string.about_me,
            details.about
        )
        addPersonalInfo(items, R.drawable.book, 32, pesonal, R.string.favorite_books, details.books)
        val beliefs = Section(Text(R.string.beliefs))
        if (getPolitivalViewRes(details.political) != null) {
            items.add(
                AdvancedItem(16, Text(R.string.political_views))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(
                        Text(
                            getPolitivalViewRes(
                                details.political
                            )
                        )
                    )
            )
        }
        if (getLifeMainRes(details.lifeMain) != null) {
            items.add(
                AdvancedItem(17, Text(R.string.personal_priority))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(
                        Text(
                            getLifeMainRes(
                                details.lifeMain
                            )
                        )
                    )
            )
        }
        if (getPeopleMainRes(details.peopleMain) != null) {
            items.add(
                AdvancedItem(18, Text(R.string.important_in_others))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(
                        Text(
                            getPeopleMainRes(
                                details.peopleMain
                            )
                        )
                    )
            )
        }
        if (getAlcoholOrSmokingViewRes(details.smoking) != null) {
            items.add(
                AdvancedItem(19, Text(R.string.views_on_smoking))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(
                        Text(
                            getAlcoholOrSmokingViewRes(
                                details.smoking
                            )
                        )
                    )
            )
        }
        if (getAlcoholOrSmokingViewRes(details.alcohol) != null) {
            items.add(
                AdvancedItem(20, Text(R.string.views_on_alcohol))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(
                        Text(
                            getAlcoholOrSmokingViewRes(
                                details.alcohol
                            )
                        )
                    )
            )
        }
        if (details.inspiredBy.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(21, Text(R.string.inspired_by))
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSection(beliefs)
                    .setSubtitle(Text(details.inspiredBy))
            )
        }
        if (details.religion.nonNullNoEmpty()) {
            items.add(
                AdvancedItem(22, Text(R.string.world_view))
                    .setSection(beliefs)
                    .setIcon(R.drawable.ic_profile_personal)
                    .setSubtitle(Text(details.religion))
            )
        }
        if (details.careers.nonNullNoEmpty()) {
            val career = Section(Text(R.string.career))
            for (c in details.careers) {
                val icon =
                    if (c.group == null) Icon.fromResources(R.drawable.ic_career) else Icon.fromUrl(
                        c.group.get100photoOrSmaller()
                    )
                val term =
                    c.from.toString() + " - " + if (c.until == 0) getString(R.string.activity_until_now) else c.until.toString()
                val company = if (c.group == null) c.company else c.group.fullName
                val title = if (c.position.isNullOrEmpty()) company else c.position + ", " + company
                items.add(
                    AdvancedItem(9, Text(title))
                        .setSubtitle(Text(term))
                        .setIcon(icon)
                        .setSection(career)
                        .setTag(c.group)
                )
            }
        }
        if (details.militaries.nonNullNoEmpty()) {
            val section = Section(Text(R.string.military_service))
            for (m in details.militaries) {
                val term =
                    m.from.toString() + " - " + if (m.until == 0) getString(R.string.activity_until_now) else m.until.toString()
                items.add(
                    AdvancedItem(10, Text(m.unit))
                        .setSubtitle(Text(term))
                        .setIcon(R.drawable.ic_military)
                        .setSection(section)
                )
            }
        }
        if (details.universities.nonNullNoEmpty() || details.schools.nonNullNoEmpty()) {
            val section = Section(Text(R.string.education))
            if (details.universities.nonNullNoEmpty()) {
                for (u in details.universities) {
                    val title = u.name
                    val subtitle =
                        joinNonEmptyStrings("\n", u.facultyName, u.chairName, u.form, u.status)
                    items.add(
                        AdvancedItem(11, Text(title))
                            .setSection(section)
                            .setSubtitle(if (subtitle.isNullOrEmpty()) null else Text(subtitle))
                            .setIcon(R.drawable.ic_university)
                    )
                }
            }
            if (details.schools.nonNullNoEmpty()) {
                for (s in details.schools) {
                    val title = joinNonEmptyStrings(", ", s.name, s.clazz)
                    val term: Text? = if (s.from > 0) {
                        Text(s.from.toString() + " - " + if (s.to == 0) getString(R.string.activity_until_now) else s.to.toString())
                    } else {
                        null
                    }
                    items.add(
                        AdvancedItem(12, Text(title))
                            .setSection(section)
                            .setSubtitle(term)
                            .setIcon(R.drawable.ic_school)
                    )
                }
            }
        }
        if (details.relation > 0 || details.relatives.nonNullNoEmpty() || details.relationPartner != null) {
            val section = Section(Text(R.string.family))
            if (details.relation > 0 || details.relationPartner != null) {
                val icon: Icon
                val subtitle: Text
                @StringRes val relationRes = getRelationStringByType(
                    details.relation
                )
                if (details.relationPartner != null) {
                    icon = Icon.fromUrl(details.relationPartner.get100photoOrSmaller())
                    subtitle = Text(
                        """
    ${getString(relationRes)}
    ${details.relationPartner.fullName}
    """.trimIndent()
                    )
                } else {
                    subtitle = Text(relationRes)
                    icon = Icon.fromResources(R.drawable.ic_relation)
                }
                items.add(
                    AdvancedItem(13, Text(R.string.relationship))
                        .setSection(section)
                        .setSubtitle(subtitle)
                        .setIcon(icon)
                        .setTag(details.relationPartner)
                )
            }
            if (details.relatives != null) {
                for (r in details.relatives) {
                    val icon =
                        if (r.user == null) Icon.fromResources(R.drawable.ic_relative_user) else Icon.fromUrl(
                            r.user.get100photoOrSmaller()
                        )
                    val subtitle = if (r.user == null) r.name else r.user.fullName
                    items.add(
                        AdvancedItem(14, Text(getRelativeStringByType(r.type)))
                            .setIcon(icon)
                            .setSubtitle(Text(subtitle))
                            .setSection(section)
                            .setTag(r.user)
                    )
                }
            }
        }
        return items
    }

    @StringRes
    private fun getRelationStringByType(relation: Int): Int {
        when (user.sex) {
            Sex.MAN, Sex.UNKNOWN -> when (relation) {
                VKApiUser.Relation.SINGLE -> return R.string.relationship_man_single
                VKApiUser.Relation.RELATIONSHIP -> return R.string.relationship_man_in_relationship
                VKApiUser.Relation.ENGAGED -> return R.string.relationship_man_engaged
                VKApiUser.Relation.MARRIED -> return R.string.relationship_man_married
                VKApiUser.Relation.COMPLICATED -> return R.string.relationship_man_its_complicated
                VKApiUser.Relation.SEARCHING -> return R.string.relationship_man_activelly_searching
                VKApiUser.Relation.IN_LOVE -> return R.string.relationship_man_in_love
                VKApiUser.Relation.IN_A_CIVIL_UNION -> return R.string.in_a_civil_union
            }
            Sex.WOMAN -> when (relation) {
                VKApiUser.Relation.SINGLE -> return R.string.relationship_woman_single
                VKApiUser.Relation.RELATIONSHIP -> return R.string.relationship_woman_in_relationship
                VKApiUser.Relation.ENGAGED -> return R.string.relationship_woman_engaged
                VKApiUser.Relation.MARRIED -> return R.string.relationship_woman_married
                VKApiUser.Relation.COMPLICATED -> return R.string.relationship_woman_its_complicated
                VKApiUser.Relation.SEARCHING -> return R.string.relationship_woman_activelly_searching
                VKApiUser.Relation.IN_LOVE -> return R.string.relationship_woman_in_love
                VKApiUser.Relation.IN_A_CIVIL_UNION -> return R.string.in_a_civil_union
            }
        }
        return R.string.relatives_others
    }

    @StringRes
    private fun getRelativeStringByType(type: String?): Int {
        return if (type == null) {
            R.string.relatives_others
        } else when (type) {
            VKApiUser.RelativeType.CHILD -> R.string.relatives_children
            VKApiUser.RelativeType.GRANDCHILD -> R.string.relatives_grandchildren
            VKApiUser.RelativeType.PARENT -> R.string.relatives_parents
            VKApiUser.RelativeType.SUBLING -> R.string.relatives_siblings
            else -> R.string.relatives_others
        }
    }

    override fun onGuiCreated(viewHost: IUserDetailsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayToolbarTitle(user)
        viewHost.displayData(createData())
        if (!photos_profile.isNullOrEmpty() && current_select >= 0 && current_select < photos_profile.size - 1) {
            viewHost.onPhotosLoaded(photos_profile[current_select])
        }
    }

    fun fireItemClick(item: AdvancedItem) {
        val tag = item.tag
        if (tag is Owner) {
            view?.openOwnerProfile(
                accountId,
                tag.ownerId,
                tag
            )
        }
    }

    companion object {
        private fun addPersonalInfo(
            items: MutableList<AdvancedItem>,
            @DrawableRes icon: Int,
            key: Int,
            section: Section,
            @StringRes title: Int,
            v: String?
        ) {
            if (v.nonNullNoEmpty()) {
                items.add(
                    AdvancedItem(key, Text(title))
                        .setIcon(icon)
                        .setSection(section)
                        .setSubtitle(Text(v))
                )
            }
        }

        private fun getPolitivalViewRes(political: Int): Int? {
            return when (political) {
                1 -> R.string.political_views_communist
                2 -> R.string.political_views_socialist
                3 -> R.string.political_views_moderate
                4 -> R.string.political_views_liberal
                5 -> R.string.political_views_conservative
                6 -> R.string.political_views_monarchist
                7 -> R.string.political_views_ultraconservative
                8 -> R.string.political_views_apathetic
                9 -> R.string.political_views_libertian
                else -> null
            }
        }

        private fun getPeopleMainRes(peopleMain: Int): Int? {
            return when (peopleMain) {
                1 -> R.string.important_in_others_intellect_and_creativity
                2 -> R.string.important_in_others_kindness_and_honesty
                3 -> R.string.important_in_others_health_and_beauty
                4 -> R.string.important_in_others_wealth_and_power
                5 -> R.string.important_in_others_courage_and_persistance
                6 -> R.string.important_in_others_humor_and_love_for_life
                else -> null
            }
        }

        private fun getLifeMainRes(lifeMain: Int): Int? {
            return when (lifeMain) {
                1 -> R.string.personal_priority_family_and_children
                2 -> R.string.personal_priority_career_and_money
                3 -> R.string.personal_priority_entertainment_and_leisure
                4 -> R.string.personal_priority_science_and_research
                5 -> R.string.personal_priority_improving_the_world
                6 -> R.string.personal_priority_personal_development
                7 -> R.string.personal_priority_beauty_and_art
                8 -> R.string.personal_priority_fame_and_influence
                else -> null
            }
        }

        private fun getAlcoholOrSmokingViewRes(value: Int): Int? {
            return when (value) {
                1 -> R.string.views_very_negative
                2 -> R.string.views_negative
                3 -> R.string.views_neutral
                4 -> R.string.views_compromisable
                5 -> R.string.views_positive
                else -> null
            }
        }
    }

    init {
        this.details = details
        appendDisposable(
            InteractorFactory.createPhotosInteractor()[accountId, user.ownerId, -6, 50, 0, true]
                .fromIOToMain()
                .subscribe({ DisplayUserProfileAlbum(it) }, ignore())
        )
    }
}