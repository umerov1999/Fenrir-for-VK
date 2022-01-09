package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.model.feedback.Feedback;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IFeedbackInteractor {
    Single<List<Feedback>> getCachedFeedbacks(int accountId);

    Single<Pair<List<Feedback>, String>> getActualFeedbacks(int accountId, int count, String startFrom);

    Single<AnswerVKOfficialList> getOfficial(int accountId, Integer count, Integer startFrom);

    Completable maskAaViewed(int accountId);
}