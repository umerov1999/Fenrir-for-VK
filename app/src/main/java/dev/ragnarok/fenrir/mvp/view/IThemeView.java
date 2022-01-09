package dev.ragnarok.fenrir.mvp.view;

import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.settings.theme.ThemeValue;


public interface IThemeView extends IMvpView {
    void displayData(ThemeValue[] data);
}
