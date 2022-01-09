package dev.ragnarok.fenrir.upload.impl;

import static dev.ragnarok.fenrir.util.RxUtils.safelyCloseAction;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;
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
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.MessageMethod;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadResult;
import io.reactivex.rxjava3.core.Single;

public class StoryUploadable implements IUploadable<Story> {

    private final Context context;
    private final INetworker networker;

    public StoryUploadable(Context context, INetworker networker) {
        this.context = context;
        this.networker = networker;
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
    public Single<UploadResult<Story>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int accountId = upload.getAccountId();

        Single<UploadServer> serverSingle;
        if (initialServer == null) {
            serverSingle = upload.getDestination().getMessageMethod() == MessageMethod.VIDEO ? networker.vkDefault(accountId).users().stories_getVideoUploadServer().map(s -> s) : networker.vkDefault(accountId).users().stories_getPhotoUploadServer().map(s -> s);
        } else {
            serverSingle = Single.just(initialServer);
        }

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
                        .uploadStoryRx(server.getUrl(), filename, is[0], listener, upload.getDestination().getMessageMethod() == MessageMethod.VIDEO)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> networker
                                .vkDefault(accountId)
                                .users()
                                .stories_save(dto.response.upload_result)
                                .map(items -> listEmptyIfNull(items.getItems()))
                                .flatMap(tmpList -> Repository.INSTANCE.getOwners().findBaseOwnersDataAsBundle(accountId, Collections.singletonList(Settings.get().accounts().getCurrent()), IOwnersRepository.MODE_ANY, null)
                                        .flatMap(owners1 -> {
                                            Story document = Dto2Model.transformStory(tmpList.get(0), owners1);
                                            UploadResult<Story> result = new UploadResult<>(server, document);
                                            return Single.just(result);
                                        })));
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }
}
