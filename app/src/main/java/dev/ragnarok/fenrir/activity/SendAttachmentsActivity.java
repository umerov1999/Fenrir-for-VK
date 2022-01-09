package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.MainActivityTransforms;
import dev.ragnarok.fenrir.util.ViewUtils;

/**
 * Тот же MainActivity, предназначенный для шаринга контента
 * Отличие только в том, что этот активити может существовать в нескольких экземплярах
 */
public class SendAttachmentsActivity extends MainActivity {

    public static void startForSendAttachments(Context context, int accountId, ModelsBundle bundle) {
        Intent intent = new Intent(context, SendAttachmentsActivity.class);
        intent.setAction(ACTION_SEND_ATTACHMENTS);
        intent.putExtra(EXTRA_INPUT_ATTACHMENTS, bundle);
        intent.putExtra(MainActivity.EXTRA_NO_REQUIRE_PIN, true);
        intent.putExtra(Extra.PLACE, PlaceFactory.getDialogsPlace(accountId, accountId, null));
        context.startActivity(intent);
    }

    public static void startForSendAttachmentsFor(Context context, int accountId, Peer peer, ModelsBundle bundle) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.setAction(ChatActivity.ACTION_OPEN_PLACE);
        intent.putExtra(EXTRA_INPUT_ATTACHMENTS, bundle);
        intent.putExtra(Extra.PLACE, PlaceFactory.getChatPlace(accountId, accountId, peer));
        context.startActivity(intent);
    }

    public static void startForSendLink(Context context, String link) {
        Intent intent = new Intent(context, SendAttachmentsActivity.class);
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, link);
        context.startActivity(intent);
    }

    public static void startForSendAttachments(@NonNull Context context, int accountId, AbsModel model) {
        startForSendAttachments(context, accountId, new ModelsBundle(1).append(model));
    }

    public static void startForSendAttachmentsFor(@NonNull Context context, int accountId, Peer peer, AbsModel model) {
        startForSendAttachmentsFor(context, accountId, peer, new ModelsBundle(1).append(model));
    }

    @Override
    protected @MainActivityTransforms
    int getMainActivityTransform() {
        return MainActivityTransforms.SEND_ATTACHMENTS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // потому, что в onBackPressed к этому числу будут прибавлять 2000 !!!! и выход за границы
        mLastBackPressedTime = Long.MAX_VALUE - DOUBLE_BACK_PRESSED_TIMEOUT;
    }

    @Override
    public void onDestroy() {
        ViewUtils.keyboardHide(this);
        super.onDestroy();
    }
}