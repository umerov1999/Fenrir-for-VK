package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.zoomhelper.ZoomHelper;

public abstract class NoMainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private final FragmentManager.OnBackStackChangedListener mBackStackListener = this::resolveToolbarNavigationIcon;
    private boolean isZoomPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(ThemesController.INSTANCE.currentStyle());
        Utils.prepareDensity(this);
        super.onCreate(savedInstanceState);
        isZoomPhoto = Settings.get().other().isDo_zoom_photo();
        setContentView(getNoMainContentView());

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(CurrentTheme.getStatusBarColor(this));
        w.setNavigationBarColor(CurrentTheme.getNavigationBarColor(this));

        getSupportFragmentManager().addOnBackStackChangedListener(mBackStackListener);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Utils.updateActivityContext(newBase));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isZoomPhoto) {
            return super.dispatchTouchEvent(ev);
        }
        return ZoomHelper.Companion.getInstance().dispatchTouchEvent(ev, this) || super.dispatchTouchEvent(ev);
    }

    @LayoutRes
    protected int getNoMainContentView() {
        return R.layout.activity_no_main;
    }

    @IdRes
    protected int getMainContainerViewId() {
        return R.id.fragment;
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        mToolbar = toolbar;
        resolveToolbarNavigationIcon();
    }

    private void resolveToolbarNavigationIcon() {
        if (Objects.isNull(mToolbar)) return;

        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            mToolbar.setNavigationIcon(R.drawable.arrow_left);
        } else {
            mToolbar.setNavigationIcon(R.drawable.close);
        }

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        Fragment front = getSupportFragmentManager().findFragmentById(getMainContainerViewId());
        if (front instanceof BackPressCallback) {
            if (!(((BackPressCallback) front).onBackPressed())) {
                return;
            }
        }

        if (fm.getBackStackEntryCount() > 1) {
            super.onBackPressed();
        } else {
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackListener);
        super.onDestroy();
    }
}
