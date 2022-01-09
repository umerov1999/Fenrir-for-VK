package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IWallsRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.mvp.view.IRepostView;
import dev.ragnarok.fenrir.util.RxUtils;


public class RepostPresenter extends AbsAttachmentsEditPresenter<IRepostView> {

    private final Post post;
    private final Integer targetGroupId;
    private final IWallsRepository walls;
    private boolean publishingNow;

    public RepostPresenter(int accountId, Post post, Integer targetGroupId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        walls = Repository.INSTANCE.getWalls();
        this.post = post;
        this.targetGroupId = targetGroupId;

        getData().add(new AttachmentEntry(false, post));
    }

    @Override
    public void onGuiCreated(@NonNull IRepostView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.setSupportedButtons(false, false, false, false, false, false);

        resolveProgressDialog();
    }

    private void resolveProgressDialog() {

        if (publishingNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.publication, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    private void setPublishingNow(boolean publishingNow) {
        this.publishingNow = publishingNow;
        resolveProgressDialog();
    }

    private void onPublishError(Throwable throwable) {
        setPublishingNow(false);
        callView(v -> showError(v, throwable));
    }

    @SuppressWarnings("unused")
    private void onPublishComplete(Post post) {
        setPublishingNow(false);
        callView(IRepostView::goBack);
    }

    public final void fireReadyClick() {
        setPublishingNow(true);

        int accountId = getAccountId();
        String body = getTextBody();

        appendDisposable(walls.repost(accountId, post.getVkid(), post.getOwnerId(), targetGroupId, body)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onPublishComplete, this::onPublishError));
    }
}