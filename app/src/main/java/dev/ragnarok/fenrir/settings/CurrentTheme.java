package dev.ragnarok.fenrir.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

import androidx.preference.PreferenceManager;

import com.squareup.picasso3.Transformation;

import java.io.File;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.picasso.transforms.EllipseTransformation;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.media.PathAnimator;

public class CurrentTheme {

    private static final String KEY_CHAT_BACKGROUND = "chat_background";

    private static final PathAnimator playPauseAnimator = createPathAnimator();

    private static PathAnimator createPathAnimator() {
        PathAnimator animator = new PathAnimator(0.293f, -26, -28, 1.0f);
        animator.addSvgKeyFrame("M 34.141 16.042 C 37.384 17.921 40.886 20.001 44.211 21.965 C 46.139 23.104 49.285 24.729 49.586 25.917 C 50.289 28.687 48.484 30 46.274 30 L 6 30.021 C 3.79 30.021 2.075 30.023 2 26.021 L 2.009 3.417 C 2.009 0.417 5.326 -0.58 7.068 0.417 C 10.545 2.406 25.024 10.761 34.141 16.042 Z", 166);
        animator.addSvgKeyFrame("M 37.843 17.769 C 41.143 19.508 44.131 21.164 47.429 23.117 C 48.542 23.775 49.623 24.561 49.761 25.993 C 50.074 28.708 48.557 30 46.347 30 L 6 30.012 C 3.79 30.012 2 28.222 2 26.012 L 2.009 4.609 C 2.009 1.626 5.276 0.664 7.074 1.541 C 10.608 3.309 28.488 12.842 37.843 17.769 Z", 200);
        animator.addSvgKeyFrame("M 40.644 18.756 C 43.986 20.389 49.867 23.108 49.884 25.534 C 49.897 27.154 49.88 24.441 49.894 26.059 C 49.911 28.733 48.6 30 46.39 30 L 6 30.013 C 3.79 30.013 2 28.223 2 26.013 L 2.008 5.52 C 2.008 2.55 5.237 1.614 7.079 2.401 C 10.656 4 31.106 14.097 40.644 18.756 Z", 217);
        animator.addSvgKeyFrame("M 43.782 19.218 C 47.117 20.675 50.075 21.538 50.041 24.796 C 50.022 26.606 50.038 24.309 50.039 26.104 C 50.038 28.736 48.663 30 46.453 30 L 6 29.986 C 3.79 29.986 2 28.196 2 25.986 L 2.008 6.491 C 2.008 3.535 5.196 2.627 7.085 3.316 C 10.708 4.731 33.992 14.944 43.782 19.218 Z", 234);
        animator.addSvgKeyFrame("M 47.421 16.941 C 50.544 18.191 50.783 19.91 50.769 22.706 C 50.761 24.484 50.76 23.953 50.79 26.073 C 50.814 27.835 49.334 30 47.124 30 L 5 30.01 C 2.79 30.01 1 28.22 1 26.01 L 1.001 10.823 C 1.001 8.218 3.532 6.895 5.572 7.26 C 7.493 8.01 47.421 16.941 47.421 16.941 Z", 267);
        animator.addSvgKeyFrame("M 47.641 17.125 C 50.641 18.207 51.09 19.935 51.078 22.653 C 51.07 24.191 51.062 21.23 51.088 23.063 C 51.109 24.886 49.587 27 47.377 27 L 5 27.009 C 2.79 27.009 1 25.219 1 23.009 L 0.983 11.459 C 0.983 8.908 3.414 7.522 5.476 7.838 C 7.138 8.486 47.641 17.125 47.641 17.125 Z", 300);
        animator.addSvgKeyFrame("M 48 7 C 50.21 7 52 8.79 52 11 C 52 19 52 19 52 19 C 52 21.21 50.21 23 48 23 L 4 23 C 1.79 23 0 21.21 0 19 L 0 11 C 0 8.79 1.79 7 4 7 C 48 7 48 7 48 7 Z", 383);
        return animator;
    }

    private static File getDrawerBackgroundFile(Context context, boolean light) {
        return new File(context.getFilesDir(), light ? "chat_light.jpg" : "chat_dark.jpg");
    }

