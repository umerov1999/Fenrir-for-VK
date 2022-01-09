package dev.ragnarok.fenrir.api.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.INotificationsApi;
import dev.ragnarok.fenrir.api.model.feedback.VkApiBaseFeedback;
import dev.ragnarok.fenrir.api.model.response.NotificationsResponse;
import dev.ragnarok.fenrir.api.services.INotificationsService;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import io.reactivex.rxjava3.core.Single;


class NotificationsApi extends AbsApi implements INotificationsApi {

    NotificationsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Integer> markAsViewed() {
        return provideService(INotificationsService.class, TokenType.USER)
                .flatMap(service -> service.markAsViewed()
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NotificationsResponse> get(Integer count, String startFrom, String filters, Long startTime, Long endTime) {
        return provideService(INotificationsService.class, TokenType.USER)
                .flatMap(service -> service.get(count, startFrom, filters, startTime, endTime)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> {
                            List<VkApiBaseFeedback> realList = new ArrayList<>(safeCountOf(response.notifications));

                            if (nonNull(response.notifications)) {
                                for (VkApiBaseFeedback n : response.notifications) {
                                    if (isNull(n)) continue;

                                    if (nonNull(n.reply)) {
                                        // fix В ответе нет этого параметра
                                        n.reply.from_id = getAccountId();
                                    }

                                    realList.add(n);
                                }
                            }

                            response.notifications = realList; //without unsupported items
                            return response;
                        }));
    }

    @Override
    public Single<AnswerVKOfficialList> getOfficial(Integer count, Integer startFrom, String filters, Long startTime, Long endTime) {
        return provideService(INotificationsService.class, TokenType.USER)
                .flatMap(service -> service.getOfficial(count, startFrom, filters, startTime, endTime, "photo_200_orig,photo_200")
                        .map(extractResponseWithErrorHandling()));
    }
}
