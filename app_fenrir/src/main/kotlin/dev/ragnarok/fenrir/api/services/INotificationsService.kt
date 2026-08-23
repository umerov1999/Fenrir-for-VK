package dev.ragnarok.fenrir.api.services

import dev.ragnarok.fenrir.api.model.response.BaseResponse
import dev.ragnarok.fenrir.api.model.response.NotificationsResponse
import dev.ragnarok.fenrir.api.rest.IServiceRest
import dev.ragnarok.fenrir.model.FeedbackVKOfficialList
import kotlinx.coroutines.flow.Flow

class INotificationsService : IServiceRest() {
    val markAsViewed: Flow<BaseResponse<Int>>
        get() = rest.request("notifications.markAsViewed", null, baseInt)

    operator fun get(
        count: Int?,
        startFrom: String?,
        filters: String?
    ): Flow<BaseResponse<NotificationsResponse>> {
        return rest.request(
            "notifications.get", form(
                "count" to count,
                "start_from" to startFrom,
                "filters" to filters,
            ), base(NotificationsResponse.serializer())
        )
    }

    fun getOfficial(
        count: Int?,
        startFrom: Int?,
        fields: String?
    ): Flow<BaseResponse<FeedbackVKOfficialList>> {
        return rest.request(
            "notifications.get", form(
                "count" to count,
                "start_from" to startFrom,
                "fields" to fields
            ), base(FeedbackVKOfficialList.serializer())
        )
    }

    fun hide(query: String?): Flow<BaseResponse<Int>> {
        return rest.request("notifications.hide", form("query" to query), baseInt)
    }
}