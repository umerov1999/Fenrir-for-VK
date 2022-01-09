package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.feedback.FeedbackEntity;
import dev.ragnarok.fenrir.model.criteria.NotificationsCriteria;
import io.reactivex.rxjava3.core.Single;

public interface IFeedbackStorage extends IStorage {
    Single<int[]> insert(int accountId, List<FeedbackEntity> dbos, OwnerEntities owners, boolean clearBefore);

    Single<List<FeedbackEntity>> findByCriteria(@NonNull NotificationsCriteria criteria);
}