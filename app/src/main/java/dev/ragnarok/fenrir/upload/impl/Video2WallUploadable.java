package dev.ragnarok.fenrir.upload.impl;

import static dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction;
import static dev.ragnarok.fenrir.util.Utils.safelyClose;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.Method;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadResult;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class Video2WallUploadable implements IUploadable<Video> {

    private final Context context;
    private final INetworker networker;
    private final IAttachmentsRepository attachmentsRepository;

    public Video2WallUploadable(Context context, INetworker networker, IAttachmentsRepository attachmentsRepository) {
        this.context = context;
        this.networker = networker;
        this.attachmentsRepository = attachmentsRepository;
    }

    private static String findFileName(Context context, Uri uri) {
        String fileName = uri.getLastPathSegment();
        try {
            String scheme = uri.getScheme();
            if (scheme.equals("file")) {
                fileName = uri.getLastPathSegment();
            } else if (scheme.equals("content")) {
                String[] proj = {MediaStore.MediaColumns.DISPLAY_NAME};

                Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);
                if (cursor != null && cursor.getCount() != 0) {
                    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
                    cursor.moveToFirst();
                    fileName = cursor.getString(columnIndex);
                }

                if (cursor != null) {
                    cursor.close();
                }
            }

        } catch (Exception ignored) {

        }

        return fileName;
    }

    @Override
    public Single<UploadResult<Video>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int accountId = upload.getAccountId();
        int ownerId = upload.getDestination().getOwnerId();
        Integer groupId = ownerId < 0 ? Math.abs(ownerId) : null;
        Single<UploadServer> serverSingle = networker.vkDefault(accountId)
                .docs()
                .getVideoServer(1, groupId, findFileName(context, upload.getFileUri()))
                .map(s -> s);

        return serverSingle.flatMap(server -> {
            InputStream[] is = new InputStream[1];

            try {
                Uri uri = upload.getFileUri();

                File file = new File(uri.getPath());
                if (file.isFile()) {
                    is[0] = new FileInputStream(file);
                } else {
                    is[0] = context.getContentResolver().openInputStream(uri);
                }

                if (is[0] == null) {
                    return Single.error(new NotFoundException("Unable to open InputStream, URI: " + uri));
                }

                String filename = findFileName(context, uri);
                return networker.uploads()
                        .uploadVideoRx(server.getUrl(), filename, is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> {

                            Video video = new Video().setId(dto.video_id).setOwnerId(dto.owner_id).setTitle(findFileName(context, upload.getFileUri()));
                            UploadResult<Video> result = new UploadResult<>(server, video);

                            if (upload.isAutoCommit()) {
                                return commit(attachmentsRepository, upload, video).andThen(Single.just(result));
                            } else {
                                return Single.just(result);
                            }
                        });
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }

    private Completable commit(IAttachmentsRepository repository, Upload upload, Video video) {
        int accountId = upload.getAccountId();
        UploadDestination dest = upload.getDestination();

        switch (dest.getMethod()) {
            case Method.TO_COMMENT:
                return repository
                        .attach(accountId, AttachToType.COMMENT, dest.getId(), Collections.singletonList(video));
            case Method.TO_WALL:
                return repository
                        .attach(accountId, AttachToType.POST, dest.getId(), Collections.singletonList(video));
        }

        return Completable.error(new UnsupportedOperationException());
    }
}
