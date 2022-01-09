package dev.ragnarok.fenrir.realtime;

import java.util.List;

import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Observable;

public interface IRealtimeMessagesProcessor {

    Observable<TmpResult> observeResults();

    int process(int accountId, List<AddMessageUpdate> updates);

    int process(int accountId, int messageId, boolean ignoreIfExists) throws QueueContainsException;

    void registerNotificationsInterceptor(int interceptorId, Pair<Integer, Integer> aidPeerPair);

    void unregisterNotificationsInterceptor(int interceptorId);

    boolean isNotificationIntercepted(int accountId, int peerId);
}
