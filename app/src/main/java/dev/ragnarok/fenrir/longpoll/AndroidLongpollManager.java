package dev.ragnarok.fenrir.longpoll;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Looper;
import android.os.Message;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executors;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiGroupLongpollUpdates;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates;
import dev.ragnarok.fenrir.realtime.IRealtimeMessagesProcessor;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.processors.PublishProcessor;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AndroidLongpollManager implements ILongpollManager, UserLongpoll.Callback, GroupLongpoll.Callback {

    private static final String TAG = AndroidLongpollManager.class.getSimpleName();
    private static final Scheduler MONO_SCHEDULER = Schedulers.from(Executors.newFixedThreadPool(1));
    private final SparseArray<LongpollEntry> map;
    private final INetworker networker;
    private final PublishProcessor<Integer> keepAlivePublisher;
    private final PublishProcessor<VkApiLongpollUpdates> actionsPublisher;
    private final IRealtimeMessagesProcessor messagesProcessor;
    private final Object lock = new Object();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    AndroidLongpollManager(INetworker networker, IRealtimeMessagesProcessor messagesProcessor) {
        this.networker = networker;
        this.messagesProcessor = messagesProcessor;
        keepAlivePublisher = PublishProcessor.create();
        actionsPublisher = PublishProcessor.create();
        map = new SparseArray<>(1);
    }

    @Override
    public Flowable<VkApiLongpollUpdates> observe() {
        return actionsPublisher.onBackpressureBuffer();
    }

    @Override
    public Flowable<Integer> observeKeepAlive() {
        return keepAlivePublisher.onBackpressureBuffer();
    }

    private ILongpoll createLongpoll(int accountId) {
        //return accountId > 0 ? new UserLongpoll(networker, accountId, this) : new GroupLongpoll(networker, Math.abs(accountId), this);
        return new UserLongpoll(networker, accountId, this);
    }

    @Override
    public void forceDestroy(int accountId) {
        Logger.d(TAG, "forceDestroy, accountId: " + accountId);
        synchronized (lock) {
            LongpollEntry entry = map.get(accountId);
            if (nonNull(entry)) {
                entry.destroy();
            }
        }
    }

    @Override
    public void keepAlive(int accountId) {
        Logger.d(TAG, "keepAlive, accountId: " + accountId);
        synchronized (lock) {
            LongpollEntry entry = map.get(accountId);
            if (nonNull(entry)) {
                entry.deferDestroy();
            } else {
                entry = new LongpollEntry(createLongpoll(accountId), this);
                map.put(accountId, entry);
                entry.connect();
            }
        }
    }

    private void notifyDestroy(LongpollEntry entry) {
        Logger.d(TAG, "destroyed, accountId: " + entry.getAccountId());
        synchronized (lock) {
            map.remove(entry.getAccountId());
        }
    }

    private void notifyPreDestroy(LongpollEntry entry) {
        Logger.d(TAG, "pre-destroy, accountId: " + entry.getAccountId());
        keepAlivePublisher.onNext(entry.getAccountId());
    }

    @Override
    public void onUpdates(int accountId, @NonNull VkApiLongpollUpdates updates) {
        Logger.d(TAG, "updates, accountId: " + accountId);

        if (nonEmpty(updates.getAddMessageUpdates())) {
            messagesProcessor.process(accountId, updates.getAddMessageUpdates());
        }

        compositeDisposable.add(new LongPollEventSaver()
                .save(accountId, updates)
                .subscribeOn(MONO_SCHEDULER)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(() -> onUpdatesSaved(accountId, updates), RxUtils.ignore()));
    }

    private void onUpdatesSaved(int accountId, VkApiLongpollUpdates updates) {
        actionsPublisher.onNext(updates);
    }

    @Override
    public void onUpdates(int groupId, @NonNull VkApiGroupLongpollUpdates updates) {

    }

    private static final class LongpollEntry {

        final ILongpoll longpoll;
        final SocketHandler handler;
        final WeakReference<AndroidLongpollManager> managerReference;
        final int accountId;
        boolean released;

        LongpollEntry(ILongpoll longpoll, AndroidLongpollManager manager) {
            this.longpoll = longpoll;
            accountId = longpoll.getAccountId();
            managerReference = new WeakReference<>(manager);
            handler = new SocketHandler(this);
        }

        void connect() {
            longpoll.connect();
            handler.restartPreDestroy();
        }

        void destroy() {
            handler.release();
            longpoll.shutdown();
            released = true;

            AndroidLongpollManager manager = managerReference.get();
            if (nonNull(manager)) {
                manager.notifyDestroy(this);
            }
        }

        void deferDestroy() {
            handler.restartPreDestroy();
        }

        int getAccountId() {
            return accountId;
        }

        void firePreDestroy() {
            AndroidLongpollManager manager = managerReference.get();
            if (nonNull(manager)) {
                manager.notifyPreDestroy(this);
            }
        }
    }

    private static final class SocketHandler extends android.os.Handler {

        static final int PRE_DESTROY = 2;
        static final int DESTROY = 3;

        final WeakReference<LongpollEntry> reference;

        SocketHandler(AndroidLongpollManager.LongpollEntry holder) {
            super(Looper.getMainLooper());
            reference = new WeakReference<>(holder);
        }

        void restartPreDestroy() {
            removeMessages(PRE_DESTROY);
            removeMessages(DESTROY);
            sendEmptyMessageDelayed(PRE_DESTROY, 30_000L);
        }

        void postDestroy() {
            sendEmptyMessageDelayed(DESTROY, 30_000L);
        }

        void release() {
            removeMessages(PRE_DESTROY);
            removeMessages(DESTROY);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            LongpollEntry holder = reference.get();
            if (holder != null && !holder.released) {
                switch (msg.what) {
                    case PRE_DESTROY:
                        postDestroy();
                        holder.firePreDestroy();
                        break;

                    case DESTROY:
                        holder.destroy();
                        break;
                }
            }
        }
    }
}