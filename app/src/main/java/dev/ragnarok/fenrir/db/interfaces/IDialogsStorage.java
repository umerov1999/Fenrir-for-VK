package dev.ragnarok.fenrir.db.interfaces;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.VKApiChat;
import dev.ragnarok.fenrir.db.PeerStateEntity;
import dev.ragnarok.fenrir.db.model.PeerPatch;
import dev.ragnarok.fenrir.db.model.entity.DialogEntity;
import dev.ragnarok.fenrir.db.model.entity.KeyboardEntity;
import dev.ragnarok.fenrir.db.model.entity.SimpleDialogEntity;
import dev.ragnarok.fenrir.model.Chat;
import dev.ragnarok.fenrir.model.criteria.DialogsCriteria;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface IDialogsStorage extends IStorage {

    int getUnreadDialogsCount(int accountId);

    Observable<Pair<Integer, Integer>> observeUnreadDialogsCount();

    Single<List<PeerStateEntity>> findPeerStates(int accountId, Collection<Integer> ids);

    void setUnreadDialogsCount(int accountId, int unreadCount);

    Single<Optional<SimpleDialogEntity>> findSimple(int accountId, int peerId);

    Completable saveSimple(int accountId, @NonNull SimpleDialogEntity entity);

    Completable updateDialogKeyboard(int accountId, int peerId, @Nullable KeyboardEntity keyboardEntity);

    Single<List<DialogEntity>> getDialogs(@NonNull DialogsCriteria criteria);

    Completable removePeerWithId(int accountId, int peerId);

    Completable insertDialogs(int accountId, List<DialogEntity> dbos, boolean clearBefore);

    /**
     * Получение списка идентификаторов диалогов, информация о которых отсутствует в базе данных
     *
     * @param ids список входящих идентификаторов
     * @return отсутствующие
     */
    Single<Collection<Integer>> getMissingGroupChats(int accountId, Collection<Integer> ids);

    Completable insertChats(int accountId, List<VKApiChat> chats);

    Completable applyPatches(int accountId, @NonNull List<PeerPatch> patches);

    Single<Optional<Chat>> findChatById(int accountId, int peerId);
}