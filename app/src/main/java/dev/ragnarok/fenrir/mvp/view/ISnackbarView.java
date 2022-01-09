package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.StringRes;


public interface ISnackbarView {
    void showSnackbar(@StringRes int res, boolean isLong);
}
