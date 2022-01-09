package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.mvp.view.IFwdsView;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class FwdsPresenter extends AbsMessageListPresenter<IFwdsView> {

    public FwdsPresenter(int accountId, List<Message> messages, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        if (!Utils.isEmpty(messages)) {
            getData().addAll(messages);
        }
    }

    public void fireTranscript(String voiceMessageId, int messageId) {
        appendDisposable(Repository.INSTANCE.getMessages().recogniseAudioMessage(getAccountId(), messageId, voiceMessageId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(v -> {
                }, t -> {
                }));
    }
}