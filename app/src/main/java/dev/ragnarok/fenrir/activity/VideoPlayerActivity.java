package dev.ragnarok.fenrir.activity;

import android.app.AppOpsManager;
import android.app.PictureInPictureParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.slidr.Slidr;
import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig;
import dev.ragnarok.fenrir.listener.AppStyleable;
import dev.ragnarok.fenrir.media.video.ExoVideoPlayer;
import dev.ragnarok.fenrir.media.video.IVideoPlayer;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.InternalVideoSize;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VideoSize;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.IProxySettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.AlternativeAspectRatioFrameLayout;
import dev.ragnarok.fenrir.view.VideoControllerView;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class VideoPlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        VideoControllerView.MediaPlayerControl, IVideoPlayer.IVideoSizeChangeListener, AppStyleable {

    public static final String EXTRA_VIDEO = "video";
    public static final String EXTRA_SIZE = "size";
    public static final String EXTRA_LOCAL = "local";
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private View mDecorView;
    private ImageView mSpeed;
    private VideoControllerView mControllerView;
    private AlternativeAspectRatioFrameLayout Frame;
    private IVideoPlayer mPlayer;
    private Video video;
    private boolean onStopCalled;
    private @InternalVideoSize
    int size;
    private boolean doNotPause;
    private final ActivityResultLauncher<Intent> requestSwipeble = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> doNotPause = false);
    private boolean isLocal;
    private boolean isLandscape;

    private void onOpen() {
        Intent intent = new Intent(this, SwipebleActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN_WALL);
        intent.putExtra(Extra.OWNER_ID, video.getOwnerId());
        doNotPause = true;
        SwipebleActivity.applyIntent(intent);
        requestSwipeble.launch(intent);
    }

    @Override
    protected void onStop() {
        onStopCalled = true;
        super.onStop();
    }

    @Override
    public void finish() {
        finishAndRemoveTask();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(VideoPlayerActivity.class.getName(), "onNewIntent, intent: " + intent);
        handleIntent(intent, true);
    }

    private void handleIntent(Intent intent, boolean update) {
        if (intent == null) {
            return;
        }
        video = intent.getParcelableExtra(EXTRA_VIDEO);
        size = intent.getIntExtra(EXTRA_SIZE, InternalVideoSize.SIZE_240);
        isLocal = intent.getBooleanExtra(EXTRA_LOCAL, false);
        Toolbar toolbar = findViewById(R.id.toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(video.getTitle());
            actionBar.setSubtitle(video.getDescription());
        }

        if (toolbar != null) {
            if (!isLocal) {
                mCompositeDisposable.add(OwnerInfo.getRx(this, Settings.get().accounts().getCurrent(), video.getOwnerId())
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(userInfo -> {
                            ImageView av = findViewById(R.id.toolbar_avatar);
                            av.setImageBitmap(userInfo.getAvatar());
                            av.setOnClickListener(v -> onOpen());
                            if (Utils.isEmpty(video.getDescription()) && actionBar != null) {
                                actionBar.setSubtitle(userInfo.getOwner().getFullName());
                            }
                        }, throwable -> {
                        }));
            } else {
                findViewById(R.id.toolbar_avatar).setVisibility(View.GONE);
            }
        }
        if (update) {
            IProxySettings settings = Injection.provideProxySettings();
            ProxyConfig config = settings.getActiveProxy();

            String url = getFileUrl();

            mPlayer.updateSource(this, url, config, size);
            mPlayer.play();
            mControllerView.updateComment(!isLocal && video.isCanComment());
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Utils.updateActivityContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(ThemesController.INSTANCE.currentStyle());
        Utils.prepareDensity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        if (Settings.get().other().isVideo_swipes()) {
            Slidr.attach(this, new SlidrConfig.Builder().scrimColor(CurrentTheme.getColorBackground(this)).fromUnColoredToColoredStatusBar(true).build());
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mDecorView = getWindow().getDecorView();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (Objects.isNull(savedInstanceState)) {
            handleIntent(getIntent(), false);
        }

        mControllerView = new VideoControllerView(this);

        ViewGroup surfaceContainer = findViewById(R.id.videoSurfaceContainer);
        SurfaceView mSurfaceView = findViewById(R.id.videoSurface);
        Frame = findViewById(R.id.aspect_ratio_layout);
        surfaceContainer.setOnClickListener(v -> resolveControlsVisibility());

        SurfaceHolder videoHolder = mSurfaceView.getHolder();
        videoHolder.addCallback(this);

        resolveControlsVisibility();

        mPlayer = createPlayer();
        mPlayer.addVideoSizeChangeListener(this);
        mPlayer.play();

        mSpeed = findViewById(R.id.toolbar_play_speed);
        Utils.setTint(mSpeed, mPlayer.isPlaybackSpeed() ? CurrentTheme.getColorPrimary(this) : Color.parseColor("#ffffff"));
        mSpeed.setOnClickListener(v -> {
            mPlayer.togglePlaybackSpeed();
            Utils.setTint(mSpeed, mPlayer.isPlaybackSpeed() ? CurrentTheme.getColorPrimary(this) : Color.parseColor("#ffffff"));
        });

        mControllerView.setMediaPlayer(this);
        if (Settings.get().other().isVideo_controller_to_decor()) {
            mControllerView.setAnchorView((ViewGroup) mDecorView, true);
        } else {
            mControllerView.setAnchorView(findViewById(R.id.panel), false);
        }
        mControllerView.updateComment(!isLocal && video != null && video.isCanComment());
        mControllerView.updatePip(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) && hasPipPermission());
    }

    private IVideoPlayer createPlayer() {
        IProxySettings settings = Injection.provideProxySettings();
        ProxyConfig config = settings.getActiveProxy();

        String url = getFileUrl();
        return new ExoVideoPlayer(this, url, config, size, isPause -> mControllerView.updatePausePlay());
    }

    private void resolveControlsVisibility() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;

        if (actionBar.isShowing()) {
            actionBar.hide();
            mControllerView.hide();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                mDecorView.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES));

            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        } else {
            actionBar.show();
            mControllerView.show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                mDecorView.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT));
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    protected void onDestroy() {
        mPlayer.release();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(false, false)
                .build()
                .apply(this);

        onStopCalled = false;

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null && actionBar.isShowing()) {
            actionBar.hide();
            mControllerView.hide();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            mDecorView.setLayoutParams(new WindowManager.LayoutParams(WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES));
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null)
            return;
        if (isInPictureInPictureMode) {
            actionBar.hide();
            mControllerView.hide();
        } else {
            if (onStopCalled) {
                finish();
            } else {
                actionBar.show();
                mControllerView.show();
            }
        }
    }

    private boolean canVideoPause() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return !isInPictureInPictureMode();
        } else return true;
    }

    @Override
    protected void onPause() {
        if (canVideoPause()) {
            if (!doNotPause) {
                mPlayer.pause();
            }
            mControllerView.updatePausePlay();
        }
        super.onPause();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mPlayer.setSurfaceHolder(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return mPlayer.getBufferPercentage();
    }

    @Override
    public long getCurrentPosition() {
        return mPlayer.getCurrentPosition();
    }

    @Override
    public long getBufferPosition() {
        return mPlayer.getBufferPosition();
    }

    @Override
    public long getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public void pause() {
        mPlayer.pause();
    }

    @Override
    public void seekTo(long i) {
        mPlayer.seekTo(i);
    }

    @Override
    public void start() {
        mPlayer.play();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void commentClick() {
        Intent intent = new Intent(this, SwipebleActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        Commented commented = Commented.from(video);
        intent.putExtra(Extra.PLACE, PlaceFactory.getCommentsPlace(Settings.get().accounts().getCurrent(), commented, null));
        doNotPause = true;
        SwipebleActivity.applyIntent(intent);
        requestSwipeble.launch(intent);
    }

    @Override
    public void toggleFullScreen() {
        setRequestedOrientation(isLandscape ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    @SuppressWarnings("deprecation")
    private boolean hasPipPermission() {
        AppOpsManager appsOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return appsOps.unsafeCheckOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    getPackageName()
            ) == AppOpsManager.MODE_ALLOWED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return appsOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_PICTURE_IN_PICTURE,
                    android.os.Process.myUid(),
                    getPackageName()
            ) == AppOpsManager.MODE_ALLOWED;
        } else {
            return false;
        }
    }

    @Override
    public void toPIPScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
                    && hasPipPermission())
                if (!isInPictureInPictureMode()) {
                    Rational aspectRatio = new Rational(Frame.getWidth(), Frame.getHeight());
                    enterPictureInPictureMode(new PictureInPictureParams.Builder().setAspectRatio(aspectRatio).build());
                }
        }
    }

    private String getFileUrl() {
        switch (size) {
            case InternalVideoSize.SIZE_240:
                return video.getMp4link240();
            case InternalVideoSize.SIZE_360:
                return video.getMp4link360();
            case InternalVideoSize.SIZE_480:
                return video.getMp4link480();
            case InternalVideoSize.SIZE_720:
                return video.getMp4link720();
            case InternalVideoSize.SIZE_1080:
                return video.getMp4link1080();
            case InternalVideoSize.SIZE_HLS:
                return video.getHls();
            case InternalVideoSize.SIZE_LIVE:
                return video.getLive();
            default:
                finish();
                return "null";
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            isLandscape = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            isLandscape = false;
        }
    }

    @Override
    public void onVideoSizeChanged(@NonNull IVideoPlayer player, VideoSize size) {
        Frame.setAspectRatio(size.getWidth(), size.getHeight());
    }

    @Override
    public void hideMenu(boolean hide) {

    }

    @Override
    public void openMenu(boolean open) {

    }

    @Override
    public void setStatusbarColored(boolean colored, boolean invertIcons) {
        int statusbarNonColored = CurrentTheme.getStatusBarNonColored(this);
        int statusbarColored = CurrentTheme.getStatusBarColor(this);

        Window w = getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(colored ? statusbarColored : statusbarNonColored);
        @ColorInt
        int navigationColor = colored ? CurrentTheme.getNavigationBarColor(this) : Color.BLACK;
        w.setNavigationBarColor(navigationColor);

        if (Utils.hasMarshmallow()) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (invertIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        if (Utils.hasOreo()) {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            if (invertIcons) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
                w.setNavigationBarColor(Color.WHITE);
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                w.getDecorView().setSystemUiVisibility(flags);
            }
        }
    }
}
