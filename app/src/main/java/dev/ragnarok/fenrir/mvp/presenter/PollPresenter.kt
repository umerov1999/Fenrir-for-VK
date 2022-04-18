package dev.ragnarok.fenrir.mvp.presenter

import android.os.Bundle
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.domain.IPollInteractor
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.fromIOToMain
import dev.ragnarok.fenrir.model.Poll
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter
import dev.ragnarok.fenrir.mvp.view.IPollView

class PollPresenter(accountId: Int, private var mPoll: Poll, savedInstanceState: Bundle?) :
    AccountDependencyPresenter<IPollView>(accountId, savedInstanceState) {
    private val pollInteractor: IPollInteractor
    private var mTempCheckedId: MutableSet<Int>
    private var loadingNow = false
    private fun setLoadingNow(loadingNow: Boolean) {
        this.loadingNow = loadingNow
        resolveButtonView()
    }

    private fun refreshPollData() {
        if (loadingNow) return
        val accountId = accountId
        setLoadingNow(true)
        appendDisposable(pollInteractor.getPollById(
            accountId,
            mPoll.ownerId,
            mPoll.id,
            mPoll.isBoard
        )
            .fromIOToMain()
            .subscribe({ poll -> onPollInfoUpdated(poll) }) { t ->
                onLoadingError(
                    t
                )
            })
    }

    private fun onLoadingError(t: Throwable) {
        showError(t)
        setLoadingNow(false)
    }

    private fun onPollInfoUpdated(poll: Poll) {
        mPoll = poll
        mTempCheckedId = arrayToSet(poll.myAnswerIds)
        setLoadingNow(false)
        resolveQuestionView()
        resolveVotesCountView()
        resolvePollTypeView()
        resolveCreationTimeView()
        resolveVotesListView()
        resolvePhotoView()
    }

    private fun resolveButtonView() {
        view?.let {
            it.displayLoading(loadingNow)
            it.setupButton(isVoted)
        }
    }

    private fun resolveVotesListView() {
        view?.displayVotesList(
            mPoll.answers,
            !isVoted,
            mPoll.isMultiple,
            mTempCheckedId
        )
    }

    private fun resolveVotesCountView() {
        view?.displayVoteCount(mPoll.voteCount)
    }

    private fun resolvePollTypeView() {
        view?.displayType(mPoll.isAnonymous)
    }

    private fun resolveQuestionView() {
        view?.displayQuestion(mPoll.question)
    }

    private fun resolveCreationTimeView() {
        view?.displayCreationTime(mPoll.creationTime)
    }

    private fun resolvePhotoView() {
        view?.displayPhoto(mPoll.photo)
    }

    override fun onGuiCreated(viewHost: IPollView) {
        super.onGuiCreated(viewHost)
        resolveButtonView()
        resolveVotesListView()
        resolveVotesCountView()
        resolvePollTypeView()
        resolveQuestionView()
        resolveCreationTimeView()
        resolvePhotoView()
    }

    fun fireVoteChecked(newid: MutableSet<Int>) {
        mTempCheckedId = newid
    }

    private fun vote() {
        if (loadingNow) return
        val accountId = accountId
        val voteIds: Set<Int> = HashSet(mTempCheckedId)
        setLoadingNow(true)
        appendDisposable(pollInteractor.addVote(accountId, mPoll, voteIds)
            .fromIOToMain()
            .subscribe({ onPollInfoUpdated(it) }) {
                onLoadingError(it)
            })
    }

    private val isVoted: Boolean
        get() = mPoll.myAnswerIds != null && mPoll.myAnswerIds.isNotEmpty()

    fun fireButtonClick() {
        if (loadingNow) return
        if (isVoted) {
            removeVote()
        } else {
            if (mTempCheckedId.isEmpty()) {
                view?.showError(R.string.select)
            } else {
                vote()
            }
        }
    }

    private fun removeVote() {
        val accountId = accountId
        val answerId = mPoll.myAnswerIds[0]
        setLoadingNow(true)
        appendDisposable(pollInteractor.removeVote(accountId, mPoll, answerId)
            .fromIOToMain()
            .subscribe({ poll -> onPollInfoUpdated(poll) }) { t ->
                onLoadingError(
                    t
                )
            })
    }

    companion object {
        private fun arrayToSet(ids: IntArray?): MutableSet<Int> {
            ids ?: return HashSet(0)
            val set: MutableSet<Int> = HashSet(ids.size)
            for (id in ids) {
                set.add(id)
            }
            return set
        }
    }

    init {
        mTempCheckedId = arrayToSet(mPoll.myAnswerIds)
        pollInteractor = InteractorFactory.createPollInteractor()
        refreshPollData()
    }
}