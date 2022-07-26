package dev.ragnarok.filegallery.mvp.view

import dev.ragnarok.filegallery.mvp.core.IMvpView
import dev.ragnarok.filegallery.settings.theme.ThemeValue

interface IThemeView : IMvpView {
    fun displayData(data: Array<ThemeValue>)
}