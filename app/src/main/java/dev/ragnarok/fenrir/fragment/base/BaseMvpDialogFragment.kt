package dev.ragnarok.fenrir.fragment.base

import android.graphics.Color
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.mvp.compat.AbsMvpDialogFragment
import dev.ragnarok.fenrir.mvp.core.AbsPresenter
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.IToastView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView
import dev.ragnarok.fenrir.service.ErrorLocalizer
import dev.ragnarok.fenrir.util.CustomToast
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.Utils

abstract class BaseMvpDialogFragment<P : AbsPresenter<V>, V : IMvpView> :
    AbsMvpDialogFragment<P, V>(), IMvpView, IAccountDependencyView, IErrorView, IToastView {
    override fun showToast(@StringRes titleTes: Int, isLong: Boolean, vararg params: Any?) {
        if (isAdded) {
            Toast.makeText(
                requireActivity(),
                getString(titleTes),
                if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun showError(errorText: String?) {
        if (isAdded) {
            Utils.showRedTopToast(requireActivity(), errorText)
        }
    }

    override fun showError(@StringRes titleTes: Int, vararg params: Any?) {
        if (isAdded) {
            showError(getString(titleTes, *params))
        }
    }

    override fun showThrowable(throwable: Throwable?) {
        if (isAdded) {
            view?.let {
                Snackbar.make(
                    it,
                    ErrorLocalizer.localizeThrowable(provideApplicationContext(), throwable),
                    BaseTransientBottomBar.LENGTH_LONG
                ).setTextColor(
                    Color.WHITE
                ).setBackgroundTint(Color.parseColor("#eeff0000"))
                    .setAction(R.string.more_info) {
                        val Text = StringBuilder()
                        for (stackTraceElement in throwable!!.stackTrace) {
                            Text.append("    ")
                            Text.append(stackTraceElement)
                            Text.append("\r\n")
                        }
                        MaterialAlertDialogBuilder(requireActivity())
                            .setIcon(R.drawable.ic_error)
                            .setMessage(Text)
                            .setTitle(R.string.more_info)
                            .setPositiveButton(R.string.button_ok, null)
                            .setCancelable(true)
                            .show()
                    }.setActionTextColor(Color.WHITE).show()
            } ?: showError(
                ErrorLocalizer.localizeThrowable(
                    provideApplicationContext(),
                    throwable
                )
            )
        }
    }

    override fun displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    override fun displayAccountSupported() {
        // TODO: 18.12.2017
    }

    override val customToast: CustomToast
        get() = if (isAdded) {
            CreateCustomToast(requireActivity())
        } else CreateCustomToast(null)
}
