package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.TokenType
import dev.ragnarok.fenrir.api.interfaces.INotificationsApi
import dev.ragnarok.fenrir.api.model.feedback.VkApiBaseFeedback
import dev.ragnarok.fenrir.api.model.response.NotificationsResponse
import dev.ragnarok.fenrir.api.services.INotificationsService
import dev.ragnarok.fenrir.model.AnswerVKOfficialList
import dev.ragnarok.fenrir.util.Utils.safeCountOf
import io.reactivex.rxjava3.core.Single

internal class NotificationsApi(accountId: Int, provider: IServiceProvider) :
    AbsApi(accountId, provider), INotificationsApi {
    override fun markAsViewed(): Single<Int> {
        return provideService(INotificationsService::class.java, TokenType.USER)
            .flatMap { service ->
                service.markAsViewed()
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun get(
        count: Int?,
        startFrom: String?,
        filters: String?,
        startTime: Long?,
        endTime: Long?
    ): Single<NotificationsResponse> {
        return provideService(INotificationsService::class.java, TokenType.USER)
            .flatMap { service ->
                service[count, startFrom, filters, startTime, endTime]
                    .map(extractResponseWithErrorHandling())
                    .map { response ->
                        val realList: MutableList<VkApiBaseFeedback> =
                            ArrayList(safeCountOf(response.notifications))
                        if (response.notifications != null) {
                            for (n in response.notifications) {
                                if (n == null) continue
                                if (n.reply != null) {
                                    // fix В ответе нет этого параметра
                                    n.reply.from_id = accountId
                                }
                                realList.add(n)
                            }
                        }
                        response.notifications = realList //without unsupported items
                        response
                    }
            }
    }

    override fun getOfficial(
        count: Int?,
        startFrom: Int?,
        filters: String?,
        startTime: Long?,
        endTime: Long?
    ): Single<AnswerVKOfficialList> {
        return provideService(INotificationsService::class.java, TokenType.USER)
            .flatMap { service ->
                service.getOfficial(
                    count,
                    startFrom,
                    filters,
                    startTime,
                    endTime,
                    "photo_200_orig,photo_200"
                )
                    .map(extractResponseWithErrorHandling())
            }
    }

    override fun hide(query: String?): Single<Int> {
        return provideService(INotificationsService::class.java, TokenType.USER)
            .flatMap { service ->
                service.hide(query)
                    .map(extractResponseWithErrorHandling())
            }
    }
}