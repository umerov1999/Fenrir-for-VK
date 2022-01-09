package dev.ragnarok.fenrir.mvp.core

interface IPresenterFactory<T : IPresenter<*>> {
    fun create(): T
}