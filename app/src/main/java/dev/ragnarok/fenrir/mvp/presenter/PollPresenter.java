package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashSet;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IPollInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IPollView;
import dev.ragnarok.fenrir.util.RxUtils;

public class PollPresenter extends AccountDependencyPresenter<IPollView> {

    private final IPollInteractor pollInteractor;
    private Poll mPoll;
    private Set<Integer> mTempCheckedId;
    private boolean loadingNow;

    public PollPresenter(int accountId, @NonNull Poll poll, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        mPoll = poll;
        mTempCheckedId = arrayToSet(poll.getMyAnswerIds());
        pollInteractor = InteractorFactory.createPollInteractor();

        refreshPollData();
    }

    private static Set<Integer> arrayToSet(int[] ids) {
        Set<Integer> set = new HashSet<>(ids.length);
        for (int id : ids) {
            set.add(id);
        }
        return set;
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveButtonView();
    }

    private void refreshPollData() {
        if (loadingNow) return;

        int accountId = getAccountId();

        setLoadingNow(true);
        appendDisposable(pollInteractor.getPollById(accountId, mPoll.getOwnerId(), mPoll.getId(), mPoll.isBoard())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }

    private void onLoadingError(Throwable t) {
        callView(v -> showError(v, t));
        setLoadingNow(false);
    }

    private void onPollInfoUpdated(Poll poll) {
        mPoll = poll;
        mTempCheckedId = arrayToSet(poll.getMyAnswerIds());

        setLoadingNow(false);

        resolveQuestionView();
        resolveVotesCountView();
        resolvePollTypeView();
        resolveVotesListView();
        resolvePhotoView();
    }

    private void resolveButtonView() {
        callView(v -> {
            v.displayLoading(loadingNow);
            v.setupButton(isVoted());
        });
    }

    private void resolveVotesListView() {
        callView(v -> v.displayVotesList(mPoll.getAnswers(), !isVoted(), mPoll.isMultiple(), mTempCheckedId));
    }

    private void resolveVotesCountView() {
        callView(v -> v.displayVoteCount(mPoll.getVoteCount()));
    }

    private void resolvePollTypeView() {
        callView(v -> v.displayType(mPoll.isAnonymous()));
    }

    private void resolveQuestionView() {
        callView(v -> v.displayQuestion(mPoll.getQuestion()));
    }

    private void resolveCreationTimeView() {
        callView(v -> v.displayCreationTime(mPoll.getCreationTime()));
    }

    private void resolvePhotoView() {
        callView(v -> v.displayPhoto(mPoll.getPhoto()));
    }

    @Override
    public void onGuiCreated(@NonNull IPollView view) {
        super.onGuiCreated(view);
        resolveButtonView();
        resolveVotesListView();
        resolveVotesCountView();
        resolvePollTypeView();
        resolveQuestionView();
        resolveCreationTimeView();
        resolvePhotoView();
    }

    public void fireVoteChecked(Set<Integer> newid) {
        mTempCheckedId = newid;
    }

    private void vote() {
        if (loadingNow) return;

        int accountId = getAccountId();
        Set<Integer> voteIds = new HashSet<>(mTempCheckedId);

        setLoadingNow(true);
        appendDisposable(pollInteractor.addVote(accountId, mPoll, voteIds)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }

    private boolean isVoted() {
        return mPoll.getMyAnswerIds() != null && mPoll.getMyAnswerIds().length > 0;
    }

    public void fireButtonClick() {
        if (loadingNow) return;

        if (isVoted()) {
            removeVote();
        } else {
            if (mTempCheckedId.isEmpty()) {
                callView(v -> v.showError(R.string.select));
            } else {
                vote();
            }
        }
    }

    private void removeVote() {
        int accountId = getAccountId();
        int answerId = mPoll.getMyAnswerIds()[0];

        setLoadingNow(true);
        appendDisposable(pollInteractor.removeVote(accountId, mPoll, answerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPollInfoUpdated, this::onLoadingError));
    }
}