package dev.ragnarok.fenrir.api.impl;

import java.util.ArrayList;
import java.util.Collection;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.TokenType;
import dev.ragnarok.fenrir.api.interfaces.INewsfeedApi;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VkApiFeedList;
import dev.ragnarok.fenrir.api.model.response.NewsfeedCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.NewsfeedResponse;
import dev.ragnarok.fenrir.api.model.response.NewsfeedSearchResponse;
import dev.ragnarok.fenrir.api.services.INewsfeedService;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;


class NewsfeedApi extends AbsApi implements INewsfeedApi {

    NewsfeedApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Items<VkApiFeedList>> getLists(Collection<Integer> listIds) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.getLists(join(listIds, ","), 1)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> saveList(String title, Collection<Integer> listIds) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.saveList(title, join(listIds, ","))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> addBan(Collection<Integer> listIds) {
        ArrayList<Integer> users = new ArrayList<>();
        ArrayList<Integer> groups = new ArrayList<>();
        for (Integer i : listIds) {
            if (i < 0) {
                groups.add(Math.abs(i));
            } else {
                users.add(i);
            }
        }
        if (Utils.isEmpty(users)) {
            users = null;
        }
        if (Utils.isEmpty(groups)) {
            groups = null;
        }
        ArrayList<Integer> finalUsers = users;
        ArrayList<Integer> finalGroups = groups;
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.addBan(join(finalUsers, ","), join(finalGroups, ","))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> deleteList(Integer list_id) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.deleteList(list_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> ignoreItem(String type, Integer owner_id, Integer item_id) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.ignoreItem(type, owner_id, item_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedSearchResponse> search(String query, Boolean extended, Integer count, Double latitude, Double longitude, Long startTime, Long endTime, String startFrom, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .search(query, integerFromBoolean(extended), count, latitude, longitude,
                                startTime, endTime, startFrom, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedCommentsResponse> getComments(Integer count, String filters, String reposts, Long startTime, Long endTime, Integer lastCommentsCount, String startFrom, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getComments(count, filters, reposts, startTime, endTime, lastCommentsCount, startFrom, fields, null)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedCommentsResponse> getMentions(Integer owner_id, Integer count, Integer offset, Long startTime, Long endTime) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getMentions(owner_id, count, offset, startTime, endTime)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedResponse> get(String filters, Boolean returnBanned, Long startTime,
                                        Long endTime, Integer maxPhotoCount, String sourceIds,
                                        String startFrom, Integer count, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .get(filters, integerFromBoolean(returnBanned), startTime, endTime,
                                maxPhotoCount, sourceIds, startFrom, count, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedResponse> getRecommended(Long startTime, Long endTime,
                                                   Integer maxPhotoCount, String startFrom, Integer count, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getRecommended(startTime, endTime,
                                maxPhotoCount, startFrom, count, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedResponse> getFeedLikes(Integer maxPhotoCount, String startFrom, Integer count, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getFeedLikes(maxPhotoCount, startFrom, count, fields)
                        .map(extractResponseWithErrorHandling()));
    }
}
