package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.Owner;
import io.reactivex.rxjava3.core.Single;

public interface ILikesInteractor {
    String FILTER_LIKES = "likes";
    String FILTER_COPIES = "copies";

    Single<List<Owner>> getLikes(int accountId, String type, int ownerId, int itemId, String filter, int count, int offset);
}