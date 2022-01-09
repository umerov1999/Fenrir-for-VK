package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.longpoll.VkApiGroupLongpollUpdates;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.rxjava3.core.Single;

public interface ILongpollApi {
    Single<VkApiLongpollUpdates> getUpdates(String server, String key, long ts, int wait, int mode, int version);

    Single<VkApiGroupLongpollUpdates> getGroupUpdates(String server, String key, String ts, int wait);
}