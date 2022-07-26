package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.mvp.core.AbsPresenter
import dev.ragnarok.fenrir.mvp.view.IThemeView
import dev.ragnarok.fenrir.settings.theme.ThemesController

class ThemePresenter(savedInstanceState: Bundle?) : AbsPresenter<IThemeView>(savedInstanceState) {

    override fun onGuiCreated(viewHost: IThemeView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(ThemesController.themes)
    }
}