package dev.ragnarok.fenrir.activity

import android.app.Activity
import dev.ragnarok.fenrir.listener.ActivityFuturesListener

class ActivityFeatures(builder: Builder) {
    private val hideMenu: Boolean = builder.blockNavigationFeature?.blockNavigationDrawer == true

    fun apply(activity: Activity) {
        if (activity !is ActivityFuturesListener) return
        val listener = activity as ActivityFuturesListener
        listener.hideMenu(hideMenu)
    }

    class Builder {
        var blockNavigationFeature: BlockNavigationFeature? = null
        fun begin(): BlockNavigationFeature {
            return BlockNavigationFeature(this)
        }

        fun build(): ActivityFeatures {
            return ActivityFeatures(this)
        }
    }

    open class Feature internal constructor(val builder: Builder)

    class BlockNavigationFeature(b: Builder) : Feature(b) {
        var blockNavigationDrawer = false
        fun setHideNavigationMenu(blockNavigationDrawer: Boolean): Builder {
            this.blockNavigationDrawer = blockNavigationDrawer
            return builder
        }

        init {
            b.blockNavigationFeature = this
        }
    }
}