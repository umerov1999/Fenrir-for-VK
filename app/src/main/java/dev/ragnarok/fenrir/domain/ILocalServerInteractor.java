package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Video;
import io.reactivex.rxjava3.core.Single;

public interface ILocalServerInteractor {
    Single<List<Video>> getVideos(int offset, int count, boolean reverse);

    Single<List<Audio>> getAudios(int offset, int count, boolean reverse);

    Single<List<Audio>> getDiscography(int offset, int count, boolean reverse);

    Single<List<Photo>> getPhotos(int offset, int count, boolean reverse);

    Single<List<Video>> searchVideos(String q, int offset, int count, boolean reverse);

    Single<List<Audio>> searchAudios(String q, int offset, int count, boolean reverse);

    Single<List<Audio>> searchDiscography(String q, int offset, int count, boolean reverse);

    Single<List<Photo>> searchPhotos(String q, int offset, int count, boolean reverse);

    Single<Integer> update_time(String hash);

    Single<Integer> delete_media(String hash);

    Single<String> get_file_name(String hash);

    Single<Integer> update_file_name(String hash, String name);
}
