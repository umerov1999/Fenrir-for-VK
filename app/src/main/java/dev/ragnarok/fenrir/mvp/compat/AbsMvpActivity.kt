package dev.ragnarok.fenrir.mvp.compat

import android.os.Bundle
import androidx.loader.app.LoaderManager
import dev.ragnarok.fenrir.activity.NoMainActivity
import dev.ragnarok.fenrir.mvp.core.IMvpView
import dev.ragnarok.fenrir.mvp.core.IPresenter
import dev.ragnarok.fenrir.mvp.core.PresenterAction
import dev.ragnarok.fenrir.mvp.core.RetPresenterAction
import dev.ragnarok.fenrir.mvp.view.IErrorView
import dev.ragnarok.fenrir.mvp.view.IProgressView
import dev.ragnarok.fenrir.mvp.view.IToastView
import dev.ragnarok.fenrir.mvp.view.IToolbarView
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView

abstract class AbsMvpActivity<P : IPresenter<V>, V : IMvpView> : NoMainActivity(),
    ViewHostDelegate.IFactoryProvider<P, V>, IAccountDependencyView, IProgressView, IErrorView,
    IToastView, IToolbarView {

    private val delegate = ViewHostDelegate<P, V>()

    protected val presenter: P?
        get() = delegate.presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegate.onCreate(
            this,
            getViewHost(),
            this,
            LoaderManager.getInstance(this),
            savedInstanceState
        )
    }

    // Override in case of fragment not implementing IPresenter<View> interface
    @Suppress("UNCHECKED_CAST")
    @SuppressWarnings("unchecked")
    private fun getViewHost(): V = this as V

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        delegate.onViewCreated()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        delegate.onSaveInstanceState(outState)
    }

    override fun onPause() {
        super.onPause()
        delegate.onPause()
    }

    override fun onResume() {
        super.onResume()
        delegate.onResume()
    }

    override fun onDestroy() {
        delegate.onDestroyView()
        delegate.onDestroy()
        super.onDestroy()
    }

    fun callPresenter(action: PresenterAction<P, V>) {
        delegate.callPresenter(action)
    }

    fun <T> callPresenter(action: RetPresenterAction<P, V, T>, onDefault: T): T {
        return delegate.callPresenter(action, onDefault)
    }

    fun postPresenterReceive(action: PresenterAction<P, V>) {
        delegate.postPresenterReceive(action)
    }
}