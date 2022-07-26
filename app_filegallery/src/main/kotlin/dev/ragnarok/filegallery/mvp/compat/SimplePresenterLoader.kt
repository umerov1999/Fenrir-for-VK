package dev.ragnarok.filegallery.mvp.compat

import android.content.Context
import dev.ragnarok.filegallery.mvp.core.IMvpView
import dev.ragnarok.filegallery.mvp.core.IPresenter
import dev.ragnarok.filegallery.mvp.core.IPresenterFactory

class SimplePresenterLoader<P : IPresenter<V>, V : IMvpView> constructor(
    context: Context,
    var factory: IPresenterFactory<P>
) : androidx.loader.content.Loader<P>(context) {

    private var f: IPresenterFactory<P>? = factory

    private var presenter: P? = null

    fun get(): P {
        if (presenter == null) {
            presenter = factory.create()
            f = null
        }

        return presenter!!
    }

    override fun onReset() {
        super.onReset()
        presenter?.destroy()
        presenter = null
    }
}