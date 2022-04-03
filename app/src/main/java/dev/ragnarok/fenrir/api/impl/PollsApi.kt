package dev.ragnarok.fenrir.api.impl

import com.google.gson.JsonArray
import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.TokenType
import dev.ragnarok.fenrir.api.interfaces.IPollsApi
import dev.ragnarok.fenrir.api.model.VKApiPoll
import dev.ragnarok.fenrir.api.services.IPollsService
import io.reactivex.rxjava3.core.Single

internal class PollsApi(accountId: Int, provider: IServiceProvider) :
    AbsApi(accountId, provider), IPollsApi {
    override fun create(
        question: String?,
        isAnonymous: Boolean?,
        isMultiple: Boolean?,
        ownerId: Int,
        addAnswers: Collection<String>
    ): Single<VKApiPoll> {
        val array = JsonArray()
        for (answer in addAnswers) {
            array.add(answer)
        }
        return provideService(IPollsService::class.java, TokenType.USER)
            .flatMap { service: IPollsService ->
                service
                    .create(
                        question,
                        integerFromBoolean(isAnonymous),
                        integerFromBoolean(isMultiple),
                        ownerId,
                        array.toString()
                    )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun deleteVote(
        ownerId: Int?,
        pollId: Int,
        answerId: Int,
        isBoard: Boolean?
    ): Single<Boolean> {
        return provideService(IPollsService::class.java, TokenType.USER)
            .flatMap { service: IPollsService ->
                service.deleteVote(ownerId, pollId, answerId, integerFromBoolean(isBoard))
                    .map(extractResponseWithErrorHandling())
                    .map { response: Int -> response == 1 }
            }
    }

    override fun addVote(
        ownerId: Int,
        pollId: Int,
        answerIds: Set<Int>,
        isBoard: Boolean?
    ): Single<Boolean> {
        return provideService(IPollsService::class.java, TokenType.USER)
            .flatMap { service: IPollsService ->
                service.addVote(ownerId, pollId, join(answerIds, ","), integerFromBoolean(isBoard))
                    .map(extractResponseWithErrorHandling())
                    .map { response: Int -> response == 1 }
            }
    }

    override fun getById(ownerId: Int, isBoard: Boolean?, pollId: Int): Single<VKApiPoll> {
        return provideService(IPollsService::class.java, TokenType.USER)
            .flatMap { service: IPollsService ->
                service.getById(ownerId, integerFromBoolean(isBoard), pollId)
                    .map(extractResponseWithErrorHandling())
            }
    }
}