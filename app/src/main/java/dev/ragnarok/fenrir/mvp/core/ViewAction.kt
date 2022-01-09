package dev.ragnarok.fenrir.mvp.core

interface ViewAction<V> {
    fun call(view: V)
}