package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.DrawerCategory;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IDrawerEditView extends IMvpView {
    void displayData(List<DrawerCategory> data);

    void goBackAndApplyChanges();
}