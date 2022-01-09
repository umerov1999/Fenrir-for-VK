package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.StringRes;


public interface IProgressView {
    void displayProgressDialog(@StringRes int title, @StringRes int message, boolean cancelable);

    void dismissProgressDialog();
}
