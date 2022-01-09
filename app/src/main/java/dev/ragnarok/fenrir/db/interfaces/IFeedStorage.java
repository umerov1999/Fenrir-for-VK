package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.FeedListEntity;
import dev.ragnarok.fenrir.db.model.entity.NewsEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.model.FeedSourceCriteria;
import dev.ragnarok.fenrir.model.criteria.FeedCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IFeedStorage extends IStorage {

    Single<List<NewsEntity>> findByCriteria(@NonNull FeedCriteria criteria);

    Single<int[]> store(int accountId, @NonNull List<NewsEntity> data, @Nullable OwnerEntities owners, boolean clearBeforeStore);

    Completable storeLists(int accountid, @NonNull List<FeedListEntity> entities);

    Single<List<FeedListEntity>> getAllLists(@NonNull FeedSourceCriteria criteria);
}