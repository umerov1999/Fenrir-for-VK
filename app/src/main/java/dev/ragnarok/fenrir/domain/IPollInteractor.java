package dev.ragnarok.fenrir.domain;

import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.model.Poll;
import io.reactivex.rxjava3.core.Single;

public interface IPollInteractor {
    Single<Poll> createPoll(int accountId, String question, boolean anon, boolean multiple, int ownerId, List<String> options);

    Single<Poll> addVote(int accountId, Poll poll, Set<Integer> answerIds);

    Single<Poll> removeVote(int accountId, Poll poll, int answerId);

    Single<Poll> getPollById(int accountId, int ownerId, int pollId, boolean isBoard);
}