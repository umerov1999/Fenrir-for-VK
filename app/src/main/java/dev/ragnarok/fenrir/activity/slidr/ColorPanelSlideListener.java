package dev.ragnarok.fenrir.activity.slidr;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.animation.ArgbEvaluator;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.Window;

import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

import dev.ragnarok.fenrir.activity.slidr.widget.SliderPanel;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;


class ColorPanelSlideListener implements SliderPanel.OnPanelSlideListener {

    private final Activity activity;
    private final boolean fromUnColoredToColoredStatusBar;
    private final boolean useAlpha;
    private final ArgbEvaluator evaluator = new ArgbEvaluator();

    private final @ColorInt
    int statusBarNonColored;
    private final @ColorInt
    int statusBarColored;
    private final @ColorInt
    int navigationBarNonColored;
    private final @ColorInt
    int navigationBarColored;

    ColorPanelSlideListener(Activity activity, boolean fromUnColoredToColoredStatusBar, boolean useAlpha) {
        this.activity = activity;
        this.fromUnColoredToColoredStatusBar = fromUnColoredToColoredStatusBar;
        this.useAlpha = useAlpha;

        statusBarNonColored = CurrentTheme.getStatusBarNonColored(activity);
        statusBarColored = CurrentTheme.getStatusBarColor(activity);

        navigationBarNonColored = Color.BLACK;
        navigationBarColored = CurrentTheme.getNavigationBarColor(activity);
    }


    @Override
    public void onStateChanged(int state) {
        // Unused.
    }


    @Override
    public void onClosed() {
        activity.finish();
        activity.overridePendingTransition(0, 0);
    }


    @Override
    public void onOpened() {
        // Unused.
    }

    private boolean isDark(@ColorInt int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }


    @Override
    public void onSlideChange(float percent) {
        try {
            if (isFromUnColoredToColoredStatusBar()) {

                int statusColor = (int) evaluator.evaluate(percent, statusBarColored, statusBarNonColored);
                int navigationColor = (int) evaluator.evaluate(percent, navigationBarColored, navigationBarNonColored);
                Window w = activity.getWindow();
                if (nonNull(w)) {
                    w.setStatusBarColor(statusColor);
                    w.setNavigationBarColor(navigationColor);
                    boolean invertIcons = !isDark(statusColor);
                    if (Utils.hasMarshmallow()) {
                        int flags = w.getDecorView().getSystemUiVisibility();
                        if (invertIcons) {
                            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        } else {
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                        }
                        w.getDecorView().setSystemUiVisibility(flags);
                    }

                    if (Utils.hasOreo()) {
                        int flags = w.getDecorView().getSystemUiVisibility();
                        if (invertIcons) {
                            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        } else {
                            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                        }
                        w.getDecorView().setSystemUiVisibility(flags);
                    }
                }
            }
            if (isUseAlpha()) {
                activity.getWindow().getDecorView().getRootView().setAlpha(Utils.clamp(percent, 0f, 1f));
            }
        } catch (Exception ignored) {
        }
    }

    boolean isFromUnColoredToColoredStatusBar() {
        return fromUnColoredToColoredStatusBar;
    }

    boolean isUseAlpha() {
        return useAlpha;
    }
}
