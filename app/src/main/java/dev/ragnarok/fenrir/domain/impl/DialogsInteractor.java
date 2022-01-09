package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.domain.IDialogsInteractor;
import dev.ragnarok.fenrir.domain.mappers.Dto2Model;
import dev.ragnarok.fenrir.exception.NotFoundException;
import dev.ragnarok.fenrir.model.Chat;
import dev.ragnarok.fenrir.model.Peer;
import io.reactivex.rxjava3.core.Single;

public class DialogsInteractor implements IDialogsInteractor {

    private final INetworker networker;

    private final IStorages repositories;

    public DialogsInteractor(INetworker networker, IStorages repositories) {
        this.networker = networker;
        this.repositories = repositories;
    }

    @Override
    public Single<Chat> getChatById(int accountId, int peerId) {
        return repositories.dialogs()
                .findChatById(accountId, peerId)
                .flatMap(optional -> {
                    if (optional.nonEmpty()) {
                        return Single.just(optional.get());
                    }

                    int chatId = Peer.toChatId(peerId);
                    return networker.vkDefault(accountId)
                            .messages()
                            .getChat(chatId, null, null, null)
                            .map(chats -> {
                                if (isEmpty(chats)) {
                                    throw new NotFoundException();
                                }

                                return chats.get(0);
                            })
                            .map(Dto2Model::transform);
                });
    }
}
