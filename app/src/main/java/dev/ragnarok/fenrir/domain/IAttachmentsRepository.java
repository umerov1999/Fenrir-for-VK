package dev.ragnarok.fenrir.domain;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface IAttachmentsRepository {

    @CheckResult
    Completable remove(int accountId, @AttachToType int type, int attachToId, int generatedAttachmentId);

    @CheckResult
    Completable attach(int accountId, @AttachToType int attachToType, int attachToDbid, @NonNull List<? extends AbsModel> models);

    Single<List<Pair<Integer, AbsModel>>> getAttachmentsWithIds(int accountId, @AttachToType int attachToType, int attachToDbid);

    Observable<IAddEvent> observeAdding();

    Observable<IRemoveEvent> observeRemoving();

    interface IBaseEvent {
        int getAccountId();

        @AttachToType
        int getAttachToType();

        int getAttachToId();
    }

    interface IRemoveEvent extends IBaseEvent {
        int getGeneratedId();
    }

    interface IAddEvent extends IBaseEvent {
        List<Pair<Integer, AbsModel>> getAttachments();
    }
}