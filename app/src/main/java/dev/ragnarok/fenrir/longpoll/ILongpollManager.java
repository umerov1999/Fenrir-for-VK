package dev.ragnarok.fenrir.longpoll;

import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.rxjava3.core.Flowable;

public interface ILongpollManager {
    void forceDestroy(int accountId);

    Flowable<VkApiLongpollUpdates> observe();

    Flowable<Integer> observeKeepAlive();

    void keepAlive(int accountId);
}