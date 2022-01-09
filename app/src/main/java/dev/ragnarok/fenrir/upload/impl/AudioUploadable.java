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

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadResult;
import io.reactivex.rxjava3.core.Single;

public class AudioUploadable implements IUploadable<Audio> {

    private final Context context;
    private final INetworker networker;

    public AudioUploadable(Context context, INetworker networker) {
        this.context = context;
        this.networker = networker;
    }

    public static String findFileName(Context context, Uri uri) {
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
    public Single<UploadResult<Audio>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int accountId = upload.getAccountId();

        Single<UploadServer> serverSingle;
        if (initialServer == null) {
            serverSingle = networker.vkDefault(accountId)
                    .audio()
                    .getUploadServer()
                    .map(s -> s);
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

                String TrackName = filename.replace(".mp3", "");
                String Artist = "";
                String[] arr = TrackName.split(" - ");
                if (arr.length > 1) {
                    Artist = arr[0];
                    TrackName = TrackName.replace(Artist + " - ", "");
                }

                String finalArtist = Artist;
                String finalTrackName = TrackName;
                return networker.uploads()
                        .uploadAudioRx(server.getUrl(), filename, is[0], listener)
                        .doFinally(safelyCloseAction(is[0]))
                        .flatMap(dto -> networker
                                .vkDefault(accountId)
                                .audio()
                                .save(dto.server, dto.audio, dto.hash, finalArtist, finalTrackName)
                                .flatMap(tmpList -> {
                                    Audio document = Dto2Model.transform(tmpList);
                                    UploadResult<Audio> result = new UploadResult<>(server, document);
                                    return Single.just(result);
                                }));
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }
}
