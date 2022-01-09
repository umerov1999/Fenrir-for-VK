package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.util.CustomToast;


public interface IToastView {
    void showToast(@StringRes int titleTes, boolean isLong, Object... params);

    CustomToast getCustomToast();
}
