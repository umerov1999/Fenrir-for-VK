package dev.ragnarok.fenrir.upload.impl;

import static dev.ragnarok.fenrir.util.Utils.safelyClose;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import io.reactivex.rxjava3.core.Single;

public class OwnerPhotoUploadable implements IUploadable<Post> {

    private final Context context;
    private final INetworker networker;
    private final IWallsRepository walls;

    public OwnerPhotoUploadable(Context context, INetworker networker, IWallsRepository walls) {
        this.context = context;
        this.networker = networker;
        this.walls = walls;
    }

    @Override
    public Single<UploadResult<Post>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int accountId = upload.getAccountId();
        int ownerId = upload.getDestination().getOwnerId();

        Single<UploadServer> serverSingle;
        if (initialServer == null) {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getOwnerPhotoUploadServer(ownerId)
                    .map(s -> s);
        } else {
            serverSingle = Single.just(initialServer);
        }

        return serverSingle.flatMap(server -> {
            InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadOwnerPhotoRx(server.getUrl(), is[0], listener)
                        .doFinally(() -> safelyClose(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .saveOwnerPhoto(dto.server, dto.hash, dto.photo)
                                .flatMap(response -> {
                                    if (response.postId == 0) {
                                        return Single.error(new NotFoundException("Post id=0"));
                                    }

                                    return walls.getById(accountId, ownerId, response.postId)
                                            .map(post -> new UploadResult<>(server, post));
                                }));
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }
}