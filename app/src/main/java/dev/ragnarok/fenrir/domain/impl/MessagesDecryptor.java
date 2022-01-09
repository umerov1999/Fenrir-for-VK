package dev.ragnarok.fenrir.domain.impl;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.crypt.AesKeyPair;
import dev.ragnarok.fenrir.crypt.CryptHelper;
import dev.ragnarok.fenrir.crypt.EncryptedMessage;
import dev.ragnarok.fenrir.db.interfaces.IStorages;
import dev.ragnarok.fenrir.domain.IMessagesDecryptor;
import dev.ragnarok.fenrir.model.CryptStatus;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.util.Pair;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleTransformer;

public class MessagesDecryptor implements IMessagesDecryptor {

    private final IStorages store;

    public MessagesDecryptor(IStorages store) {
        this.store = store;
    }

    @Override
    public SingleTransformer<List<Message>, List<Message>> withMessagesDecryption(int accountId) {
        return single -> single
                .flatMap(messages -> {
                    List<Pair<Integer, Long>> sessions = new ArrayList<>(0);
                    List<Pair<Message, EncryptedMessage>> needDecryption = new ArrayList<>(0);

                    for (Message message : messages) {
                        if (message.getCryptStatus() != CryptStatus.ENCRYPTED) {
                            continue;
                        }

                        try {
                            EncryptedMessage em = CryptHelper.parseEncryptedMessage(message.getBody());

                            if (nonNull(em)) {
                                needDecryption.add(Pair.Companion.create(message, em));
                                sessions.add(Pair.Companion.create(em.getKeyLocationPolicy(), em.getSessionId()));
                            } else {
                                message.setCryptStatus(CryptStatus.DECRYPT_FAILED);
                            }
                        } catch (Exception e) {
                            message.setCryptStatus(CryptStatus.DECRYPT_FAILED);
                        }
                    }

                    if (needDecryption.isEmpty()) {
                        return Single.just(messages);
                    }

                    return getKeyPairs(accountId, sessions)
                            .map(keys -> {
                                for (Pair<Message, EncryptedMessage> pair : needDecryption) {
                                    Message message = pair.getFirst();
                                    EncryptedMessage em = pair.getSecond();

                                    try {
                                        AesKeyPair keyPair = keys.get(em.getSessionId());

                                        if (isNull(keyPair)) {
                                            message.setCryptStatus(CryptStatus.DECRYPT_FAILED);
                                            continue;
                                        }

                                        String key = message.isOut() ? keyPair.getMyAesKey() : keyPair.getHisAesKey();
                                        String decryptedBody = CryptHelper.decryptWithAes(em.getOriginalBody(), key);

                                        message.setDecryptedBody(decryptedBody);
                                        message.setCryptStatus(CryptStatus.DECRYPTED);
                                    } catch (Exception e) {
                                        message.setCryptStatus(CryptStatus.DECRYPT_FAILED);
                                    }
                                }

                                return messages;
                            });
                });
    }

    private Single<LongSparseArray<AesKeyPair>> getKeyPairs(int accountId, List<Pair<Integer, Long>> tokens) {
        return Single.create(emitter -> {
            LongSparseArray<AesKeyPair> keys = new LongSparseArray<>(tokens.size());

            for (Pair<Integer, Long> token : tokens) {
                if (emitter.isDisposed()) {
                    break;
                }

                long sessionId = token.getSecond();
                int keyPolicy = token.getFirst();

                AesKeyPair keyPair = store.keys(keyPolicy).findKeyPairFor(accountId, sessionId).blockingGet();

                if (nonNull(keyPair)) {
                    keys.append(sessionId, keyPair);
                }
            }

            emitter.onSuccess(keys);
        });
    }
}