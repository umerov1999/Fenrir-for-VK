package dev.ragnarok.fenrir.util;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.squareup.picasso3.Transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class ShortcutUtils {

    private static final String SHURTCUT_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";
    private static final int MAX_DYNAMIC_COUNT = 5;

    private static int getLauncherIconSize(Context context) {
        return ContextCompat.getDrawable(context, R.mipmap.ic_launcher).getIntrinsicWidth();
    }

    private static void createAccountShortcut(Context context, int accountId, String title, String url) throws IOException {
        Bitmap avatar = null;

        if (nonEmpty(url)) {
            int size = getLauncherIconSize(context);

            avatar = PicassoInstance.with()
                    .load(url)
                    .transform(new RoundTransformation())
                    .resize(size, size)
                    .get();
        }

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_SWITH_ACCOUNT);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);

        String id = "fenrir_account_" + accountId;

        sendShortcutBroadcast(context, id, intent, title, avatar);
    }

    public static Completable createAccountShortcutRx(Context context, int accountId, String title, String url) {
        return Completable.create(e -> {
            try {
                createAccountShortcut(context, accountId, title, url);
            } catch (Exception e1) {
                e.onError(e1);
            }
            e.onComplete();
        });
    }

    private static void createWallShortcut(Context context, int accountId, Owner owner) throws IOException {
        Bitmap avatar = null;

        if (nonEmpty(owner.getMaxSquareAvatar())) {
            int size = getLauncherIconSize(context);

            avatar = PicassoInstance.with()
                    .load(owner.getMaxSquareAvatar())
                    .transform(new RoundTransformation())
                    .resize(size, size)
                    .get();
        }

        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHORTCUT_WALL);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.OWNER_ID, owner.getOwnerId());

        String id = "fenrir_wall_" + owner.getOwnerId() + "_" + accountId;

        sendShortcutBroadcast(context, id, intent, owner.getFullName(), avatar);
    }

    public static Completable createWallShortcutRx(Context context, int accountId, Owner owner) {
        return Completable.create(e -> {
            try {
                createWallShortcut(context, accountId, owner);
            } catch (Exception e1) {
                e.onError(e1);
            }
            e.onComplete();
        });
    }

    private static void sendShortcutBroadcast(Context context, String shortcutId, Intent shortcutIntent, String title, Bitmap bitmap) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Icon icon = Icon.createWithBitmap(bitmap);

            ShortcutInfo shortcutInfo = new ShortcutInfo.Builder(context, shortcutId)
                    .setIcon(icon)
                    .setShortLabel(title)
                    .setIntent(shortcutIntent)
                    .build();

            ShortcutManager manager = context.getSystemService(ShortcutManager.class);
            if (manager != null) {
                manager.requestPinShortcut(shortcutInfo, null);
            }
        } else {
            Intent intent = new Intent();
            intent.setAction(SHURTCUT_ACTION);
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
            context.sendBroadcast(intent);
        }
    }

    public static Intent chatOpenIntent(Context context, String url, int accountId, int peerId, String title) {
        Intent intent = new Intent(context.getApplicationContext(), MainActivity.class);
        intent.setAction(MainActivity.ACTION_CHAT_FROM_SHORTCUT);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.TITLE, title);
        intent.putExtra(Extra.IMAGE, url);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        return intent;
    }

    private static void createChatShortcut(Context context, String url, int accountId, int peerId, String title) throws IOException {
        String id = "fenrir_peer_" + peerId + "_aid_" + accountId;

        Bitmap bm = createBitmap(context, url);
        Intent intent = chatOpenIntent(context, url, accountId, peerId, title);
        sendShortcutBroadcast(context, id, intent, title, bm);
    }

    public static Completable createChatShortcutRx(Context context, String url, int accountId, int peerId, String title) {
        return Completable.create(e -> {
            try {
                createChatShortcut(context, url, accountId, peerId, title);
            } catch (Exception e1) {
                e.onError(e1);
            }
            e.onComplete();
        });
    }

    private static Single<Bitmap> loadRoundAvatar(String url) {
        return Single.fromCallable(() -> PicassoInstance.with()
                .load(url)
                .transform(new RoundTransformation())
                .get());
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static Completable addDynamicShortcut(Context context, int accountId, Peer peer) {
        Context app = context.getApplicationContext();

        return loadRoundAvatar(peer.getAvaUrl())
                .flatMapCompletable(bitmap -> Completable.fromAction(() -> {
                    ShortcutManager manager = app.getSystemService(ShortcutManager.class);
                    List<ShortcutInfo> infos = new ArrayList<>(manager.getDynamicShortcuts());

                    List<String> mustBeRemoved = new ArrayList<>(1);

                    if (infos.size() >= MAX_DYNAMIC_COUNT) {
                        Collections.sort(infos, (o1, o2) -> Integer.compare(o1.getRank(), o2.getRank()));

                        ShortcutInfo infoWhichMustBeRemoved = infos.get(infos.size() - 1);
                        mustBeRemoved.add(infoWhichMustBeRemoved.getId());
                    }

                    String title = peer.getTitle();
                    String id = "fenrir_peer_" + peer.getId() + "_aid_" + accountId;
                    String avaurl = peer.getAvaUrl();
                    Intent intent = chatOpenIntent(app, avaurl, accountId, peer.getId(), title);

                    int rank = 0;

                    ShortcutInfo.Builder builder = new ShortcutInfo.Builder(app, id)
                            .setShortLabel(title)
                            .setIntent(intent)
                            .setRank(rank);

                    Icon icon = Icon.createWithBitmap(bitmap);
                    builder.setIcon(icon);

                    if (!mustBeRemoved.isEmpty()) {
                        manager.removeDynamicShortcuts(mustBeRemoved);
                    }

                    manager.addDynamicShortcuts(Collections.singletonList(builder.build()));
                }));
    }

    private static Bitmap createBitmap(Context mContext, String url) throws IOException {
        int appIconSize = getLauncherIconSize(mContext);
        Bitmap bm;

        Transformation transformation = new RoundTransformation();

        if (isEmpty(url)) {
            bm = PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(transformation)
                    .resize(appIconSize, appIconSize)
                    .get();
        } else {
            bm = PicassoInstance.with()
                    .load(url)
                    .transform(transformation)
                    .resize(appIconSize, appIconSize)
                    .get();
        }

        return bm;
    }
}
