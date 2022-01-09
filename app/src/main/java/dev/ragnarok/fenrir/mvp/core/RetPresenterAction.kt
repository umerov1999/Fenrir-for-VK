package dev.ragnarok.fenrir.mvp.core

interface RetPresenterAction<P : IPresenter<V>, V : IMvpView, T> {
    fun call(presenter: P): T
}