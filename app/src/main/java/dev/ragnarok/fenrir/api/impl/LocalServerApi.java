package dev.ragnarok.fenrir.api.impl;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import dev.ragnarok.fenrir.api.ILocalServerServiceProvider;
import dev.ragnarok.fenrir.api.interfaces.ILocalServerApi;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;

class LocalServerApi implements ILocalServerApi {

    private final ILocalServerServiceProvider service;

    LocalServerApi(ILocalServerServiceProvider service) {
        this.service = service;
    }

    static <T> Function<BaseResponse<T>, T> extractResponseWithErrorHandling() {
        return response -> {
            if (nonNull(response.error)) {
                throw Exceptions.propagate(new Exception(Utils.firstNonEmptyString(response.error.errorMsg, "Error")));
            }

            return response.response;
        };
    }

    @Override
    public Single<Items<VKApiVideo>> getVideos(Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.getVideos(offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> getAudios(Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.getAudios(offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> getPhotos(Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.getPhotos(offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> getDiscography(Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.getDiscography(offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiVideo>> searchVideos(String query, Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.searchVideos(query, offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiPhoto>> searchPhotos(String query, Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.searchPhotos(query, offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> searchAudios(String query, Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.searchAudios(query, offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudio>> searchDiscography(String query, Integer offset, Integer count, boolean reverse) {
        return service.provideLocalServerService()
                .flatMap(service -> service.searchDiscography(query, offset, count, reverse ? 1 : 0)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> update_time(String hash) {
        return service.provideLocalServerService()
                .flatMap(service -> service.update_time(hash)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> delete_media(String hash) {
        return service.provideLocalServerService()
                .flatMap(service -> service.delete_media(hash)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<String> get_file_name(String hash) {
        return service.provideLocalServerService()
                .flatMap(service -> service.get_file_name(hash)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> update_file_name(String hash, String name) {
        return service.provideLocalServerService()
                .flatMap(service -> service.update_file_name(hash, name)
                        .map(extractResponseWithErrorHandling()));
    }
}
