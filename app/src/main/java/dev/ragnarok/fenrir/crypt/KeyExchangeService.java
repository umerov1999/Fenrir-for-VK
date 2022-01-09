package dev.ragnarok.fenrir.crypt;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.LongSparseArray;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.KeyExchangeCommitActivity;
import dev.ragnarok.fenrir.api.Apis;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.crypt.ver.Version;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.push.OwnerInfo;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Unixtime;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class KeyExchangeService extends Service {

    public static final String ACTION_APPLY_EXHANGE = "ACTION_APPLY_EXHANGE";
    public static final String ACTION_DECLINE = "ACTION_DECLINE";
    static final String WHAT_SESSION_STATE_CHANGED = "WHAT_SESSION_STATE_CHANGED";
    private static final String TAG = KeyExchangeService.class.getSimpleName();
    private static final String EXTRA_KEY_LOCATION_POLICY = "key_location_policy";
    private static final String ACTION_PROCESS_MESSAGE = "ACTION_PROCESS_MESSAGE";
    private static final String ACTION_INICIATE_KEY_EXCHANGE = "ACTION_INICIATE_KEY_EXCHANGE";
    private static final int NOTIFICATION_KEY_EXCHANGE = 20;
    private static final int NOTIFICATION_KEY_EXCHANGE_REQUEST = 10;
    private static final int WHAT_STOP_SERVICE = 12;
    private final ISessionIdGenerator mSessionIdGenerator = new FirebaseSessionIdGenerator();
    private final CompositeDisposable mCompositeSubscription = new CompositeDisposable();
    private LongSparseArray<KeyExchangeSession> mCurrentActiveSessions;
    private LongSparseArray<NotificationCompat.Builder> mCurrentActiveNotifications;
    private Set<Long> mFinishedSessionsIds;
    private NotificationManager mNotificationManager;
    private final Handler mStopServiceHandler = new Handler(Looper.getMainLooper(), msg -> {
        if (msg.what == WHAT_STOP_SERVICE) {
            finishAllByTimeout();
            stopSelf();
        }
        return false;
    });

    public static boolean intercept(@NonNull Context context, int accountId, VKApiMessage dto) {
        if (isNull(dto)) {
            return false;
        }
        return intercept(context, accountId, dto.peer_id, dto.id, dto.body, dto.out);
    }

    public static boolean intercept(@NonNull Context context, int accountId, int peerId, int messageId, String messageBody, boolean out) {
        @MessageType
        int type = CryptHelper.analizeMessageBody(messageBody);

        if (type == MessageType.KEY_EXCHANGE) {
            try {
                String exchangeMessageBody = messageBody.substring(3); // without RSA on start
                ExchangeMessage message = new Gson().fromJson(exchangeMessageBody, ExchangeMessage.class);
                if (!out) {
                    processMessage(context, accountId, peerId, messageId, message);
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        return false;
    }

    private static void assertSessionState(@NonNull KeyExchangeSession session, @SessionState int requiredState) throws InvalidSessionStateException {
        if (session.getLocalSessionState() != requiredState) {
            throw new InvalidSessionStateException("Invalid session state, require: " + requiredState + ", existing: " + session.getLocalSessionState());
        }
    }

    private static Single<Integer> sendMessageImpl(int accountId, int peerId, @NonNull ExchangeMessage message) {
        return Single.just(new Object())
                .delay(1, TimeUnit.SECONDS)
                .flatMap(o -> Apis.get()
                        .vkDefault(accountId)
                        .messages()
                        .send(new Random().nextInt(), peerId, null, message.toString(), null, null, null, null, null, null, null));
    }

    public static void iniciateKeyExchangeSession(@NonNull Context context, int accountId,
                                                  int peerId, @KeyLocationPolicy int policy) {
        Intent intent = new Intent(context, KeyExchangeService.class);
        intent.setAction(ACTION_INICIATE_KEY_EXCHANGE);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(EXTRA_KEY_LOCATION_POLICY, policy);
        context.startService(intent);
    }

    public static void processMessage(@NonNull Context context, int accountId, int peerId, int messageId, @NonNull ExchangeMessage message) {
        Intent intent = new Intent(context, KeyExchangeService.class);
        intent.setAction(ACTION_PROCESS_MESSAGE);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.MESSAGE_ID, messageId);
        intent.putExtra(Extra.MESSAGE, message);
        context.startService(intent);
    }

    public static Intent createIntentForApply(@NonNull Context context, @NonNull ExchangeMessage message, int accountId, int peerId, int messageId) {
        Intent apply = new Intent(context, KeyExchangeService.class);
        apply.setAction(ACTION_APPLY_EXHANGE);
        apply.putExtra(Extra.ACCOUNT_ID, accountId);
        apply.putExtra(Extra.PEER_ID, peerId);
        apply.putExtra(Extra.MESSAGE_ID, messageId);
        apply.putExtra(Extra.MESSAGE, message);
        return apply;
    }

    public static Intent createIntentForDecline(@NonNull Context context, @NonNull ExchangeMessage message, int accountId, int peerId, int messageId) {
        Intent intent = new Intent(context, KeyExchangeService.class);
        intent.setAction(ACTION_DECLINE);
        intent.putExtra(Extra.MESSAGE, message);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        intent.putExtra(Extra.MESSAGE_ID, messageId);
        return intent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mCurrentActiveSessions = new LongSparseArray<>(1);
        mCurrentActiveNotifications = new LongSparseArray<>(1);
        mFinishedSessionsIds = new HashSet<>(1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = nonNull(intent) ? intent.getAction() : null;

        if (ACTION_PROCESS_MESSAGE.equals(action)) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            int messageId = intent.getExtras().getInt(Extra.MESSAGE_ID);
            ExchangeMessage message = intent.getParcelableExtra(Extra.MESSAGE);
            processNewKeyExchangeMessage(accountId, peerId, messageId, message);
        } else if (ACTION_INICIATE_KEY_EXCHANGE.equals(action)) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            @KeyLocationPolicy
            int keyLocationPolicy = intent.getExtras().getInt(EXTRA_KEY_LOCATION_POLICY);
            iniciateKeyExchange(accountId, peerId, keyLocationPolicy);
        } else if (ACTION_APPLY_EXHANGE.equals(action)) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            int messageId = intent.getExtras().getInt(Extra.MESSAGE_ID);
            ExchangeMessage message = intent.getParcelableExtra(Extra.MESSAGE);
            mNotificationManager.cancel(String.valueOf(message.getSessionId()), NOTIFICATION_KEY_EXCHANGE_REQUEST);
            processKeyExchangeMessage(accountId, peerId, messageId, message, false);
        } else if (ACTION_DECLINE.equals(action)) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            ExchangeMessage message = intent.getParcelableExtra(Extra.MESSAGE);
            declineInputSession(accountId, peerId, message);
        }

        toggleServiceLiveHandler();
        return START_NOT_STICKY;
    }

    private void declineInputSession(int accounId, int peerId, @NonNull ExchangeMessage message) {
        notifyOpponentAboutSessionFail(accounId, peerId, message.getSessionId(), ErrorCodes.CANCELED_BY_USER);
    }

    private void processNewKeyExchangeMessage(int accountId, int peerId, int messageId, @NonNull ExchangeMessage message) {
        @SessionState
        int opponentSessionState = message.getSenderSessionState();

        switch (opponentSessionState) {
            case SessionState.NO_INITIATOR_STATE_1:
            case SessionState.NO_INITIATOR_FINISHED:
            case SessionState.INITIATOR_STATE_1:
            case SessionState.INITIATOR_STATE_2:
            case SessionState.INITIATOR_FINISHED:
                processKeyExchangeMessage(accountId, peerId, messageId, message, true);
                break;
            case SessionState.INITIATOR_EMPTY:
            case SessionState.NO_INITIATOR_EMPTY:
                throw new IllegalStateException("Invalid session state");
            case SessionState.FAILED:
                onReceiveSessionFailStatus(message);
                break;
            case SessionState.CLOSED:
                break;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Nullable
    private KeyExchangeSession findSessionFor(int accountId, int peerId) {
        for (int i = 0; i < mCurrentActiveSessions.size(); i++) {
            long key = mCurrentActiveSessions.keyAt(i);
            KeyExchangeSession session = mCurrentActiveSessions.get(key);
            if (session.getAccountId() == accountId && session.getPeerId() == peerId) {
                return session;
            }
        }

        return null;
    }

    private void registerSession(@NonNull KeyExchangeSession session) {
        mCurrentActiveSessions.put(session.getId(), session);
    }

    private void iniciateKeyExchange(int accountId, int peerId, @KeyLocationPolicy int keyLocationPolicy) {
        KeyExchangeSession existsSession = findSessionFor(accountId, peerId);

        if (nonNull(existsSession)) {
            Toast.makeText(this, R.string.session_already_created, Toast.LENGTH_LONG).show();
            return;
        }

        mSessionIdGenerator.generateNextId()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(nextSessionId -> {
                    KeyExchangeSession session = KeyExchangeSession.createOutSession(nextSessionId, accountId, peerId, keyLocationPolicy);
                    session.setOppenentSessionState(SessionState.NO_INITIATOR_EMPTY);
                    registerSession(session);

                    notifyAboutKeyExchangeAsync(accountId, peerId, session.getId());

                    fireSessionStateChanged(session);

                    try {
                        KeyPair pair = CryptHelper.generateRsaKeyPair(Version.ofCurrent().getRsaKeySize());
                        session.setMyPrivateKey(pair.getPrivate());

                        byte[] encodedPublicKey = pair.getPublic().getEncoded();
                        String pulicBase64 = Base64.encodeToString(encodedPublicKey, Base64.DEFAULT);

                        ExchangeMessage message = new ExchangeMessage.Builder(Version.getCurrentVersion(), session.getId(), SessionState.INITIATOR_STATE_1)
                                .setPublicKey(pulicBase64)
                                .setKeyLocationPolicy(keyLocationPolicy)
                                .create();

                        sendMessage(accountId, peerId, message);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }, t -> {
                    // TODO: 01.08.2017
                });
    }

    private void onReceiveSessionFailStatus(@NonNull ExchangeMessage message) {
        Logger.d(TAG, "onReceiveSessionFailStatus, message: " + message);

        if (mFinishedSessionsIds.contains(message.getSessionId())) {
            Logger.wtf(TAG, "onReceiveSessionFailStatus, session already finished");
            return;
        }

        KeyExchangeSession session = mCurrentActiveSessions.get(message.getSessionId());
        finishSessionByOpponentFail(session, message);
    }

    private void sendSessionStateChangeBroadcast(@NonNull KeyExchangeSession session) {
        Intent intent = new Intent(WHAT_SESSION_STATE_CHANGED);
        intent.putExtra(Extra.ACCOUNT_ID, session.getAccountId());
        intent.putExtra(Extra.PEER_ID, session.getPeerId());
        intent.putExtra(Extra.SESSION_ID, session.getId());
        intent.putExtra(Extra.STATUS, session.getLocalSessionState());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void notifyOpponentAboutSessionFail(int accountId, int peerId, long sessionId, int errorCode) {
        Logger.d(TAG, "notifyOpponentAboutSessionFail, sessionId: " + sessionId);
        ExchangeMessage message = new ExchangeMessage.Builder(Version.getCurrentVersion(), sessionId, SessionState.FAILED)
                .setErrorCode(errorCode)
                .create();
        sendMessage(accountId, peerId, message);
    }

    private void displayUserConfirmNotification(int accountId, int peerId, int messageId, @NonNull ExchangeMessage message) {
        mCompositeSubscription.add(OwnerInfo.getRx(this, accountId, Peer.toUserId(peerId))
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(userInfo -> displayUserConfirmNotificationImpl(accountId, peerId, messageId, message, userInfo), throwable -> {/*ignore*/}));
    }

    @Override
    public void onDestroy() {
        mCompositeSubscription.dispose();
        super.onDestroy();
    }

    private NotificationCompat.Builder findBuilder(long sessionId) {
        return mCurrentActiveNotifications.get(sessionId);
    }

    private void notifyAboutKeyExchangeAsync(int accountId, int peerId, long sessionId) {
        mCompositeSubscription.add(OwnerInfo.getRx(this, accountId, Peer.toUserId(peerId))
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(userInfo -> notifyAboutKeyExchange(sessionId, userInfo), throwable -> {/*ignore*/}));
    }

    private void notifyAboutKeyExchange(long sessionId, @NonNull OwnerInfo info) {
        KeyExchangeSession session = mCurrentActiveSessions.get(sessionId);
        if (isNull(session)) {
            //сессия уже неактивна
            return;
        }

        String targetContentText = getString(R.string.key_exchange_content_text, info.getUser().getFullName());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppNotificationChannels.KEY_EXCHANGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_crypt_key_vector)
                .setLargeIcon(info.getAvatar())
                .setContentTitle(getString(R.string.key_exchange))
                .setContentText(targetContentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(targetContentText))
                .setAutoCancel(true);

        mCurrentActiveNotifications.put(sessionId, builder);
        refreshSessionNotification(session);
    }

    private void displayUserConfirmNotificationImpl(int accountId, int peerId, int messageId, @NonNull ExchangeMessage message, @NonNull OwnerInfo ownerInfo) {
        if (Utils.hasOreo()) {
            mNotificationManager.createNotificationChannel(AppNotificationChannels.getKeyExchangeChannel(this));
        }

        String targetContentText = getString(R.string.key_exchange_request_content_text, ownerInfo.getUser().getFullName());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, AppNotificationChannels.KEY_EXCHANGE_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_crypt_key_vector)
                .setLargeIcon(ownerInfo.getAvatar())
                .setContentTitle(getString(R.string.key_exchange_request))
                .setContentText(targetContentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(targetContentText))
                .setAutoCancel(true);

        Intent intent = KeyExchangeCommitActivity.createIntent(this, accountId, peerId, ownerInfo.getUser(), messageId, message);
        PendingIntent contentIntent = PendingIntent.getActivity(this, messageId, intent, Utils.makeMutablePendingIntent(PendingIntent.FLAG_CANCEL_CURRENT));
        builder.setContentIntent(contentIntent);

        Intent apply = createIntentForApply(this, message, accountId, peerId, messageId);
        PendingIntent quickPendingIntent = PendingIntent.getService(this, messageId, apply, Utils.makeMutablePendingIntent(PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationCompat.Action applyAction = new NotificationCompat.Action(R.drawable.check, getString(R.string.apply), quickPendingIntent);

        builder.addAction(applyAction);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);

        Notification notification = builder.build();
        mNotificationManager.notify(String.valueOf(message.getSessionId()), NOTIFICATION_KEY_EXCHANGE_REQUEST, notification);
    }

    private void processKeyExchangeMessage(int accountId, int peerId, int messageId,
                                           @NonNull ExchangeMessage message, boolean needConfirmIfSessionNotStarted) {
        KeyExchangeSession session = mCurrentActiveSessions.get(message.getSessionId());

        if (nonNull(session) && session.isMessageProcessed(messageId)) {
            Logger.d(TAG, "This message was processed, id: " + messageId);
            return;
        }

        if (mFinishedSessionsIds.contains(message.getSessionId())) {
            Logger.d(TAG, "This session was CLOSED, mFinishedSessionsIds contains session_id");
            return;
        }

        if (nonNull(session) && session.getLocalSessionState() == SessionState.CLOSED) {
            Logger.d(TAG, "This session was CLOSED, mCurrentActiveSessions array contains session");
            return;
        }

        @SessionState
        int opponentSessionState = message.getSenderSessionState();

        if (isNull(session)) {
            if (opponentSessionState != SessionState.INITIATOR_STATE_1) {
                notifyOpponentAboutSessionFail(accountId, peerId, message.getSessionId(), ErrorCodes.SESSION_EXPIRED);
                return;
            }

            if (needConfirmIfSessionNotStarted) {
                displayUserConfirmNotification(accountId, peerId, messageId, message);
                return;
            }

            session = KeyExchangeSession.createInputSession(message.getSessionId(), accountId,
                    peerId, message.getKeyLocationPolicy());

            mCurrentActiveSessions.put(message.getSessionId(), session);

            notifyAboutKeyExchangeAsync(accountId, peerId, session.getId());
        }

        session.appendMessageId(messageId);
        session.setOppenentSessionState(opponentSessionState);

        fireSessionStateChanged(session);

        Logger.d(TAG, "processKeyExchangeMessage, opponentSessionState: " + opponentSessionState);

        try {
            switch (opponentSessionState) {
                case SessionState.INITIATOR_STATE_1:
                    assertSessionState(session, SessionState.NO_INITIATOR_EMPTY);
                    processNoIniciatorEmptyState(accountId, peerId, session, message);
                    break;
                case SessionState.NO_INITIATOR_STATE_1:
                    assertSessionState(session, SessionState.INITIATOR_STATE_1);
                    processIniciatorState1(accountId, peerId, session, message);
                    break;
                case SessionState.INITIATOR_STATE_2:
                    assertSessionState(session, SessionState.NO_INITIATOR_STATE_1);
                    processNoIniciatorState1(accountId, peerId, session, message);
                    break;
                case SessionState.NO_INITIATOR_FINISHED:
                    assertSessionState(session, SessionState.INITIATOR_STATE_2);
                    processIniciatorState2(accountId, peerId, session, message);
                    break;
                case SessionState.INITIATOR_FINISHED:
                    assertSessionState(session, SessionState.NO_INITIATOR_FINISHED);
                    processNoIniciatorFinished(accountId, peerId, session, message);
                    break;
                case SessionState.CLOSED:
                case SessionState.FAILED:
                case SessionState.INITIATOR_EMPTY:
                case SessionState.NO_INITIATOR_EMPTY:
                    break;
            }
        } catch (InvalidSessionStateException e) {
            e.printStackTrace();

            notifyOpponentAboutSessionFail(accountId, peerId, session.getId(), ErrorCodes.INVALID_SESSION_STATE);
            finishSession(session, true);
        }
    }

    private void processNoIniciatorFinished(int accountId, int peerId, @NonNull KeyExchangeSession session, @NonNull ExchangeMessage message) {
        storeKeyToDatabase(accountId, peerId, session);
        finishSession(session, false);
    }

    private void finishSessionByOpponentFail(@NonNull KeyExchangeSession session, @NonNull ExchangeMessage message) {
        session.setLocalSessionState(SessionState.CLOSED);

        mCurrentActiveSessions.remove(session.getId());
        mFinishedSessionsIds.add(session.getId());

        mCurrentActiveNotifications.remove(session.getId());
        mNotificationManager.cancel(String.valueOf(session.getId()), NOTIFICATION_KEY_EXCHANGE);

        showError(localizeError(message.getErrorCode()));

        Logger.d(TAG, "Session was released by opponent, id: " + session.getId() + ", error_code: " + message.getErrorCode());

        toggleServiceLiveHandler();
    }

    private void finishSession(@NonNull KeyExchangeSession session, boolean withError) {
        session.setLocalSessionState(SessionState.CLOSED);

        mCurrentActiveSessions.remove(session.getId());
        mFinishedSessionsIds.add(session.getId());

        mCurrentActiveNotifications.remove(session.getId());
        mNotificationManager.cancel(String.valueOf(session.getId()), NOTIFICATION_KEY_EXCHANGE);

        if (withError) {
            showError(getString(R.string.key_exchange_failed));
        } else {
            Toast.makeText(this, R.string.you_have_successfully_exchanged_keys, Toast.LENGTH_LONG).show();
        }

        Logger.d(TAG, "Session was released, id: " + session.getId() + ", withError: " + withError);

        toggleServiceLiveHandler();
    }

    private String localizeError(int code) {
        switch (code) {
            case ErrorCodes.INVALID_SESSION_STATE:
                return getString(R.string.error_key_exchange_invalid_session_state);
            case ErrorCodes.CANCELED_BY_USER:
                return getString(R.string.error_key_exchange_cancelled_by_user);
            case ErrorCodes.SESSION_EXPIRED:
                return getString(R.string.error_key_exchange_session_expired);
            default:
                return getString(R.string.key_exchange_failed);
        }
    }

    private void showError(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private void finishAllByTimeout() {
        for (int i = 0; i < mCurrentActiveSessions.size(); i++) {
            long id = mCurrentActiveSessions.keyAt(i);

            KeyExchangeSession session = mCurrentActiveSessions.get(id);
            if (nonNull(session)) {
                finishSession(session, true);
            }
        }
    }

    private void toggleServiceLiveHandler() {
        mStopServiceHandler.removeMessages(WHAT_STOP_SERVICE);
        mStopServiceHandler.sendEmptyMessageDelayed(WHAT_STOP_SERVICE, 60 * 1000);
    }

    private void storeKeyToDatabase(int accountId, int peerId, @NonNull KeyExchangeSession session) {
        AesKeyPair pair = new AesKeyPair()
                .setVersion(Version.getCurrentVersion())
                .setAccountId(accountId)
                .setPeerId(peerId)
                .setDate(Unixtime.now())
                .setHisAesKey(session.getHisAesKey())
                .setMyAesKey(session.getMyAesKey())
                .setSessionId(session.getId())
                .setStartMessageId(session.getStartMessageId())
                .setEndMessageId(session.getEndMessageId());

        Stores.getInstance()
                .keys(session.getKeyLocationPolicy())
                .saveKeyPair(pair)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    //do nothink
                }, throwable -> showError(throwable.toString()));
    }

    private void processIniciatorState2(int accountId, int peerId, @NonNull KeyExchangeSession session, @NonNull ExchangeMessage message) {
        ExchangeMessage m = new ExchangeMessage.Builder(Version.getCurrentVersion(), message.getSessionId(), SessionState.INITIATOR_FINISHED)
                .create();
        sendMessage(accountId, peerId, m);

        storeKeyToDatabase(accountId, peerId, session);
        finishSession(session, false);
    }

    private void processIniciatorState1(int accountId, int peerId, @NonNull KeyExchangeSession session, @NonNull ExchangeMessage message) {
        String hisAesKey = message.getAesKey();
        PrivateKey myPrivateKey = session.getMyPrivateKey();

        try {
            byte[] hisAesEncoded = Base64.decode(hisAesKey, Base64.DEFAULT);
            String hisOriginalAes = CryptHelper.decryptRsa(hisAesEncoded, myPrivateKey);
            session.setHisAesKey(hisOriginalAes);

            String myOriginalAesKey = CryptHelper.generateRandomAesKey(Version.ofCurrent().getAesKeySize());
            session.setMyAesKey(myOriginalAesKey);

            PublicKey hisPublicKey = CryptHelper.createRsaPublicKeyFromString(message.getPublicKey());
            byte[] myEncodedAesKey = CryptHelper.encryptRsa(myOriginalAesKey, hisPublicKey);
            String myEncodedAesKeyBase64 = Base64.encodeToString(myEncodedAesKey, Base64.DEFAULT);

            Logger.d(TAG, "processIniciatorState1, myOriginalAesKey: " + myOriginalAesKey + ", hisOriginalAes: " + hisOriginalAes);

            ExchangeMessage m = new ExchangeMessage.Builder(Version.getCurrentVersion(), session.getId(), SessionState.INITIATOR_STATE_2)
                    .setAesKey(myEncodedAesKeyBase64)
                    .create();
            sendMessage(accountId, peerId, m);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    //NO_INITIATOR_STATE_1
    private void processNoIniciatorState1(int accountId, int peerId, @NonNull KeyExchangeSession session, @NonNull ExchangeMessage message) {
        String hisAesKey = message.getAesKey();

        PrivateKey myPrivateKey = session.getMyPrivateKey();

        try {
            byte[] hisAesEncoded = Base64.decode(hisAesKey, Base64.DEFAULT);
            String hisOriginalAes = CryptHelper.decryptRsa(hisAesEncoded, myPrivateKey);

            Logger.d(TAG, "processNoIniciatorState1, hisOriginalAes: " + hisOriginalAes);

            session.setHisAesKey(hisOriginalAes);

            ExchangeMessage m = new ExchangeMessage.Builder(Version.getCurrentVersion(), session.getId(), SessionState.NO_INITIATOR_FINISHED)
                    .create();
            sendMessage(accountId, peerId, m);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    //NO_INITIATOR_EMPTY
    private void processNoIniciatorEmptyState(int accountId, int peerId, @NonNull KeyExchangeSession session, @NonNull ExchangeMessage message) {
        try {
            String originalAesKey = CryptHelper.generateRandomAesKey(Version.ofCurrent().getAesKeySize());
            session.setMyAesKey(originalAesKey);

            Logger.d(TAG, "processNoIniciatorEmptyState, originalAesKey: " + originalAesKey);

            PublicKey publicKey = CryptHelper.createRsaPublicKeyFromString(message.getPublicKey());
            byte[] encodedAesKey = CryptHelper.encryptRsa(originalAesKey, publicKey);
            String encodedAesKeyBase64 = Base64.encodeToString(encodedAesKey, Base64.DEFAULT);

            KeyPair myPair = CryptHelper.generateRsaKeyPair(Version.ofCurrent().getRsaKeySize());
            session.setMyPrivateKey(myPair.getPrivate());

            byte[] myEncodedPublicKey = myPair.getPublic().getEncoded();
            String myPulicBase64 = Base64.encodeToString(myEncodedPublicKey, Base64.DEFAULT);

            ExchangeMessage m = new ExchangeMessage.Builder(Version.getCurrentVersion(), message.getSessionId(), SessionState.NO_INITIATOR_STATE_1)
                    .setAesKey(encodedAesKeyBase64)
                    .setPublicKey(myPulicBase64)
                    .create();

            sendMessage(accountId, peerId, m);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(int accountId, int peerId, @NonNull ExchangeMessage message) {
        Logger.d(TAG, "sendMessage, message: " + message);

        sendMessageImpl(accountId, peerId, message)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(integer -> onMessageSent(message, integer), throwable -> {
                    showError(throwable.toString());

                    KeyExchangeSession session = findSessionFor(accountId, peerId);
                    if (nonNull(session)) {
                        finishSession(session, true);
                    }
                });
    }

    private void refreshSessionNotification(@NonNull KeyExchangeSession session) {
        if (Utils.hasOreo()) {
            mNotificationManager.createNotificationChannel(AppNotificationChannels.getKeyExchangeChannel(this));
        }
        NotificationCompat.Builder builder = findBuilder(session.getId());

        if (nonNull(builder)) {
            int localState = session.getLocalSessionState();
            int opponentState = session.getOppenentSessionState();

            int state = Math.max(localState, opponentState);

            builder.setProgress(SessionState.CLOSED, state, false);
            mNotificationManager.notify(String.valueOf(session.getId()), NOTIFICATION_KEY_EXCHANGE, builder.build());
        }
    }

    private void fireSessionStateChanged(@NonNull KeyExchangeSession session) {
        Logger.d(TAG, "fireSessionStateChanged, id: " + session.getId() + ", state: " + session.getLocalSessionState());
        refreshSessionNotification(session);
        sendSessionStateChangeBroadcast(session);
    }

    private void onMessageSent(@NonNull ExchangeMessage message, int id) {
        Logger.d(TAG, "onMessageSent, result_id: " + id + ", message: " + message);

        KeyExchangeSession session = mCurrentActiveSessions.get(message.getSessionId());
        if (nonNull(session)) {
            session.setLocalSessionState(message.getSenderSessionState());
            session.appendMessageId(id);
            fireSessionStateChanged(session);
        }

        toggleServiceLiveHandler();
    }
}
