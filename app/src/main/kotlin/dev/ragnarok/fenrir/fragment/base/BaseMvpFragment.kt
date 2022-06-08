package dev.ragnarok.fenrir.fragment.base

import android.graphics.Color
import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import dev.ragnarok.fenrir.Includes.provideApplicationContext
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.ActivityUtils
import dev.ragnarok.fenrir.mvp.compat.AbsMvpFragment
import dev.ragnarok.fenrir.mvp.core.AbsPresenter
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.IProgressView
import dev.ragnarok.fenrir.mvp.view.IToastView
import dev.ragnarok.fenrir.mvp.view.IToolbarView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView
import dev.ragnarok.fenrir.service.ErrorLocalizer.localizeThrowable
import dev.ragnarok.fenrir.util.CustomToast
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.ViewUtils
import dev.ragnarok.fenrir.util.spots.SpotsDialog

abstract class BaseMvpFragment<P : AbsPresenter<V>, V : IMvpView> : AbsMvpFragment<P, V>(),
    IMvpView, IAccountDependencyView, IProgressView, IErrorView, IToastView, IToolbarView {
    private var mLoadingProgressDialog: AlertDialog? = null
    protected fun hasHideToolbarExtra(): Boolean {
        return arguments?.getBoolean(EXTRA_HIDE_TOOLBAR) == true
    }

    override fun showError(errorText: String?) {
        if (isAdded) {
            Utils.showRedTopToast(requireActivity(), errorText)
        }
    }

    override fun showThrowable(throwable: Throwable?) {
        if (isAdded) {
            view?.let {
                Snackbar.make(
                    it,
                    localizeThrowable(provideApplicationContext(), throwable),
                    BaseTransientBottomBar.LENGTH_LONG
                ).setTextColor(
                    Color.WHITE
                ).setBackgroundTint(Color.parseColor("#eeff0000"))
                    .setAction(R.string.more_info) {
                        val Text = StringBuilder()
                        Text.append(localizeThrowable(provideApplicationContext(), throwable))
                        Text.append("\r\n")
                        for (stackTraceElement in (throwable ?: return@setAction).stackTrace) {
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
            } ?: showError(localizeThrowable(provideApplicationContext(), throwable))
        }
    }

    override val customToast: CustomToast
        get() = if (isAdded) {
            CreateCustomToast(requireActivity())
        } else CreateCustomToast(null)

    override fun showError(@StringRes titleTes: Int, vararg params: Any?) {
        if (isAdded) {
            showError(getString(titleTes, *params))
        }
    }

    override fun setToolbarSubtitle(subtitle: String?) {
        ActivityUtils.setToolbarSubtitle(this, subtitle)
    }

    override fun setToolbarTitle(title: String?) {
        ActivityUtils.setToolbarTitle(this, title)
    }

    override fun displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    override fun displayAccountSupported() {
        // TODO: 18.12.2017
    }

    protected fun styleSwipeRefreshLayoutWithCurrentTheme(
        swipeRefreshLayout: SwipeRefreshLayout?,
        needToolbarOffset: Boolean
    ) {
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(
            requireActivity(),
            swipeRefreshLayout,
            needToolbarOffset
        )
    }

    override fun displayProgressDialog(
        @StringRes title: Int,
        @StringRes message: Int,
        cancelable: Boolean
    ) {
        dismissProgressDialog()
        mLoadingProgressDialog = SpotsDialog.Builder().setContext(requireActivity())
            .setMessage(getString(title) + ": " + getString(message)).setCancelable(cancelable)
            .build()
        mLoadingProgressDialog?.show()
    }

    override fun dismissProgressDialog() {
        if (mLoadingProgressDialog?.isShowing == true) {
            mLoadingProgressDialog?.cancel()
        }
    }

    companion object {
        const val EXTRA_HIDE_TOOLBAR = "extra_hide_toolbar"


        fun safelySetChecked(button: CompoundButton?, checked: Boolean) {
            button?.isChecked = checked
        }


        fun safelySetText(target: TextView?, text: String?) {
            target?.text = text
        }


        fun safelySetText(target: TextView?, @StringRes text: Int) {
            target?.setText(text)
        }


        fun safelySetVisibleOrGone(target: View?, visible: Boolean) {
            target?.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }
}
