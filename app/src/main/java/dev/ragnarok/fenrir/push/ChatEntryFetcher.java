package dev.ragnarok.fenrir.push;

import android.content.Context;
import android.graphics.Bitmap;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.Mode;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Single;

public class ChatEntryFetcher {

    public static Single<DialogInfo> getRx(Context context, int accountId, int peerId) {
        Context app = context.getApplicationContext();

        switch (Peer.getType(peerId)) {
            case Peer.USER:
            case Peer.GROUP:
                int ownerId = Peer.toOwnerId(peerId);
                return OwnerInfo.getRx(app, accountId, ownerId)
                        .map(info -> {
                            Owner owner = info.getOwner();

                            DialogInfo response = new DialogInfo();
                            response.title = owner.getFullName();
                            response.img = owner.get100photoOrSmaller();
                            response.icon = info.getAvatar();
                            return response;
                        });
            case Peer.CHAT:
                return Repository.INSTANCE.getMessages()
                        .getConversation(accountId, peerId, Mode.ANY).singleOrError()
                        .flatMap(chat -> NotificationUtils.loadRoundedImageRx(app, chat.get100orSmallerAvatar(), R.drawable.ic_group_chat)
                                .map(Optional::wrap)
                                .onErrorReturnItem(Optional.empty())
                                .map(optional -> {
                                    DialogInfo response = new DialogInfo();
                                    response.title = chat.getTitle();
                                    response.img = chat.get100orSmallerAvatar();
                                    response.icon = optional.get();
                                    return response;
                                }));
        }

        throw new UnsupportedOperationException();
    }

    public static class DialogInfo {
        public String title;
        public String img;
        public Bitmap icon;
    }
}