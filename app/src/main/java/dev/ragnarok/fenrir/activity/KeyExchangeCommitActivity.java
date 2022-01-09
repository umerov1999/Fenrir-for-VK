package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.crypt.ExchangeMessage;
import dev.ragnarok.fenrir.crypt.KeyExchangeService;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemeOverlay;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class KeyExchangeCommitActivity extends AppCompatActivity {

    public static Intent createIntent(@NonNull Context context, int accountId, int peerId, @NonNull User user, int messageId, @NonNull ExchangeMessage message) {
        Intent intent = new Intent(context, KeyExchangeCommitActivity.class);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.OWNER, user);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.MESSAGE_ID, messageId);
        intent.putExtra(Extra.MESSAGE, message);
        return intent;
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

        setContentView(R.layout.activity_key_exchange_commit);

        int accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
        int peerId = getIntent().getExtras().getInt(Extra.PEER_ID);

        User user = getIntent().getParcelableExtra(Extra.OWNER);

        int messageId = getIntent().getExtras().getInt(Extra.MESSAGE_ID);
        ExchangeMessage message = getIntent().getParcelableExtra(Extra.MESSAGE);

        ImageView avatar = findViewById(R.id.avatar);
        ViewUtils.displayAvatar(avatar, CurrentTheme.createTransformationForAvatar(), user.getMaxSquareAvatar(), null);

        TextView userName = findViewById(R.id.user_name);
        userName.setText(user.getFullName());

        findViewById(R.id.accept_button).setOnClickListener(v -> {
            startService(KeyExchangeService.createIntentForApply(this, message, accountId, peerId, messageId));
            finish();
        });

        findViewById(R.id.decline_button).setOnClickListener(v -> {
            startService(KeyExchangeService.createIntentForDecline(this, message, accountId, peerId, messageId));
            finish();
        });
    }
}
