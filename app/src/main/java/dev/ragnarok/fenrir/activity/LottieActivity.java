package dev.ragnarok.fenrir.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.io.File;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.rlottie.RLottie2Gif;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;


public class LottieActivity extends AppCompatActivity {

    private RLottieImageView lottie;
    private String file;
    private TextView lg;

    private void log(String log) {
        lg.post(() -> lg.setText(log.trim()));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Utils.updateActivityContext(newBase));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(ThemesController.INSTANCE.currentStyle());
        Utils.prepareDensity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lottie);

        lottie = findViewById(R.id.lottie_preview);
        lg = findViewById(R.id.log_tag);
        MaterialButton toGif = findViewById(R.id.lottie_to_gif);
        toGif.setOnClickListener(v -> {
            if (!FenrirNative.isNativeLoaded()) {
                return;
            }
            toGif.setEnabled(false);
            lottie.stopAnimation();
            RLottie2Gif.create(lottie.getAnimatedDrawable())
                    .setListener(new RLottie2Gif.Lottie2GifListener() {
                        long start;
                        String logs;

                        @Override
                        public void onStarted() {
                            start = System.currentTimeMillis();
                            logs = "Wait a moment...\r\n";
                            log(logs);
                        }

                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onProgress(int frame, int totalFrame) {
                            log(logs + "progress : " + frame + "/" + totalFrame);
                        }

                        @Override
                        public void onFinished() {
                            logs = "GIF created (" + (System.currentTimeMillis() - start) + "ms)\r\n" +
                                    "Resolution : " + 500 + "x" + 500 + "\r\n" +
                                    "Path : " + file + ".gif" + "\r\n" +
                                    "File Size : " + (new File(file + ".gif").length() / 1024) + "kb";
                            log(logs);
                            toGif.post(() -> toGif.setEnabled(true));
                            lottie.post(() -> lottie.playAnimation());
                        }
                    })
                    .setBackgroundColor(Color.TRANSPARENT)
                    .setOutputPath(file + ".gif")
                    .setSize(500, 500)
                    .setBackgroundTask(true)
                    .setDithering(false)
                    .build();
        });

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(CurrentTheme.getStatusBarColor(this));
        w.setNavigationBarColor(CurrentTheme.getNavigationBarColor(this));

        if (Objects.isNull(savedInstanceState)) {
            handleIntent(getIntent());
        }
    }

    private String findFileName(Uri uri) {
        String fileName = uri.getLastPathSegment();
        try {
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                fileName = uri.getPath();
            } else if (scheme.equals("content")) {
                String[] proj = {MediaStore.MediaColumns.DATA};
                Cursor cursor = getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        } catch (Exception ignored) {
            finish();
        }
        return fileName;
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            finish();
            return;
        }
        int accountId = Settings.get().accounts().getCurrent();
        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            finish();
            return;
        }
        String action = intent.getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            try {
                file = findFileName(getIntent().getData());
                lottie.setAutoRepeat(true);
                lottie.fromFile(new File(file), Utils.dp(500), Utils.dp(500));
                lottie.playAnimation();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
