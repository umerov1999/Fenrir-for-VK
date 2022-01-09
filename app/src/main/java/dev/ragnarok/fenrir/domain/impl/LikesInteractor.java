package dev.ragnarok.fenrir.domain.impl;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.api.model.VKApiOwner;
import dev.ragnarok.fenrir.domain.ILikesInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;


public class LikesInteractor implements ILikesInteractor {

    private final INetworker networker;

    public LikesInteractor(INetworker networker) {
        this.networker = networker;
    }

    @Override
    public Single<List<Owner>> getLikes(int accountId, String type, int ownerId, int itemId, String filter, int count, int offset) {
        return networker.vkDefault(accountId)
                .likes()
                .getList(type, ownerId, itemId, null, filter, null, offset, count, null, Constants.MAIN_OWNER_FIELDS)
                .map(response -> {
                    List<VKApiOwner> dtos = Utils.listEmptyIfNull(response.owners);
                    List<Owner> owners = new ArrayList<>(dtos.size());

                    for (VKApiOwner dto : dtos) {
                        owners.add(Dto2Model.transformOwner(dto));
                    }

                    return owners;
                });
    }
}