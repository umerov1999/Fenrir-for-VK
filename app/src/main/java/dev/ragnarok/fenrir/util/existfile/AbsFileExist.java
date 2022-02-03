package dev.ragnarok.fenrir.util.existfile;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.util.List;

import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper;
import io.reactivex.rxjava3.core.Completable;

public interface AbsFileExist {
    void findRemoteAudios(Context context) throws IOException;

    Completable findLocalImages(@Nullable List<SelectablePhotoWrapper> photos);

    void addAudio(@NonNull String file);

    Completable findAllAudios(Context context);

    void markExistPhotos(@NonNull List<SelectablePhotoWrapper> photos);

    boolean isExistRemoteAudio(@NonNull String file);

    int isExistAllAudio(@NonNull String file);
}
