package dev.ragnarok.fenrir.activity;

import static dev.ragnarok.fenrir.util.RxUtils.ignore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.ICaptchaProvider;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemeOverlay;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class CaptchaActivity extends AppCompatActivity {

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private TextInputEditText mTextField;
    private ICaptchaProvider captchaProvider;

    private String requestSid;

    public static Intent createIntent(@NonNull Context context, String captchaSid, String captchaImg) {
        return new Intent(context, CaptchaActivity.class)
                .putExtra(Extra.CAPTCHA_SID, captchaSid)
                .putExtra(Extra.CAPTCHA_URL, captchaImg);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Utils.updateActivityContext(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        @StyleRes int theme;
        switch (Settings.get().main().getThemeOverlay()) {
            case ThemeOverlay.AMOLED:
                theme = R.style.QuickReply_Amoled;
                break;
            case ThemeOverlay.MD1:
                theme = R.style.QuickReply_MD1;
                break;
            case ThemeOverlay.OFF:
            default:
                theme = R.style.QuickReply;
        }
        setTheme(theme);
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);

        setContentView(R.layout.activity_captcha);

        ImageView imageView = findViewById(R.id.captcha_view);
        mTextField = findViewById(R.id.captcha_text);

        String image = getIntent().getStringExtra(Extra.CAPTCHA_URL);

        //onSuccess, w: 130, h: 50
        PicassoInstance.with()
                .load(image)
                .into(imageView);

        findViewById(R.id.button_cancel).setOnClickListener(v -> cancel());
        findViewById(R.id.button_ok).setOnClickListener(v -> onOkButtonClick());

        requestSid = getIntent().getStringExtra(Extra.CAPTCHA_SID);

        captchaProvider = Injection.provideCaptchaProvider();

        mCompositeDisposable.add(captchaProvider.observeWaiting()
                .filter(sid -> sid.equals(requestSid))
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(rid -> onWaitingRequestRecieved(), ignore()));

        mCompositeDisposable.add(captchaProvider.observeCanceling()
                .filter(sid -> sid.equals(requestSid))
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(integer -> onRequestCancelled(), ignore()));
    }

    private void cancel() {
        captchaProvider.cancel(requestSid);
        finish();
    }

    private void onRequestCancelled() {
        finish();
    }

    private void onWaitingRequestRecieved() {
        captchaProvider.notifyThatCaptchaEntryActive(requestSid);
    }

    @Override
    protected void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    private void onOkButtonClick() {
        CharSequence text = mTextField.getText();
        if (TextUtils.isEmpty(text)) {
            Utils.showRedTopToast(this, getString(R.string.enter_captcha_text));
            return;
        }

        captchaProvider.enterCode(requestSid, text.toString());

        finish();
    }
}
