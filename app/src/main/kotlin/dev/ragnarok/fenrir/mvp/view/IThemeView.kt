package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.settings.theme.ThemeValue

interface IThemeView : IMvpView {
    fun displayData(data: Array<ThemeValue>)
}