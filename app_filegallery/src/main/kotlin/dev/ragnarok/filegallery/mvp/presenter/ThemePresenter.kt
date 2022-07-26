package dev.ragnarok.filegallery.mvp.presenter

import android.os.Bundle
import dev.ragnarok.filegallery.mvp.core.AbsPresenter
import dev.ragnarok.filegallery.mvp.view.IThemeView
import dev.ragnarok.filegallery.settings.theme.ThemesController

class ThemePresenter(savedInstanceState: Bundle?) : AbsPresenter<IThemeView>(savedInstanceState) {

    override fun onGuiCreated(viewHost: IThemeView) {
        super.onGuiCreated(viewHost)
        viewHost.displayData(ThemesController.themes)
    }
}