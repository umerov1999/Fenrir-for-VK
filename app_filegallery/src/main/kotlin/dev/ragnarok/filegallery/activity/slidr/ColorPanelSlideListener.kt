package dev.ragnarok.filegallery.activity.slidr

import android.animation.ArgbEvaluator
import android.app.Activity
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowCompat
import dev.ragnarok.filegallery.activity.slidr.widget.SliderPanel.OnPanelSlideListener
import dev.ragnarok.filegallery.settings.CurrentTheme
import dev.ragnarok.filegallery.util.Utils

internal open class ColorPanelSlideListener(
    private val activity: Activity,
    private val fromFromBlackToNormalNavigation: Boolean,
    private val useAlpha: Boolean
) : OnPanelSlideListener {
    private val evaluator = ArgbEvaluator()
    private val backgroundColor = CurrentTheme.getColorBackground(activity)
    private var lastInvertColor: Boolean? = null

    override fun onStateChanged(state: Int) {
        // Unused.
    }

    override fun onClosed() {
        Utils.finishActivityImmediate(activity)
    }

    override fun onOpened() {
        // Unused.
    }

    private fun isDark(@ColorInt color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.2
    }

    override fun onSlideChange(percent: Float) {
        try {
            if (fromFromBlackToNormalNavigation) {
                val w = activity.window
                if (w != null) {
                    val targetColor =
                        evaluator.evaluate(percent, backgroundColor, Color.BLACK) as Int
                    if (!Utils.hasVanillaIceCreamTarget()) {
                        @Suppress("deprecation")
                        w.statusBarColor = targetColor
                        @Suppress("deprecation")
                        w.navigationBarColor = targetColor
                    }
                    val invertIcons = !isDark(targetColor)
                    if (lastInvertColor != invertIcons) {
                        lastInvertColor = invertIcons
                        WindowCompat.getInsetsController(w, w.decorView).apply {
                            isAppearanceLightStatusBars = invertIcons
                            isAppearanceLightNavigationBars = invertIcons
                        }
                    }
                }
            }
            if (useAlpha) {
                activity.window.decorView.rootView.alpha = Utils.clamp(percent, 0f, 1f)
            }
        } catch (_: Exception) {
        }
    }
}
