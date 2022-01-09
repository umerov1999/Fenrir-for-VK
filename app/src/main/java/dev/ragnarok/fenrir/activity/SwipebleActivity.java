package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import dev.ragnarok.fenrir.activity.slidr.Slidr;
import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.MainActivityTransforms;
import dev.ragnarok.fenrir.util.ViewUtils;

/**
 * Тот же MainActivity, предназначенный для шаринга контента
 * Отличие только в том, что этот активити может существовать в нескольких экземплярах
 */
public class SwipebleActivity extends MainActivity {

    public static void applyIntent(Intent intent) {
        intent.putExtra(MainActivity.EXTRA_NO_REQUIRE_PIN, true);
    }

    public static void start(Context context, Intent intent) {
        intent.putExtra(MainActivity.EXTRA_NO_REQUIRE_PIN, true);
        context.startActivity(intent);
    }

    @Override
    protected @MainActivityTransforms
    int getMainActivityTransform() {
        return MainActivityTransforms.SWIPEBLE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Slidr.attach(this, new SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this)).build());
        // потому, что в onBackPressed к этому числу будут прибавлять 2000 !!!! и выход за границы
        mLastBackPressedTime = Long.MAX_VALUE - DOUBLE_BACK_PRESSED_TIMEOUT;
    }

    @Override
    public void onDestroy() {
        ViewUtils.keyboardHide(this);
        super.onDestroy();
    }
}
