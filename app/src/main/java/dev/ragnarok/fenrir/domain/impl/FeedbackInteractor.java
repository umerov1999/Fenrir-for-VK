package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.domain.mappers.Entity2Model.fillCommentOwnerIds;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.feedback.VkApiBaseFeedback;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.db.model.IdPairEntity;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.feedback.CopyEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.FeedbackEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.LikeCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.LikeEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.MentionCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.MentionEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.NewCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.PostFeedbackEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.ReplyCommentEntity;
import dev.ragnarok.fenrir.db.model.entity.feedback.UsersEntity;
import dev.ragnarok.fenrir.domain.IFeedbackInteractor;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.mappers.Dto2Entity;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.domain.mappers.FeedbackEntity2Model;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.criteria.NotificationsCriteria;
import dev.ragnarok.fenrir.model.feedback.Feedback;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class FeedbackInteractor implements IFeedbackInteractor {

    private final IStorages cache;
    private final INetworker networker;
    private final IOwnersRepository ownersRepository;

    public FeedbackInteractor(IStorages cache, INetworker networker, IOwnersRepository ownersRepository) {
        this.cache = cache;
        this.networker = networker;
        this.ownersRepository = ownersRepository;
    }

    private static void populateOwnerIds(VKOwnIds ids, FeedbackEntity dbo) {
        fillCommentOwnerIds(ids, dbo.getReply());

        if (dbo instanceof CopyEntity) {
            populateOwnerIds(ids, (CopyEntity) dbo);
        } else if (dbo instanceof LikeCommentEntity) {
            populateOwnerIds(ids, (LikeCommentEntity) dbo);
        } else if (dbo instanceof LikeEntity) {
            populateOwnerIds(ids, (LikeEntity) dbo);
        } else if (dbo instanceof MentionCommentEntity) {
            populateOwnerIds(ids, (MentionCommentEntity) dbo);
        } else if (dbo instanceof MentionEntity) {
            populateOwnerIds(ids, (MentionEntity) dbo);
        } else if (dbo instanceof NewCommentEntity) {
            populateOwnerIds(ids, (NewCommentEntity) dbo);
        } else if (dbo instanceof PostFeedbackEntity) {
            populateOwnerIds(ids, (PostFeedbackEntity) dbo);
        } else if (dbo instanceof ReplyCommentEntity) {
            populateOwnerIds(ids, (ReplyCommentEntity) dbo);
        } else if (dbo instanceof UsersEntity) {
            populateOwnerIds(ids, (UsersEntity) dbo);
        }
    }

    private static void populateOwnerIds(VKOwnIds ids, UsersEntity dbo) {
        ids.appendAll(dbo.getOwners());
    }

    private static void populateOwnerIds(VKOwnIds ids, ReplyCommentEntity dbo) {
        Entity2Model.fillOwnerIds(ids, dbo.getCommented());
        Entity2Model.fillOwnerIds(ids, dbo.getFeedbackComment());
        Entity2Model.fillOwnerIds(ids, dbo.getOwnComment());
    }

    private static void populateOwnerIds(VKOwnIds ids, PostFeedbackEntity dbo) {
        Entity2Model.fillOwnerIds(ids, dbo.getPost());
    }

    private static void populateOwnerIds(VKOwnIds ids, NewCommentEntity dbo) {
        Entity2Model.fillOwnerIds(ids, dbo.getComment());
        Entity2Model.fillOwnerIds(ids, dbo.getCommented());
    }

    private static void populateOwnerIds(VKOwnIds ids, MentionEntity dbo) {
        Entity2Model.fillOwnerIds(ids, dbo.getWhere());
    }

    private static void populateOwnerIds(VKOwnIds ids, MentionCommentEntity dbo) {
        Entity2Model.fillOwnerIds(ids, dbo.getCommented());
        Entity2Model.fillOwnerIds(ids, dbo.getWhere());
    }

    private static void populateOwnerIds(VKOwnIds ids, LikeEntity dbo) {
        Entity2Model.fillOwnerIds(ids, dbo.getLiked());
        ids.appendAll(dbo.getLikesOwnerIds());
    }

    private static void populateOwnerIds(VKOwnIds ids, LikeCommentEntity dbo) {
        Entity2Model.fillOwnerIds(ids, dbo.getLiked());
        Entity2Model.fillOwnerIds(ids, dbo.getCommented());
        ids.appendAll(dbo.getLikesOwnerIds());
    }

    private static void populateOwnerIds(VKOwnIds ids, CopyEntity dbo) {
        for (IdPairEntity i : dbo.getCopies().getPairDbos()) {
            ids.append(i.getOwnerId());
        }

        Entity2Model.fillOwnerIds(ids, dbo.getCopied());
    }

    @Override
    public Single<List<Feedback>> getCachedFeedbacks(int accountId) {
        NotificationsCriteria criteria = new NotificationsCriteria(accountId);
        return getCachedFeedbacksByCriteria(criteria);
    }

    @Override
    public Single<AnswerVKOfficialList> getOfficial(int accountId, Integer count, Integer startFrom) {
        return networker.vkDefault(accountId)
                .notifications()
                .getOfficial(count, startFrom, null, null, null)
                .map(response -> response);
    }

    @Override
    public Single<Pair<List<Feedback>, String>> getActualFeedbacks(int accountId, int count, String startFrom) {
        return networker.vkDefault(accountId)
                .notifications()
                .get(count, startFrom, null, null, null)
                .flatMap(response -> {
                    List<VkApiBaseFeedback> dtos = Utils.listEmptyIfNull(response.notifications);
                    List<FeedbackEntity> dbos = new ArrayList<>(dtos.size());

                    VKOwnIds ownIds = new VKOwnIds();

                    for (VkApiBaseFeedback dto : dtos) {
                        FeedbackEntity dbo = Dto2Entity.buildFeedbackDbo(dto);
                        populateOwnerIds(ownIds, dbo);
                        dbos.add(dbo);
                    }

                    OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    return cache.notifications()
                            .insert(accountId, dbos, ownerEntities, isEmpty(startFrom))
                            .flatMap(ints -> ownersRepository
                                    .findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY, owners)
                                    .map(ownersBundle -> {
                                        List<Feedback> feedbacks = new ArrayList<>(dbos.size());

                                        for (FeedbackEntity dbo : dbos) {
                                            feedbacks.add(FeedbackEntity2Model.buildFeedback(dbo, ownersBundle));
                                        }

                                        return Pair.Companion.create(feedbacks, response.nextFrom);
                                    }));
                });
    }

    @Override
    public Completable maskAaViewed(int accountId) {
        return networker.vkDefault(accountId)
                .notifications()
                .markAsViewed()
                .ignoreElement();
    }

    private Single<List<Feedback>> getCachedFeedbacksByCriteria(NotificationsCriteria criteria) {
        return cache.notifications()
                .findByCriteria(criteria)
                .flatMap(dbos -> {
                    VKOwnIds ownIds = new VKOwnIds();

                    for (FeedbackEntity dbo : dbos) {
                        populateOwnerIds(ownIds, dbo);
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(criteria.getAccountId(), ownIds.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> {
                                List<Feedback> feedbacks = new ArrayList<>(dbos.size());
                                for (FeedbackEntity dbo : dbos) {
                                    feedbacks.add(FeedbackEntity2Model.buildFeedback(dbo, owners));
                                }
                                return feedbacks;
                            });
                });
    }
}