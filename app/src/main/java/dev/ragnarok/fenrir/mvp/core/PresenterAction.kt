package dev.ragnarok.fenrir.mvp.core

interface PresenterAction<P : IPresenter<V>, V : IMvpView> {
    fun call(presenter: P)
}