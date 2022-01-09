package dev.ragnarok.fenrir.push;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Single;

public class OwnerInfo {

    private final Owner owner;
    private final Bitmap avatar;

    private OwnerInfo(@NonNull Owner owner, Bitmap avatar) {
        this.owner = owner;
        this.avatar = avatar;
    }

    public static Single<OwnerInfo> getRx(@NonNull Context context, int accountId, int ownerId) {
        Context app = context.getApplicationContext();
        IOwnersRepository interactor = Repository.INSTANCE.getOwners();

        return interactor.getBaseOwnerInfo(accountId, ownerId, IOwnersRepository.MODE_ANY)
                .flatMap(owner -> Single.fromCallable(() -> NotificationUtils.loadRoundedImage(app, owner.get100photoOrSmaller(), R.drawable.ic_avatar_unknown))
                        .map(Optional::wrap)
                        .onErrorReturnItem(Optional.empty())
                        .map(optional -> new OwnerInfo(owner, optional.get())));
    }

    @NonNull
    public User getUser() {
        return (User) owner;
    }

    @NonNull
    public Owner getOwner() {
        return owner;
    }

    @NonNull
    public Community getCommunity() {
        return (Community) owner;
    }

    public Bitmap getAvatar() {
        return avatar;
    }
}