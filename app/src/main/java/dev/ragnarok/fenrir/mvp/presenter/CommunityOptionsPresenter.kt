package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.model.GroupSettings
import dev.ragnarok.fenrir.model.IdOption
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.ICommunityOptionsView
import dev.ragnarok.fenrir.nonNullNoEmpty
import java.util.*

class CommunityOptionsPresenter(
    accountId: Int,
    private val community: VKApiCommunity,
    private val settings: GroupSettings,
    savedInstanceState: Bundle?
) : AccountDependencyPresenter<ICommunityOptionsView>(accountId, savedInstanceState) {
    override fun onGuiCreated(viewHost: ICommunityOptionsView) {
        super.onGuiCreated(viewHost)
        viewHost.displayName(settings.title)
        viewHost.displayDescription(settings.description)
        viewHost.setCommunityTypeVisible(community.type == VKApiCommunity.Type.GROUP)
        viewHost.displayAddress(settings.address)
        viewHost.displayWebsite(settings.website)
        viewHost.setFeedbackCommentsRootVisible(community.type == VKApiCommunity.Type.PAGE)
        viewHost.setFeedbackCommentsChecked(settings.isFeedbackCommentsEnabled)
        viewHost.setObsceneFilterChecked(settings.isObsceneFilterEnabled)
        viewHost.setObsceneStopWordsChecked(settings.isObsceneStopwordsEnabled)
        viewHost.displayObsceneStopWords(settings.obsceneWords)
        resolveObsceneWordsEditorVisibility()
        resolvePublicDateView()
        resolveCategoryView()
        resolveSubjectView()
    }

    private fun resolveObsceneWordsEditorVisibility() {
        view?.setObsceneStopWordsVisible(
            settings.isObsceneStopwordsEnabled
        )
    }

    private fun resolvePublicDateView() {
        view?.let {
            it.setPublicDateVisible(community.type == VKApiCommunity.Type.PAGE)
            it.dislayPublicDate(settings.dateCreated)
        }
    }

    private fun resolveCategoryView() {
        val available = community.type == VKApiCommunity.Type.PAGE
        view?.setCategoryVisible(
            available
        )
        if (available) {
            view?.displayCategory(
                if (settings.category != null) settings.category.title else null
            )
        }
    }

    private fun resolveSubjectView() {
        val available = community.type == VKApiCommunity.Type.GROUP
        view?.setSubjectRootVisible(
            available
        )
        if (available) {
            val category = settings.category
            view?.displaySubjectValue(
                0,
                category?.title
            )
            val subAvailable = category != null && category.childs.nonNullNoEmpty()
            view?.setSubjectVisible(
                1,
                subAvailable
            )
            if (subAvailable) {
                val sub = settings.subcategory
                view?.displaySubjectValue(
                    1,
                    sub?.title
                )
            }
        }
    }

    fun onCategoryClick() {
        view?.showSelectOptionDialog(
            REQUEST_CATEGORY, settings.availableCategories
        )
    }

    fun fireOptionSelected(requestCode: Int, option: IdOption) {
        when (requestCode) {
            REQUEST_CATEGORY -> {
                settings.category = option
                resolveCategoryView()
            }
            REQUEST_DAY -> {
                settings.dateCreated.day = option.id
                resolvePublicDateView()
            }
            REQUEST_MONTH -> {
                settings.dateCreated.month = option.id
                resolvePublicDateView()
            }
            REQUEST_YEAR -> {
                settings.dateCreated.year = option.id
                resolvePublicDateView()
            }
        }
    }

    fun fireDayClick() {
        val options: MutableList<IdOption> = ArrayList(32)
        options.add(IdOption(0, getString(R.string.not_selected)))
        for (i in 1..31) {
            options.add(IdOption(i, i.toString()))
        }
        view?.showSelectOptionDialog(
            REQUEST_DAY, options
        )
    }

    fun fireMonthClick() {
        val options: MutableList<IdOption> = ArrayList(13)
        options.add(IdOption(0, getString(R.string.not_selected)))
        options.add(IdOption(1, getString(R.string.january)))
        options.add(IdOption(1, getString(R.string.january)))
        options.add(IdOption(2, getString(R.string.february)))
        options.add(IdOption(3, getString(R.string.march)))
        options.add(IdOption(4, getString(R.string.april)))
        options.add(IdOption(5, getString(R.string.may)))
        options.add(IdOption(6, getString(R.string.june)))
        options.add(IdOption(7, getString(R.string.july)))
        options.add(IdOption(8, getString(R.string.august)))
        options.add(IdOption(9, getString(R.string.september)))
        options.add(IdOption(10, getString(R.string.october)))
        options.add(IdOption(11, getString(R.string.november)))
        options.add(IdOption(12, getString(R.string.december)))
        view?.showSelectOptionDialog(
            REQUEST_MONTH, options
        )
    }

    fun fireYearClick() {
        val options: MutableList<IdOption> = ArrayList()
        options.add(IdOption(0, getString(R.string.not_selected)))
        for (i in Calendar.getInstance()[Calendar.YEAR] downTo 1800) {
            options.add(IdOption(i, i.toString()))
        }
        view?.showSelectOptionDialog(
            REQUEST_YEAR, options
        )
    }

    companion object {
        private const val REQUEST_CATEGORY = 1
        private const val REQUEST_DAY = 2
        private const val REQUEST_MONTH = 3
        private const val REQUEST_YEAR = 4
    }
}