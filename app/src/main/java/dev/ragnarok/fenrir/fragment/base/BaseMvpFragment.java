package dev.ragnarok.fenrir.fragment.base;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.mvp.compat.AbsMvpFragment;
import dev.ragnarok.fenrir.mvp.core.AbsPresenter;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IErrorView;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.mvp.view.IToastView;
import dev.ragnarok.fenrir.mvp.view.IToolbarView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.service.ErrorLocalizer;
import dev.ragnarok.fenrir.spots.SpotsDialog;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public abstract class BaseMvpFragment<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpFragment<P, V> implements IMvpView, IAccountDependencyView, IProgressView, IErrorView, IToastView, IToolbarView {

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

    protected boolean hasHideToolbarExtra() {
        return nonNull(getArguments()) && getArguments().getBoolean(EXTRA_HIDE_TOOLBAR);
    }

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if (isAdded()) {
            Toast.makeText(requireActivity(), getString(titleTes, params), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String text) {
        if (isAdded()) {
            Utils.showRedTopToast(requireActivity(), text);
        }
    }

    @Override
    public void showThrowable(Throwable throwable) {
        if (isAdded()) {
            if (getView() == null) {
                showError(ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), throwable));
                return;
            }
            Snackbar.make(getView(), ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), throwable), BaseTransientBottomBar.LENGTH_LONG).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000"))
                    .setAction(R.string.more_info, v -> {
                        StringBuilder Text = new StringBuilder();
                        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                            Text.append("    ");
                            Text.append(stackTraceElement);
                            Text.append("\r\n");
                        }
                        new MaterialAlertDialogBuilder(requireActivity())
                                .setIcon(R.drawable.ic_error)
                                .setMessage(Text)
                                .setTitle(R.string.more_info)
                                .setPositiveButton(R.string.button_ok, null)
                                .setCancelable(true)
                                .show();
                    }).setActionTextColor(Color.WHITE).show();
        }
    }

    @Override
    public CustomToast getCustomToast() {
        if (isAdded()) {
            return CustomToast.CreateCustomToast(requireActivity());
        }
        return CustomToast.CreateCustomToast(null);
    }

    @Override
    public void showError(@StringRes int titleTes, Object... params) {
        if (isAdded()) {
            showError(getString(titleTes, params));
        }
    }

    @Override
    public void setToolbarSubtitle(@Nullable String subtitle) {
        ActivityUtils.setToolbarSubtitle(this, subtitle);
    }

    @Override
    public void setToolbarTitle(String title) {
        ActivityUtils.setToolbarTitle(this, title);
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
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), swipeRefreshLayout, needToolbarOffset);
    }

    @Override
    public void displayProgressDialog(@StringRes int title, @StringRes int message, boolean cancelable) {
        dismissProgressDialog();

        mLoadingProgressDialog = new SpotsDialog.Builder().setContext(requireActivity()).setMessage(getString(title) + ": " + getString(message)).setCancelable(cancelable).build();
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
