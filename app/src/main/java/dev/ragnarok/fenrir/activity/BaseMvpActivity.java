package dev.ragnarok.fenrir.activity;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.mvp.compat.AbsMvpActivity;
import dev.ragnarok.fenrir.mvp.core.AbsPresenter;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.service.ErrorLocalizer;
import dev.ragnarok.fenrir.spots.SpotsDialog;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public abstract class BaseMvpActivity<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpActivity<P, V> implements IMvpView {

    public static final String EXTRA_HIDE_TOOLBAR = "extra_hide_toolbar";
    private AlertDialog mLoadingProgressDialog;

    protected static void safelySetCheched(CompoundButton button, boolean checked) {
        if (nonNull(button)) {
            button.setChecked(checked);
        }
    }

    protected static void safelySetText(TextView target, String text) {
        if (nonNull(target)) {
            target.setText(text);
        }
    }

    protected static void safelySetText(TextView target, @StringRes int text) {
        if (nonNull(target)) {
            target.setText(text);
        }
    }

    protected static void safelySetVisibleOrGone(View target, boolean visible) {
        if (nonNull(target)) {
            target.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    protected @Nullable
    Bundle getArguments() {
        if (nonNull(getIntent())) {
            return getIntent().getExtras();
        }
        return null;
    }

    protected @NonNull
    Bundle requireArguments() {
        return getIntent().getExtras();
    }

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if (!isFinishing()) {
            Toast.makeText(this, getString(titleTes, params), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String text) {
        if (!isFinishing()) {
            Utils.showRedTopToast(this, text);
        }
    }

    @Override
    public void showThrowable(Throwable throwable) {
        if (!isFinishing()) {
            showError(ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), throwable));
        }
    }

    @Override
    public CustomToast getCustomToast() {
        if (!isFinishing()) {
            return CustomToast.CreateCustomToast(this);
        }
        return CustomToast.CreateCustomToast(null);
    }

    @Override
    public void showError(@StringRes int titleTes, Object... params) {
        if (!isFinishing()) {
            showError(getString(titleTes, params));
        }
    }

    @Override
    public void setToolbarSubtitle(@Nullable String subtitle) {
        if (nonNull(getSupportActionBar())) {
            getSupportActionBar().setSubtitle(subtitle);
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        if (nonNull(getSupportActionBar())) {
            getSupportActionBar().setTitle(title);
        }
    }

    @Override
    public void displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public void displayAccountSupported() {
        // TODO: 18.12.2017
    }

    protected void styleSwipeRefreshLayoutWithCurrentTheme(@NonNull SwipeRefreshLayout swipeRefreshLayout, boolean needToolbarOffset) {
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(this, swipeRefreshLayout, needToolbarOffset);
    }

    @Override
    public void displayProgressDialog(@StringRes int title, @StringRes int message, boolean cancelable) {
        dismissProgressDialog();

        mLoadingProgressDialog = new SpotsDialog.Builder().setContext(this).setMessage(getString(title) + ": " + getString(message)).setCancelable(cancelable).build();
        mLoadingProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (nonNull(mLoadingProgressDialog)) {
            if (mLoadingProgressDialog.isShowing()) {
                mLoadingProgressDialog.cancel();
            }
        }
    }
}
