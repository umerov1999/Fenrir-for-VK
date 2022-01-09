package dev.ragnarok.fenrir.domain.impl;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.db.AttachToType;
import dev.ragnarok.fenrir.db.interfaces.IAttachmentsStorage;
import dev.ragnarok.fenrir.db.model.entity.Entity;
import dev.ragnarok.fenrir.domain.IAttachmentsRepository;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.mappers.Entity2Model;
import dev.ragnarok.fenrir.domain.mappers.Model2Entity;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.VKOwnIds;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class AttachmentsRepository implements IAttachmentsRepository {

    private final PublishSubject<IAddEvent> addPublishSubject;
    private final PublishSubject<IRemoveEvent> removePublishSubject;

    private final IAttachmentsStorage store;
    private final IOwnersRepository ownersRepository;

    public AttachmentsRepository(IAttachmentsStorage store, IOwnersRepository ownersRepository) {
        this.store = store;
        this.ownersRepository = ownersRepository;
        addPublishSubject = PublishSubject.create();
        removePublishSubject = PublishSubject.create();
    }

    @Override
    public Completable remove(int accountId, int attachToType, int attachToDbid, int generatedAttachmentId) {
        return store.remove(accountId, attachToType, attachToDbid, generatedAttachmentId)
                .doOnComplete(() -> {
                    RemoveEvent event = new RemoveEvent(accountId, attachToType, attachToDbid, generatedAttachmentId);
                    removePublishSubject.onNext(event);
                });
    }

    @Override
    public Completable attach(int accountId, int attachToType, int attachToDbid, @NonNull List<? extends AbsModel> models) {
        List<Entity> entities = Model2Entity.buildDboAttachments(models);

        return store.attachDbos(accountId, attachToType, attachToDbid, entities)
                .doAfterSuccess(ids -> {
                    List<Pair<Integer, AbsModel>> events = new ArrayList<>(models.size());

                    for (int i = 0; i < models.size(); i++) {
                        AbsModel model = models.get(i);
                        int generatedId = ids[i];
                        events.add(Pair.Companion.create(generatedId, model));
                    }

                    AddEvent event = new AddEvent(accountId, attachToType, attachToDbid, events);
                    addPublishSubject.onNext(event);
                })
                .ignoreElement();
    }

    @Override
    public Single<List<Pair<Integer, AbsModel>>> getAttachmentsWithIds(int accountId, int attachToType, int attachToDbid) {
        return store.getAttachmentsDbosWithIds(accountId, attachToType, attachToDbid)
                .flatMap(pairs -> {
                    VKOwnIds ids = new VKOwnIds();

                    for (Pair<Integer, Entity> pair : pairs) {
                        Entity2Model.fillOwnerIds(ids, pair.getSecond());
                    }

                    return ownersRepository
                            .findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> {
                                List<Pair<Integer, AbsModel>> models = new ArrayList<>(pairs.size());

                                for (Pair<Integer, Entity> pair : pairs) {
                                    AbsModel model = Entity2Model.buildAttachmentFromDbo(pair.getSecond(), owners);
                                    models.add(Pair.Companion.create(pair.getFirst(), model));
                                }

                                return models;
                            });
                });
    }

    @Override
    public Observable<IAddEvent> observeAdding() {
        return addPublishSubject;
    }

    @Override
    public Observable<IRemoveEvent> observeRemoving() {
        return removePublishSubject;
    }

    private static final class AddEvent extends Event implements IAddEvent {

        final List<Pair<Integer, AbsModel>> attachments;

        private AddEvent(int accountId, @AttachToType int attachToType, int attachToId, List<Pair<Integer, AbsModel>> attachments) {
            super(accountId, attachToType, attachToId);
            this.attachments = attachments;
        }

        @Override
        public List<Pair<Integer, AbsModel>> getAttachments() {
            return attachments;
        }
    }

    private static class Event implements IBaseEvent {

        @AttachToType
        final int attachToType;
        final int attachToId;
        final int accountId;

        private Event(int accountId, int attachToType, int attachToId) {
            this.accountId = accountId;
            this.attachToType = attachToType;
            this.attachToId = attachToId;
        }

        @Override
        public int getAccountId() {
            return accountId;
        }

        @Override
        public int getAttachToType() {
            return attachToType;
        }

        @Override
        public int getAttachToId() {
            return attachToId;
        }
    }

    private class RemoveEvent extends Event implements IRemoveEvent {

        final int generatedId;

        private RemoveEvent(int accountId, @AttachToType int attachToType, int attachToId, int generatedId) {
            super(accountId, attachToType, attachToId);
            this.generatedId = generatedId;
        }

        @Override
        public int getGeneratedId() {
            return generatedId;
        }
    }
}