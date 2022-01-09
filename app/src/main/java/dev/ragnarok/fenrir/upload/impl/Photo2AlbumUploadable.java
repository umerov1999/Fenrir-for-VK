package dev.ragnarok.fenrir.upload.impl;

import static dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction;
import static dev.ragnarok.fenrir.util.Utils.safelyClose;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import java.io.InputStream;
import java.util.Collections;

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.db.interfaces.IPhotosStorage;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.ExifGeoDegree;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class Photo2AlbumUploadable implements IUploadable<Photo> {

    private final Context context;
    private final INetworker networker;
    private final IPhotosStorage storage;

    public Photo2AlbumUploadable(Context context, INetworker networker, IPhotosStorage storage) {
        this.context = context;
        this.networker = networker;
        this.storage = storage;
    }

    @Override
    public Single<UploadResult<Photo>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int accountId = upload.getAccountId();
        int albumId = upload.getDestination().getId();
        Integer groupId = upload.getDestination().getOwnerId() < 0 ? Math.abs(upload.getDestination().getOwnerId()) : null;

        Single<UploadServer> serverSingle;
        if (Objects.nonNull(initialServer)) {
            serverSingle = Single.just(initialServer);
        } else {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getUploadServer(albumId, groupId)
                    .map(s -> s);
        }

        return serverSingle.flatMap(server -> {
            InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadPhotoToAlbumRx(server.getUrl(), is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> {
                            Double latitude = null;
                            Double longitude = null;

                            try {
                                ExifInterface exif = new ExifInterface(UploadUtils.createStream(context, upload.getFileUri()));
                                ExifGeoDegree exifGeoDegree = new ExifGeoDegree(exif);
                                if (exifGeoDegree.isValid()) {
                                    latitude = exifGeoDegree.getLatitude();
                                    longitude = exifGeoDegree.getLongitude();
                                }
                            } catch (Exception ignored) {
                            }

                            return networker
                                    .vkDefault(accountId)
                                    .photos()
                                    .save(albumId, groupId, dto.server, dto.photos_list, dto.hash, latitude, longitude, null)
                                    .flatMap(photos -> {
                                        if (photos.isEmpty()) {
                                            return Single.error(new NotFoundException());
                                        }

                                        PhotoEntity entity = Dto2Entity.mapPhoto(photos.get(0));
                                        Photo photo = Dto2Model.transform(photos.get(0));
                                        Single<UploadResult<Photo>> result = Single.just(new UploadResult<>(server, photo));
                                        return upload.isAutoCommit() ? commit(storage, upload, entity).andThen(result) : result;
                                    });
                        });
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }

    private Completable commit(IPhotosStorage storage, Upload upload, PhotoEntity entity) {
        return storage.insertPhotosRx(upload.getAccountId(), entity.getOwnerId(), entity.getAlbumId(), Collections.singletonList(entity), false);
    }
}