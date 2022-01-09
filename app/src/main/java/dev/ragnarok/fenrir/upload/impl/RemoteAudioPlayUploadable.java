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
import java.net.URLEncoder;

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.LocalServerSettings;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.api.model.server.VkApiAudioUploadServer;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class RemoteAudioPlayUploadable implements IUploadable<Audio> {

    private final Context context;
    private final INetworker networker;

    public RemoteAudioPlayUploadable(Context context, INetworker networker) {
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
        InputStream[] is = new InputStream[1];
        LocalServerSettings local_settings = Settings.get().other().getLocalServer();
        try {
            String server_url = Utils.firstNonEmptyString(local_settings.url, "https://debug.dev") + "/method/audio.remoteplay";
            if (local_settings.password != null) {
                server_url += "?password=" + URLEncoder.encode(local_settings.password, "utf-8");
            }
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
            String finalServer_url = server_url;
            return networker.uploads()
                    .remotePlayAudioRx(server_url, filename, is[0], listener)
                    .doFinally(safelyCloseAction(is[0]))
                    .flatMap(dto -> Single.just(new UploadResult<>(new VkApiAudioUploadServer(finalServer_url), new Audio().setId(dto.response))));
        } catch (Exception e) {
            safelyClose(is[0]);
            return Single.error(e);
        }
    }
}
