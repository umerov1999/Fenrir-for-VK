package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.Message;
import io.reactivex.rxjava3.core.SingleTransformer;

public interface IMessagesDecryptor {
    /**
     * Предоставляет RX-трансформер для дешифровки сообщений
     *
     * @param accountId идентификатор аккаунта
     * @return RX-трансформер
     */
    SingleTransformer<List<Message>, List<Message>> withMessagesDecryption(int accountId);
}