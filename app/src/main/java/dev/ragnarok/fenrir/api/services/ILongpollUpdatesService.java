package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.longpoll.VkApiGroupLongpollUpdates;
import dev.ragnarok.fenrir.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ILongpollUpdatesService {

    @GET
    Single<VkApiLongpollUpdates> getUpdates(@Url String server,
                                            @Query("act") String act,
                                            @Query("key") String key,
                                            @Query("ts") long ts,
                                            @Query("wait") int wait,
                                            @Query("mode") int mode,
                                            @Query("version") int version);

    @GET
    Single<VkApiGroupLongpollUpdates> getGroupUpdates(@Url String server,
                                                      @Query("act") String act,
                                                      @Query("key") String key,
                                                      @Query("ts") String ts,
                                                      @Query("wait") int wait);
}
