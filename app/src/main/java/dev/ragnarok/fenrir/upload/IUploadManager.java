package dev.ragnarok.fenrir.upload;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public interface IUploadManager {
    Single<List<Upload>> get(int accountId, @NonNull UploadDestination destination);

    void enqueue(@NonNull List<UploadIntent> intents);

    void cancel(int id);

    void cancelAll(int accountId, @NonNull UploadDestination destination);

    Optional<Upload> getCurrent();

    Flowable<int[]> observeDeleting(boolean includeCompleted);

    Flowable<List<Upload>> observeAdding();

    Flowable<Upload> obseveStatus();

    Flowable<Pair<Upload, UploadResult<?>>> observeResults();

    Flowable<List<IProgressUpdate>> observeProgress();

    interface IProgressUpdate {
        int getId();

        int getProgress();
    }
}