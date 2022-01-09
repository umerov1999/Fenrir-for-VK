package dev.ragnarok.fenrir.upload;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.api.PercentagePublisher;
import dev.ragnarok.fenrir.api.model.server.UploadServer;
import io.reactivex.rxjava3.core.Single;

public interface IUploadable<T> {
    Single<UploadResult<T>> doUpload(@NonNull Upload upload,
                                     @Nullable UploadServer initialServer,
                                     @Nullable PercentagePublisher listener);
}