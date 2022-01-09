package dev.ragnarok.fenrir.upload.impl;

import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.safelyClose;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.InputStream;

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.upload.IUploadable;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import io.reactivex.rxjava3.core.Single;

public class ChatPhotoUploadable implements IUploadable<String> {

    private final Context context;
    private final INetworker networker;

    public ChatPhotoUploadable(Context context, INetworker networker) {
        this.context = context;
        this.networker = networker;
    }

    @Override
    public Single<UploadResult<String>> doUpload(@NonNull Upload upload, @Nullable UploadServer initialServer, @Nullable PercentagePublisher listener) {
        int accountId = upload.getAccountId();
        int chat_id = upload.getDestination().getOwnerId();

        Single<UploadServer> serverSingle;
        if (initialServer == null) {
            serverSingle = networker.vkDefault(accountId)
                    .photos()
                    .getChatUploadServer(chat_id)
                    .map(s -> s);
        } else {
            serverSingle = Single.just(initialServer);
        }

        return serverSingle.flatMap(server -> {
            InputStream[] is = new InputStream[1];

            try {
                is[0] = UploadUtils.openStream(context, upload.getFileUri(), upload.getSize());
                return networker.uploads()
                        .uploadChatPhotoRx(server.getUrl(), is[0], listener)
                        .doFinally(() -> safelyClose(is[0]))
                        .flatMap(dto -> networker.vkDefault(accountId)
                                .photos()
                                .setChatPhoto(dto.response)
                                .flatMap(response -> {
                                    if (response.message_id == 0 || response.chat == null) {
                                        return Single.error(new NotFoundException("message_id=0"));
                                    }

                                    return Single.just(new UploadResult<>(server, firstNonEmptyString(response.chat.photo_200, response.chat.photo_100, response.chat.photo_50)));
                                }));
            } catch (Exception e) {
                safelyClose(is[0]);
                return Single.error(e);
            }
        });
    }
}