    private static Drawable getStatic(Activity activity) {
        if (Settings.get().other().isCustom_chat_color()) {

            return new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{Settings.get().other().getColorChat(), Settings.get().other().getSecondColorChat()});
        }
        int color = getColorFromAttrs(R.attr.messages_background_color, activity, Color.WHITE);
        return new ColorDrawable(color);
    }

    public static PathAnimator getPlayPauseAnimator() {
        return playPauseAnimator;
    }

    public static Drawable getChatBackground(Activity activity) {
        boolean dark = Settings.get().ui().isDarkModeEnabled(activity);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String page = preferences.getString(KEY_CHAT_BACKGROUND, "0");
        assert page != null;
        Drawable ret;
        switch (page) {
            case "1":
                ret = getDrawableFromAttribute(activity, R.attr.chat_background_cookies);
                break;
            case "2":
                ret = getDrawableFromAttribute(activity, R.attr.chat_background_lines);
                break;
            case "3":
                ret = getDrawableFromAttribute(activity, R.attr.chat_background_runes);
                break;
            case "4":
                File bitmap = getDrawerBackgroundFile(activity, !dark);
                if (bitmap.exists()) {
                    Drawable d = Drawable.createFromPath(bitmap.getAbsolutePath());
                    if (d == null) {
                        return getStatic(activity);
                    }
                    if (Settings.get().other().isCustom_chat_color())
                        Utils.setColorFilter(d, Settings.get().other().getColorChat());
                    return d;
                }
                return getStatic(activity);
            default: //"0
                return getStatic(activity);
        }
        if (Settings.get().other().isCustom_chat_color()) {
            Drawable r1 = ret.mutate();
            Utils.setColorFilter(r1, Settings.get().other().getColorChat());
            return r1;
        }
        return ret;
    }

    public static Transformation createTransformationForAvatar() {
        int style = Settings.get()
                .ui()
                .getAvatarStyle();

        switch (style) {
            case AvatarStyle.OVAL:
                return new EllipseTransformation();
            case AvatarStyle.CIRCLE:
            default:
                return new RoundTransformation();
        }
    }

    public static int getColorPrimary(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorPrimary, context, "#000000");
    }

    public static int getColorControlNormal(Context context) {
        return getColorFromAttrs(androidx.appcompat.R.attr.colorControlNormal, context, "#000000");
    }

    public static int getColorToast(Context context) {
        return getColorFromAttrs(R.attr.toast_background, context, "#FFFFFF");
    }

    public static int getColorWhite(Context context) {
        return getColorFromAttrs(R.attr.white_color, context, "#FFFFFF");
    }

    public static int getColorOnPrimary(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorOnPrimary, context, "#000000");
    }

    public static int getColorSurface(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorSurface, context, "#000000");
    }

    public static int getColorOnSurface(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorOnSurface, context, "#000000");
    }

    public static int getColorBackground(Context context) {
        return getColorFromAttrs(android.R.attr.colorBackground, context, "#000000");
    }

    public static int getColorOnBackground(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorOnBackground, context, "#000000");
    }

    public static int getStatusBarColor(Context context) {
        return getColorFromAttrs(android.R.attr.statusBarColor, context, "#000000");
    }

    public static int getNavigationBarColor(Context context) {
        return getColorFromAttrs(android.R.attr.navigationBarColor, context, "#000000");
    }

    public static int getColorSecondary(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorSecondary, context, "#000000");
    }

    public static int getStatusBarNonColored(Context context) {
        return getColorFromAttrs(R.attr.statusbarNonColoredColor, context, "#000000");
    }

    public static int getMessageUnreadColor(Context context) {
        return getColorFromAttrs(R.attr.message_unread_color, context, "#ffffff");
    }

    public static int getMessageBackgroundSquare(Context context) {
        return getColorFromAttrs(R.attr.message_bubble_color, context, "#000000");
    }

    public static int getPrimaryTextColorCode(Context context) {
        return getColorFromAttrs(android.R.attr.textColorPrimary, context, "#000000");
    }

    public static int getSecondaryTextColorCode(Context context) {
        return getColorFromAttrs(android.R.attr.textColorSecondary, context, "#000000");
    }

    public static int getDialogsUnreadColor(Context context) {
        return getColorFromAttrs(R.attr.dialogs_unread_color, context, "#20b0b0b0");
    }

    public static int getMy_messages_bubble_color(Context context) {
        return getColorFromAttrs(R.attr.my_messages_bubble_color, context, "#20b0b0b0");
    }

    public static int getColorFromAttrs(int resId, Context context, String defaultColor) {
        int[] attribute = {resId};
        TypedArray array = context.getTheme().obtainStyledAttributes(attribute);
        int color = array.getColor(0, Color.parseColor(defaultColor));
        array.recycle();
        return color;
    }

    public static int getColorFromAttrs(int resId, Context context, int defaultColor) {
        int[] attribute = {resId};
        TypedArray array = context.getTheme().obtainStyledAttributes(attribute);
        int color = array.getColor(0, defaultColor);
        array.recycle();
        return color;
    }

    public static Drawable getDrawableFromAttribute(Context context, int attr) {
        int[] attribute = {attr};
        TypedArray array = context.getTheme().obtainStyledAttributes(attribute);
        Drawable ret = array.getDrawable(0);
        array.recycle();
        return ret;
    }
}
