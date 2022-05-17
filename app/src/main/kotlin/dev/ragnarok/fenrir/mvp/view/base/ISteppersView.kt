package dev.ragnarok.fenrir.mvp.view.base

import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.view.steppers.base.AbsStepsHost

interface ISteppersView<H : AbsStepsHost<*>> : IMvpView {
    fun updateStepView(step: Int)
    fun moveSteppers(from: Int, to: Int)
    fun goBack()
    fun hideKeyboard()
    fun updateStepButtonsAvailability(step: Int)
    fun attachSteppersHost(mHost: H)
}