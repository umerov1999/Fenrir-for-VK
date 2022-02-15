package dev.ragnarok.fenrir.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.material.button.MaterialButton;
import com.google.common.io.CharStreams;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.filepicker.model.DialogConfigs;
import dev.ragnarok.fenrir.filepicker.model.DialogProperties;
import dev.ragnarok.fenrir.filepicker.view.FilePickerDialog;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.module.rlottie.RLottie2Gif;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemesController;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;


public class LottieActivity extends AppCompatActivity {

    private static final String REQUEST_EXPORT_GIF = "export_gif";
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissionsActivity(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            this::startExportGif);
    private RLottieImageView lottie;
    private TextView lg;

    private void log(String log) {
        lg.post(() -> lg.setText(log.trim()));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Utils.updateActivityContext(newBase));
    }

    private void startExportGif() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.DIR_SELECT;
        properties.root = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.error_dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.offset = Environment.getExternalStorageDirectory().getAbsolutePath();
        properties.extensions = null;
        properties.show_hidden_files = true;
        properties.tittle = R.string.export_accounts;
        properties.request = REQUEST_EXPORT_GIF;
        FilePickerDialog.newInstance(properties).show(getSupportFragmentManager(), "ExportGif");
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
            if (!AppPerms.hasReadWriteStoragePermission(this)) {
                requestWritePermission.launch();
                return;
            }
            startExportGif();
        });

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(CurrentTheme.getStatusBarColor(this));
        w.setNavigationBarColor(CurrentTheme.getNavigationBarColor(this));

        if (Objects.isNull(savedInstanceState)) {
            handleIntent(getIntent());
        }

        getSupportFragmentManager().setFragmentResultListener(REQUEST_EXPORT_GIF, this, (requestKey, result) -> {
            DocumentFile v = DocumentFile.fromSingleUri(this, getIntent().getData());
            String title;
            if (v == null || Utils.isEmpty(v.getName())) {
                title = "converted.gif";
            } else {
                title = v.getName() + ".gif";
            }
            File file = new File(result.getStringArray(FilePickerDialog.RESULT_VALUE)[0], title);
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
                                    "Path : " + file + "\r\n" +
                                    "File Size : " + file.length() / 1024 + "kb";
                            log(logs);
                            toGif.post(() -> toGif.setEnabled(true));
                            lottie.post(() -> lottie.playAnimation());
                        }
                    })
                    .setBackgroundColor(Color.TRANSPARENT)
                    .setOutputPath(file)
                    .setSize(500, 500)
                    .setBackgroundTask(true)
                    .setDithering(false)
                    .build();
        });
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
                InputStreamReader b = new InputStreamReader(getContentResolver().openInputStream(getIntent().getData()), StandardCharsets.UTF_8);
                lottie.setAutoRepeat(true);
                String result = CharStreams.toString(b);
                lottie.fromString(result, Utils.dp(500), Utils.dp(500));
                lottie.playAnimation();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
