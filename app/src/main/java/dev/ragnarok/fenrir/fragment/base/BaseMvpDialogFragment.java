package dev.ragnarok.fenrir.fragment.base;

import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.mvp.compat.AbsMvpDialogFragment;
import dev.ragnarok.fenrir.mvp.core.AbsPresenter;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IErrorView;
import dev.ragnarok.fenrir.mvp.view.IToastView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.service.ErrorLocalizer;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;

public abstract class BaseMvpDialogFragment<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpDialogFragment<P, V> implements IMvpView, IAccountDependencyView, IErrorView, IToastView {

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if (isAdded()) {
            Toast.makeText(requireActivity(), getString(titleTes), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String text) {
        if (isAdded()) {
            Utils.showRedTopToast(requireActivity(), text);
        }
    }

    @Override
    public void showError(@StringRes int titleTes, Object... params) {
        if (isAdded()) {
            showError(getString(titleTes, params));
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
    public void displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public void displayAccountSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public CustomToast getCustomToast() {
        if (isAdded()) {
            return CustomToast.CreateCustomToast(requireActivity());
        }
        return CustomToast.CreateCustomToast(null);
    }
}
