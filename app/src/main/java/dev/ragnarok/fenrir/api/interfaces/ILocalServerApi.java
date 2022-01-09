package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import io.reactivex.rxjava3.core.Single;

public interface ILocalServerApi {
    @CheckResult
    Single<Items<VKApiVideo>> getVideos(Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Items<VKApiAudio>> getAudios(Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Items<VKApiAudio>> getDiscography(Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Items<VKApiVideo>> searchVideos(String query, Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Items<VKApiAudio>> searchAudios(String query, Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Items<VKApiAudio>> searchDiscography(String query, Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Items<VKApiPhoto>> searchPhotos(String query, Integer offset, Integer count, boolean reverse);

    @CheckResult
    Single<Integer> update_time(String hash);

    @CheckResult
    Single<Integer> delete_media(String hash);

    @CheckResult
    Single<String> get_file_name(String hash);

    @CheckResult
    Single<Integer> update_file_name(String hash, String name);
}
