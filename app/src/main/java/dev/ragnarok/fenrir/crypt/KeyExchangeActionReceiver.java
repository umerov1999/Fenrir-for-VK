package dev.ragnarok.fenrir.crypt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.util.Objects;

public class KeyExchangeActionReceiver extends BroadcastReceiver {

    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = Objects.nonNull(intent) ? intent.getAction() : null;
        if (KeyExchangeService.WHAT_SESSION_STATE_CHANGED.equals(action)) {
            @SessionState
            int state = intent.getExtras().getInt(Extra.STATUS);
            onSessionStateChanged(intent.getExtras().getInt(Extra.ACCOUNT_ID),
                    intent.getExtras().getInt(Extra.PEER_ID),
                    intent.getExtras().getLong(Extra.SESSION_ID),
                    state);
        }
    }

    public void register(@NonNull Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(KeyExchangeService.WHAT_SESSION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(context).registerReceiver(this, filter);
    }

    public void unregister(@NonNull Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    protected void onSessionStateChanged(int accountId, int peerId, long sessionId, @SessionState int sessionState) {

    }
}
