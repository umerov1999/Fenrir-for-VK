package dev.ragnarok.fenrir.mvp.view.base;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.view.steppers.base.AbsStepsHost;

public interface ISteppersView<H extends AbsStepsHost<?>> extends IMvpView {
    void updateStepView(int step);

    void moveSteppers(int from, int to);

    void goBack();

    void hideKeyboard();

    void updateStepButtonsAvailability(int step);

    void attachSteppersHost(@NonNull H mHost);
}
