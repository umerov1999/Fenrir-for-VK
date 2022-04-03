package dev.ragnarok.fenrir.api.interfaces

import androidx.annotation.CheckResult
import dev.ragnarok.fenrir.api.model.VKApiPoll
import io.reactivex.rxjava3.core.Single

interface IPollsApi {
    @CheckResult
    fun create(
        question: String?,
        isAnonymous: Boolean?,
        isMultiple: Boolean?,
        ownerId: Int,
        addAnswers: Collection<String>
    ): Single<VKApiPoll>

    @CheckResult
    fun deleteVote(ownerId: Int?, pollId: Int, answerId: Int, isBoard: Boolean?): Single<Boolean>

    @CheckResult
    fun addVote(
        ownerId: Int,
        pollId: Int,
        answerIds: Set<Int>,
        isBoard: Boolean?
    ): Single<Boolean>

    @CheckResult
    fun getById(ownerId: Int, isBoard: Boolean?, pollId: Int): Single<VKApiPoll>
}