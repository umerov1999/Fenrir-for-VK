package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PollEntity;
import dev.ragnarok.fenrir.db.model.entity.TopicEntity;
import dev.ragnarok.fenrir.model.criteria.TopicsCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface ITopicsStore {

    @CheckResult
    Single<List<TopicEntity>> getByCriteria(@NonNull TopicsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, List<TopicEntity> topics, OwnerEntities owners, boolean canAddTopic, int defaultOrder, boolean clearBefore);

    @CheckResult
    Completable attachPoll(int accountId, int ownerId, int topicId, PollEntity pollDbo);
}