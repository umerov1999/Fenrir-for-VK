package dev.ragnarok.fenrir.mvp.view

import androidx.annotation.StringRes
import dev.ragnarok.fenrir.model.Poll
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

interface ICreatePollView : IAccountDependencyView, IMvpView, IProgressView, IErrorView {
    fun displayQuestion(question: String?)
    fun setAnonymous(anomymous: Boolean)
    fun displayOptions(options: Array<String?>?)
    fun showQuestionError(@StringRes message: Int)
    fun showOptionError(index: Int, @StringRes message: Int)
    fun sendResultAndGoBack(poll: Poll)
    fun setMultiply(multiply: Boolean)
}