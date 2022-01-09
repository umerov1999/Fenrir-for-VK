package dev.ragnarok.fenrir.longpoll;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiGroupLongpollUpdates;
import dev.ragnarok.fenrir.api.model.response.GroupLongpollServer;
import dev.ragnarok.fenrir.util.PersistentLogger;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

class GroupLongpoll implements ILongpoll {

    private static final int DELAY_ON_ERROR = 10 * 1000;

    private final int groupId;
    private final INetworker networker;
    private final Callback callback;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final Observable<Long> delayedObservable = Observable.interval(DELAY_ON_ERROR, DELAY_ON_ERROR,
            TimeUnit.MILLISECONDS, Injection.provideMainThreadScheduler());
    private String key;
    private String server;
    private String ts;

    GroupLongpoll(INetworker networker, int groupId, Callback callback) {
        this.groupId = groupId;
        this.callback = callback;
        this.networker = networker;
    }

    @Override
    public int getAccountId() {
        return -groupId;
    }

    private void resetServerAttrs() {
        server = null;
        key = null;
        ts = null;
    }

    @Override
    public void shutdown() {
        compositeDisposable.dispose();
    }

    @Override
    public void connect() {
        if (!isListeningNow()) {
            get();
        }
    }

    private boolean isListeningNow() {
        return compositeDisposable.size() > 0;
    }

    private void onServerInfoReceived(GroupLongpollServer info) {
        ts = info.ts;
        key = info.key;
        server = info.server;

        get();
    }

    private void onServerGetError(Throwable throwable) {
        PersistentLogger.logThrowable("Longpoll, ServerGet", throwable);
        getWithDelay();
    }

    private void get() {
        compositeDisposable.clear();

        boolean validServer = nonEmpty(server) && nonEmpty(key) && nonEmpty(ts);
        if (validServer) {
            compositeDisposable.add(networker.longpoll()
                    .getGroupUpdates(server, key, ts, 25)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onUpdates, this::onUpdatesGetError));
        } else {
            compositeDisposable.add(networker.vkDefault(getAccountId())
                    .groups()
                    .getLongPollServer(groupId)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onServerInfoReceived, this::onServerGetError));
        }
    }

    private void onUpdates(VkApiGroupLongpollUpdates updates) {
        if (updates.failed > 0) {
            resetServerAttrs();
            getWithDelay();
        } else {
            ts = updates.ts;

            if (updates.getCount() > 0) {
                callback.onUpdates(groupId, updates);
            }

            get();
        }
    }

    private void onUpdatesGetError(Throwable throwable) {
        PersistentLogger.logThrowable("Longpoll, UpdatesGet", throwable);
        getWithDelay();
    }

    private void getWithDelay() {
        compositeDisposable.add(delayedObservable.subscribe(o -> get()));
    }

    public interface Callback {
        void onUpdates(int groupId, @NonNull VkApiGroupLongpollUpdates updates);
    }
}