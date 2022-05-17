package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.Day
import dev.ragnarok.fenrir.model.IdOption
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICommunityOptionsView : IMvpView, IAccountDependencyView, IErrorView, IProgressView {
    fun displayName(name: String?)
    fun displayDescription(description: String?)
    fun setCommunityTypeVisible(visible: Boolean)
    fun displayAddress(address: String?)
    fun setCategoryVisible(visible: Boolean)
    fun displayCategory(categoryText: String?)
    fun showSelectOptionDialog(requestCode: Int, data: List<IdOption>)
    fun setSubjectRootVisible(visible: Boolean)
    fun setSubjectVisible(index: Int, visible: Boolean)
    fun displaySubjectValue(index: Int, value: String?)
    fun displayWebsite(website: String?)
    fun setPublicDateVisible(visible: Boolean)
    fun dislayPublicDate(day: Day)
    fun setFeedbackCommentsRootVisible(visible: Boolean)
    fun setFeedbackCommentsChecked(checked: Boolean)
    fun setObsceneFilterChecked(checked: Boolean)
    fun setObsceneStopWordsChecked(checked: Boolean)
    fun setObsceneStopWordsVisible(visible: Boolean)
    fun displayObsceneStopWords(words: String?)
}