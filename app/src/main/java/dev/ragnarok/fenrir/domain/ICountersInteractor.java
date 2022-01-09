package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.model.SectionCounters;
import io.reactivex.rxjava3.core.Single;

public interface ICountersInteractor {
    Single<SectionCounters> getApiCounters(int accountId);
}