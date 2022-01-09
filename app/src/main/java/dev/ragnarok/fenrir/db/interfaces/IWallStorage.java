package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.db.model.PostPatch;
import dev.ragnarok.fenrir.db.model.entity.OwnerEntities;
import dev.ragnarok.fenrir.db.model.entity.PostEntity;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.criteria.WallCriteria;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IWallStorage extends IStorage {

    @CheckResult
    Single<int[]> storeWallEntities(int accountId, @NonNull List<PostEntity> posts,
                                    @Nullable OwnerEntities owners,
                                    @Nullable IClearWallTask clearWall);

    @CheckResult
    Single<Integer> replacePost(int accountId, @NonNull PostEntity post);

    @CheckResult
    Single<PostEntity> getEditingPost(int accountId, int ownerId, @EditingPostType int type, boolean includeAttachment);

    @CheckResult
    Completable deletePost(int accountId, int dbid);

    @CheckResult
    Single<Optional<PostEntity>> findPostById(int accountId, int dbid);

    @CheckResult
    Single<Optional<PostEntity>> findPostById(int accountId, int ownerId, int vkpostId, boolean includeAttachment);

    Single<List<PostEntity>> findDbosByCriteria(@NonNull WallCriteria criteria);

    @CheckResult
    Completable update(int accountId, int ownerId, int postId, @NonNull PostPatch update);

    /**
     * Уведомить хранилище, что пост более не существует
     *
     * @param accountId
     * @param postVkid
     * @param postOwnerId
     * @return
     */
    Completable invalidatePost(int accountId, int postVkid, int postOwnerId);

    interface IClearWallTask {
        int getOwnerId();
    }
}