package dev.ragnarok.fenrir.activity;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.listener.AppStyleable;
import dev.ragnarok.fenrir.settings.Settings;

public class ActivityFeatures {

    private final boolean hideMenu;
    private final int statusBarColorOption;
    private final boolean statusBarInvertIconsOption;

    public ActivityFeatures(@NonNull Builder builder) {
        hideMenu = builder.blockNavigationFeature.blockNavigationDrawer;
        statusBarColorOption = builder.statusbarColorFeature.statusBarColorOption;
        statusBarInvertIconsOption = builder.statusbarColorFeature.statusBarIconInvertedOption;
    }

    public void apply(@NonNull Activity activity) {
        if (!(activity instanceof AppStyleable)) return;

        AppStyleable styleable = (AppStyleable) activity;
        styleable.hideMenu(hideMenu);

        styleable.setStatusbarColored(statusBarColorOption == StatusbarColorFeature.STATUSBAR_COLOR_COLORED, statusBarInvertIconsOption);
    }

    public static class Builder {

        private BlockNavigationFeature blockNavigationFeature;
        private StatusbarColorFeature statusbarColorFeature;

        public BlockNavigationFeature begin() {
            return new BlockNavigationFeature(this);
        }

        public ActivityFeatures build() {
            return new ActivityFeatures(this);
        }
    }

    private static class Feature {
        final Builder builder;

        Feature(Builder b) {
            builder = b;
        }
    }

    public static class StatusbarColorFeature extends Feature {

        public static final int STATUSBAR_COLOR_COLORED = 1;
        public static final int STATUSBAR_COLOR_NON_COLORED = 2;

        private int statusBarColorOption;
        private boolean statusBarIconInvertedOption;

        private StatusbarColorFeature(Builder b) {
            super(b);
            b.statusbarColorFeature = this;
        }

        public Builder setBarsColored(Context context, boolean colored) {
            statusBarColorOption = colored ? STATUSBAR_COLOR_COLORED : STATUSBAR_COLOR_NON_COLORED;
            statusBarIconInvertedOption = !Settings.get().ui().isDarkModeEnabled(context);
            return builder;
        }

        public Builder setBarsColored(boolean colored, boolean invertIcons) {
            statusBarColorOption = colored ? STATUSBAR_COLOR_COLORED : STATUSBAR_COLOR_NON_COLORED;
            statusBarIconInvertedOption = invertIcons;
            return builder;
        }
    }

    public static class BlockNavigationFeature extends Feature {
        private boolean blockNavigationDrawer;

        private BlockNavigationFeature(Builder b) {
            super(b);
            b.blockNavigationFeature = this;
        }

        public StatusbarColorFeature setHideNavigationMenu(boolean blockNavigationDrawer) {
            this.blockNavigationDrawer = blockNavigationDrawer;
            return new StatusbarColorFeature(builder);
        }
    }
}
