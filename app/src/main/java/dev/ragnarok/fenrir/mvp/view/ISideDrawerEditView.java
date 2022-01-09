package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.SideDrawerCategory;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface ISideDrawerEditView extends IMvpView {
    void displayData(List<SideDrawerCategory> data);

    void goBackAndApplyChanges();
}
