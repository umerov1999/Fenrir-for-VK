package dev.ragnarok.filegallery.mvp.core

interface IPresenterFactory<T : IPresenter<*>> {
    fun create(): T
}