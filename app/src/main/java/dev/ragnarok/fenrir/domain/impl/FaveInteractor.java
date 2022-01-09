package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.domain.mappers.MapUtil.mapAll;
import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.listEmptyIfNull;
import static dev.ragnarok.fenrir.util.Utils.safeCountOf;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.FaveLinkDto;
import dev.ragnarok.fenrir.api.model.VKApiArticle;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.VkApiMarket;
import dev.ragnarok.fenrir.api.model.response.FavePageResponse;
import dev.ragnarok.fenrir.db.column.UserColumns;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.entity.ArticleEntity;
import dev.ragnarok.fenrir.db.model.entity.CommunityEntity;
import dev.ragnarok.fenrir.db.model.entity.FaveLinkEntity;
import dev.ragnarok.fenrir.db.model.entity.FavePageEntity;
import dev.ragnarok.fenrir.db.model.entity.MarketEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PhotoEntity;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.db.model.entity.UserEntity;
import dev.ragnarok.fenrir.db.model.entity.VideoEntity;
import dev.ragnarok.fenrir.domain.IFaveInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.FaveLink;
import dev.ragnarok.fenrir.model.FavePage;
import dev.ragnarok.fenrir.model.FavePageType;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.criteria.FaveArticlesCriteria;
import dev.ragnarok.fenrir.model.criteria.FavePhotosCriteria;
import dev.ragnarok.fenrir.model.criteria.FavePostsCriteria;
import dev.ragnarok.fenrir.model.criteria.FaveProductsCriteria;
import dev.ragnarok.fenrir.model.criteria.FaveVideosCriteria;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class FaveInteractor implements IFaveInteractor {

    private final INetworker networker;
    private final IStorages cache;
    private final IOwnersRepository ownersRepository;

    public FaveInteractor(INetworker networker, IStorages cache, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.cache = cache;
        this.ownersRepository = ownersRepository;
    }

    private static FaveLink createLinkFromEntity(FaveLinkEntity entity) {
        return new FaveLink(entity.getId())
                .setDescription(entity.getDescription())
                .setPhoto(nonNull(entity.getPhoto()) ? Entity2Model.map(entity.getPhoto()) : null)
                .setTitle(entity.getTitle())
                .setUrl(entity.getUrl());
    }

    private static FaveLinkEntity createLinkEntityFromDto(FaveLinkDto dto) {
        return new FaveLinkEntity(dto.id, dto.url)
                .setDescription(dto.description)
                .setTitle(dto.title)
                .setPhoto(nonNull(dto.photo) ? Dto2Entity.mapPhoto(dto.photo) : null);
    }

    @Override
    public Single<List<Post>> getPosts(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getPosts(offset, count)
                .flatMap(response -> {
                    List<VkApiAttachments.Entry> dtos = listEmptyIfNull(response.posts);

                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ids = new VKOwnIds();
                    for (VkApiAttachments.Entry dto : dtos) {
                        if (dto.attachment instanceof VKApiPost)
                            ids.append((VKApiPost) dto.attachment);
                    }

                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);

                    List<PostEntity> dbos = new ArrayList<>(safeCountOf(response.posts));
                    if (nonNull(response.posts)) {
                        for (VkApiAttachments.Entry dto : response.posts) {
                            if (dto.attachment instanceof VKApiPost)
                                dbos.add(Dto2Entity.mapPost((VKApiPost) dto.attachment));
                        }
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .map(bundle -> Dto2Model.transformAttachmentsPosts(dtos, bundle))
                            .flatMap(posts -> cache.fave()
                                    .storePosts(accountId, dbos, ownerEntities, offset == 0)
                                    .andThen(Single.just(posts)));
                });
    }

    @Override
    public Single<List<Post>> getCachedPosts(int accountId) {
        return cache.fave().getFavePosts(new FavePostsCriteria(accountId))
                .flatMap(postDbos -> {
                    VKOwnIds ids = new VKOwnIds();
                    for (PostEntity dbo : postDbos) {
                        Entity2Model.fillPostOwnerIds(ids, dbo);
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> {
                                List<Post> posts = new ArrayList<>();
                                for (PostEntity dbo : postDbos) {
                                    posts.add(Entity2Model.buildPostFromDbo(dbo, owners));
                                }
                                return posts;
                            });
                });
    }

    @Override
    public Single<List<Photo>> getCachedPhotos(int accountId) {
        FavePhotosCriteria criteria = new FavePhotosCriteria(accountId);
        return cache.fave()
                .getPhotos(criteria)
                .map(photoDbos -> {
                    List<Photo> photos = new ArrayList<>(photoDbos.size());
                    for (PhotoEntity dbo : photoDbos) {
                        photos.add(Entity2Model.map(dbo));
                    }
                    return photos;
                });
    }

    @Override
    public Single<List<Photo>> getPhotos(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getPhotos(offset, count)
                .flatMap(items -> {
                    List<VKApiPhoto> dtos = listEmptyIfNull(items.getItems());

                    List<PhotoEntity> dbos = new ArrayList<>(dtos.size());
                    List<Photo> photos = new ArrayList<>(dtos.size());

                    for (VKApiPhoto dto : dtos) {
                        dbos.add(Dto2Entity.mapPhoto(dto));
                        photos.add(Dto2Model.transform(dto));
                    }

                    return cache.fave().storePhotos(accountId, dbos, offset == 0)
                            .map(ints -> photos);
                });
    }

    @Override
    public Single<List<Video>> getCachedVideos(int accountId) {
        FaveVideosCriteria criteria = new FaveVideosCriteria(accountId);

        return cache.fave()
                .getVideos(criteria)
                .map(videoDbos -> {
                    List<Video> videos = new ArrayList<>(videoDbos.size());
                    for (VideoEntity dbo : videoDbos) {
                        videos.add(Entity2Model.buildVideoFromDbo(dbo));
                    }
                    return videos;
                });
    }

    @Override
    public Single<List<Article>> getCachedArticles(int accountId) {
        FaveArticlesCriteria criteria = new FaveArticlesCriteria(accountId);

        return cache.fave()
                .getArticles(criteria)
                .map(articleDbos -> {
                    List<Article> articles = new ArrayList<>(articleDbos.size());
                    for (ArticleEntity dbo : articleDbos) {
                        articles.add(Entity2Model.buildArticleFromDbo(dbo));
                    }
                    return articles;
                });
    }

    @Override
    public Single<List<Market>> getCachedProducts(int accountId) {
        FaveProductsCriteria criteria = new FaveProductsCriteria(accountId);

        return cache.fave()
                .getProducts(criteria)
                .map(productDbos -> {
                    List<Market> markets = new ArrayList<>(productDbos.size());
                    for (MarketEntity dbo : productDbos) {
                        markets.add(Entity2Model.buildMarketFromDbo(dbo));
                    }
                    return markets;
                });
    }

    @Override
    public Single<List<Video>> getVideos(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getVideos(offset, count)
                .flatMap(items -> {
                    List<VKApiVideo> dtos = listEmptyIfNull(items);

                    List<VideoEntity> dbos = new ArrayList<>(dtos.size());
                    List<Video> videos = new ArrayList<>(dtos.size());

                    for (VKApiVideo dto : dtos) {
                        dbos.add(Dto2Entity.mapVideo(dto));
                        videos.add(Dto2Model.transform(dto));
                    }

                    return cache.fave().storeVideos(accountId, dbos, offset == 0)
                            .map(ints -> videos);
                });
    }

    @Override
    public Single<List<Article>> getArticles(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getArticles(offset, count)
                .flatMap(items -> {
                    List<VKApiArticle> dtos = listEmptyIfNull(items);

                    List<ArticleEntity> dbos = new ArrayList<>(dtos.size());
                    List<Article> articles = new ArrayList<>(dtos.size());

                    for (VKApiArticle dto : dtos) {
                        dbos.add(Dto2Entity.mapArticle(dto));
                        articles.add(Dto2Model.transform(dto));
                    }

                    return cache.fave().storeArticles(accountId, dbos, offset == 0)
                            .map(ints -> articles);
                });
    }

    @Override
    public Single<List<Market>> getProducts(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getProducts(offset, count)
                .flatMap(items -> {
                    List<VkApiMarket> dtos = listEmptyIfNull(items);

                    List<MarketEntity> dbos = new ArrayList<>(dtos.size());
                    List<Market> markets = new ArrayList<>(dtos.size());

                    for (VkApiMarket dto : dtos) {
                        dbos.add(Dto2Entity.mapMarket(dto));
                        markets.add(Dto2Model.transform(dto));
                    }

                    return cache.fave().storeProducts(accountId, dbos, offset == 0)
                            .map(ints -> markets);
                });
    }

    @Override
    public Single<List<Article>> getOwnerPublishedArticles(int accountId, int ownerId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getOwnerPublishedArticles(ownerId, offset, count)
                .map(items -> {
                    List<VKApiArticle> dtos = listEmptyIfNull(items.items);

                    List<Article> articles = new ArrayList<>(dtos.size());

                    for (VKApiArticle dto : dtos) {
                        articles.add(Dto2Model.transform(dto));
                    }

                    return articles;
                });
    }

    @Override
    public Single<List<FavePage>> getCachedPages(int accountId, boolean isUser) {
        if (isUser) {
            return cache.fave()
                    .getFaveUsers(accountId)
                    .map(Entity2Model::buildFaveUsersFromDbo);
        } else {
            return cache.fave()
                    .getFaveGroups(accountId)
                    .map(Entity2Model::buildFaveUsersFromDbo);
        }
    }

    @Override
    public Single<List<FavePage>> getPages(int accountId, int count, int offset, boolean isUser) {
        return networker.vkDefault(accountId)
                .fave()
                .getPages(offset, count, UserColumns.API_FIELDS, isUser ? "users" : "groups")
                .flatMap(items -> {
                    List<FavePageResponse> dtos = listEmptyIfNull(items.getItems());

                    List<UserEntity> userEntities = new ArrayList<>();
                    List<CommunityEntity> communityEntities = new ArrayList<>();
                    for (FavePageResponse item : dtos) {
                        switch (item.type) {
                            case FavePageType.USER:
                                userEntities.add(Dto2Entity.mapUser(item.user));
                                break;
                            case FavePageType.COMMUNITY:
                                communityEntities.add(Dto2Entity.mapCommunity(item.group));
                                break;
                        }
                    }

                    List<FavePageEntity> entities = mapAll(dtos, Dto2Entity::mapFavePage, true);
                    List<FavePage> pages = mapAll(dtos, Dto2Model::transformFaveUser, true);

                    if (isUser) {
                        return cache.fave()
                                .storePages(accountId, entities, offset == 0)
                                .andThen(cache.owners().storeOwnerEntities(accountId, new OwnerEntities(userEntities, communityEntities)))
                                .andThen(Single.just(pages));
                    } else {
                        return cache.fave()
                                .storeGroups(accountId, entities, offset == 0)
                                .andThen(cache.owners().storeOwnerEntities(accountId, new OwnerEntities(userEntities, communityEntities)))
                                .andThen(Single.just(pages));
                    }
                });
    }

    @Override
    public Single<List<FaveLink>> getCachedLinks(int accountId) {
        return cache.fave()
                .getFaveLinks(accountId)
                .map(entities -> {
                    List<FaveLink> links = new ArrayList<>(entities.size());

                    for (FaveLinkEntity entity : entities) {
                        links.add(createLinkFromEntity(entity));
                    }

                    return links;
                });
    }

    @Override
    public Single<List<FaveLink>> getLinks(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getLinks(offset, count)
                .flatMap(items -> {
                    List<FaveLinkDto> dtos = listEmptyIfNull(items.getItems());
                    List<FaveLink> links = new ArrayList<>(dtos.size());
                    List<FaveLinkEntity> entities = new ArrayList<>(dtos.size());

                    for (FaveLinkDto dto : dtos) {
                        FaveLinkEntity entity = createLinkEntityFromDto(dto);
                        links.add(createLinkFromEntity(entity));
                        entities.add(entity);
                    }

                    return cache.fave()
                            .storeLinks(accountId, entities, offset == 0)
                            .andThen(Single.just(links));
                });
    }

    @Override
    public Completable removeLink(int accountId, String id) {
        return networker.vkDefault(accountId)
                .fave()
                .removeLink(id)
                .flatMapCompletable(ignore -> cache.fave()
                        .removeLink(accountId, id));
    }

    @Override
    public Single<Boolean> removeArticle(int accountId, Integer owner_id, Integer article_id) {
        return networker.vkDefault(accountId)
                .fave()
                .removeArticle(owner_id, article_id);
    }

    @Override
    public Single<Boolean> removeProduct(int accountId, Integer id, Integer owner_id) {
        return networker.vkDefault(accountId)
                .fave()
                .removeProduct(id, owner_id);
    }

    @Override
    public Single<Boolean> removePost(int accountId, Integer owner_id, Integer id) {
        return networker.vkDefault(accountId)
                .fave()
                .removePost(owner_id, id);
    }

    @Override
    public Single<Boolean> removeVideo(int accountId, Integer owner_id, Integer id) {
        return networker.vkDefault(accountId)
                .fave()
                .removeVideo(owner_id, id);
    }

    @Override
    public Single<Boolean> pushFirst(int accountId, int owner_id) {
        return networker.vkDefault(accountId)
                .fave()
                .pushFirst(owner_id);
    }

    @Override
    public Completable addPage(int accountId, int ownerId) {
        return networker.vkDefault(accountId)
                .fave()
                .addPage(ownerId > 0 ? ownerId : null, ownerId < 0 ? Math.abs(ownerId) : null)
                .ignoreElement();
    }

    @Override
    public Completable addLink(int accountId, String link) {
        return networker.vkDefault(accountId)
                .fave()
                .addLink(link)
                .ignoreElement();
    }

    @Override
    public Completable addVideo(int accountId, Integer owner_id, Integer id, String access_key) {
        return networker.vkDefault(accountId)
                .fave()
                .addVideo(owner_id, id, access_key)
                .ignoreElement();
    }

    @Override
    public Completable addArticle(int accountId, String url) {
        return networker.vkDefault(accountId)
                .fave()
                .addArticle(url)
                .ignoreElement();
    }

    @Override
    public Completable addProduct(int accountId, int id, int owner_id, String access_key) {
        return networker.vkDefault(accountId)
                .fave()
                .addProduct(id, owner_id, access_key)
                .ignoreElement();
    }

    @Override
    public Completable addPost(int accountId, Integer owner_id, Integer id, String access_key) {
        return networker.vkDefault(accountId)
                .fave()
                .addPost(owner_id, id, access_key)
                .ignoreElement();
    }

    @Override
    public Completable removePage(int accountId, int ownerId, boolean isUser) {
        return networker.vkDefault(accountId)
                .fave()
                .removePage(ownerId > 0 ? ownerId : null, ownerId < 0 ? Math.abs(ownerId) : null)
                .flatMapCompletable(ignored -> cache.fave().removePage(accountId, ownerId, isUser));
    }
}
