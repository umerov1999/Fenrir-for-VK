package dev.ragnarok.fenrir.domain;

import dev.ragnarok.fenrir.model.Chat;
import io.reactivex.rxjava3.core.Single;

public interface IDialogsInteractor {
    Single<Chat> getChatById(int accountId, int peerId);
}