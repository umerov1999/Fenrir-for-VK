package dev.ragnarok.fenrir.upload.impl;

import static dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction;
import static dev.ragnarok.fenrir.util.Utils.safelyClose;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;
import java.util.Collections;

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.Method;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class Photo2WallUploadable implements IUploadable<Photo> {

    private final Context context;
    private final INetworker networker;
    private final IAttachmentsRepository attachmentsRepository;

    public Photo2WallUploadable(Context context, INetworker networker, IAttachmentsRepository attachmentsRepository) {
        this.context = context;
        this.networker = networker;
        this.attachmentsRepository = attachmentsRepository;
    }

    @Override
    public Single<UploadResult<Photo>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int subjectOwnerId = upload.getDestination().getOwnerId();
        Integer userId = subjectOwnerId > 0 ? subjectOwnerId : null;
        Integer groupId = subjectOwnerId < 0 ? Math.abs(subjectOwnerId) : null;
        int accountId = upload.getAccountId();

        Single<UploadServer> serverSingle;
        if (Objects.nonNull(initialServer)) {
            serverSingle = Single.just(initialServer);
        } else {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getWallUploadServer(groupId)
                    .map(s -> s);
        }

        return serverSingle.flatMap(server -> {
            InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadPhotoToWallRx(server.getUrl(), is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .saveWallPhoto(userId, groupId, dto.photo, dto.server, dto.hash, null, null, null)
                                .flatMap(photos -> {
                                    if (photos.isEmpty()) {
                                        return Single.error(new NotFoundException());
                                    }

                                    Photo photo = Dto2Model.transform(photos.get(0));
                                    UploadResult<Photo> result = new UploadResult<>(server, photo);

                                    if (upload.isAutoCommit()) {
                                        return commit(attachmentsRepository, upload, photo).andThen(Single.just(result));
                                    } else {
                                        return Single.just(result);
                                    }
                                }));
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }

    private Completable commit(IAttachmentsRepository repository, Upload upload, Photo photo) {
        int accountId = upload.getAccountId();
        UploadDestination dest = upload.getDestination();

        switch (dest.getMethod()) {
            case Method.TO_COMMENT:
                return repository
                        .attach(accountId, AttachToType.COMMENT, dest.getId(), Collections.singletonList(photo));
            case Method.TO_WALL:
                return repository
                        .attach(accountId, AttachToType.POST, dest.getId(), Collections.singletonList(photo));
        }

        return Completable.error(new UnsupportedOperationException());
    }
}