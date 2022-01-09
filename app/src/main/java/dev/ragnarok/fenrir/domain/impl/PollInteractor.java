package dev.ragnarok.fenrir.domain.impl;

import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.domain.IPollInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.model.Poll;
import io.reactivex.rxjava3.core.Single;

public class PollInteractor implements IPollInteractor {

    private final INetworker networker;

    public PollInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Single<Poll> createPoll(int accountId, String question, boolean anon, boolean multiple, int ownerId, List<String> options) {
        return networker.vkDefault(accountId)
                .polls()
                .create(question, anon, multiple, ownerId, options)
                .map(Dto2Model::transform);
    }

    @Override
    public Single<Poll> addVote(int accountId, Poll poll, Set<Integer> answerIds) {
        return networker.vkDefault(accountId)
                .polls()
                .addVote(poll.getOwnerId(), poll.getId(), answerIds, poll.isBoard())
                .flatMap(ignore -> getPollById(accountId, poll.getOwnerId(), poll.getId(), poll.isBoard()));
    }

    @Override
    public Single<Poll> removeVote(int accountId, Poll poll, int answerId) {
        return networker.vkDefault(accountId)
                .polls()
                .deleteVote(poll.getOwnerId(), poll.getId(), answerId, poll.isBoard())
                .flatMap(ignore -> getPollById(accountId, poll.getOwnerId(), poll.getId(), poll.isBoard()));
    }

    @Override
    public Single<Poll> getPollById(int accountId, int ownerId, int pollId, boolean isBoard) {
        return networker.vkDefault(accountId)
                .polls()
                .getById(ownerId, isBoard, pollId)
                .map(dto -> Dto2Model.transform(dto).setBoard(isBoard));
    }
}