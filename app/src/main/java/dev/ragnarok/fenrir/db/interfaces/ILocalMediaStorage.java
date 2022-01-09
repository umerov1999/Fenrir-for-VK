package dev.ragnarok.fenrir.db.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.List;

import javax.annotation.Nullable;

import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.LocalVideo;
import dev.ragnarok.fenrir.picasso.Content_Local;
import io.reactivex.rxjava3.core.Single;


public interface ILocalMediaStorage extends IStorage {

    Single<List<LocalPhoto>> getPhotos(long albumId);

    Single<List<LocalPhoto>> getPhotos();

    Single<List<LocalImageAlbum>> getImageAlbums();

    Single<List<LocalImageAlbum>> getAudioAlbums();

    Single<List<LocalVideo>> getVideos();

    Single<List<Audio>> getAudios(int accountId);

    Single<List<Audio>> getAudios(int accountId, long albumId);

    @Nullable
    Bitmap getOldThumbnail(@Content_Local int type, long content_Id);

    @Nullable
    Bitmap getThumbnail(Uri uri, int x, int y);
}
