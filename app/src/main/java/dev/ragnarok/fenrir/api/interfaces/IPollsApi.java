package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.Set;

import dev.ragnarok.fenrir.api.model.VKApiPoll;
import io.reactivex.rxjava3.core.Single;


public interface IPollsApi {

    @CheckResult
    Single<VKApiPoll> create(String question, Boolean isAnonymous, Boolean isMultiple, Integer ownerId, Collection<String> addAnswers);

    @CheckResult
    Single<Boolean> deleteVote(Integer ownerId, int pollId, int answerId, Boolean isBoard);

    @CheckResult
    Single<Boolean> addVote(Integer ownerId, int pollId, Set<Integer> answerIds, Boolean isBoard);

    @CheckResult
    Single<VKApiPoll> getById(Integer ownerId, Boolean isBoard, Integer pollId);

}
