package dev.ragnarok.fenrir.mvp.presenter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.BaseTransientBottomBar;

import java.util.ArrayList;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.media.gif.IGifPlayer;
import dev.ragnarok.fenrir.media.gif.PlayerPrepareException;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.VideoSize;
import dev.ragnarok.fenrir.mvp.view.IBasicDocumentView;
import dev.ragnarok.fenrir.mvp.view.IGifPagerView;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class GifPagerPresenter extends BaseDocumentPresenter<IGifPagerView> implements IGifPlayer.IStatusChangeListener, IGifPlayer.IVideoSizeChangeListener {

    private static final String SAVE_PAGER_INDEX = "save_pager_index";
    private static final VideoSize DEF_SIZE = new VideoSize(1, 1);
    private final ArrayList<Document> mDocuments;
    private IGifPlayer mGifPlayer;
    private int mCurrentIndex;

    public GifPagerPresenter(int accountId, @NonNull ArrayList<Document> documents, int index, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        mDocuments = documents;

        if (savedInstanceState == null) {
            mCurrentIndex = index;
        } else {
            mCurrentIndex = savedInstanceState.getInt(SAVE_PAGER_INDEX);
        }

        initGifPlayer();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_PAGER_INDEX, mCurrentIndex);
    }

    public void fireSurfaceCreated(int adapterPosition) {
        if (mCurrentIndex == adapterPosition) {
            resolvePlayerDisplay();
        }
    }

    private void resolveToolbarTitle() {
        callView(v -> v.setToolbarTitle(R.string.gif_player));
    }

    private void resolveToolbarSubtitle() {
        callView(v -> v.setToolbarSubtitle(R.string.image_number, mCurrentIndex + 1, mDocuments.size()));
    }

    private void resolvePlayerDisplay() {
        if (getGuiIsReady()) {
            callView(v -> v.attachDisplayToPlayer(mCurrentIndex, mGifPlayer));
        } else {
            mGifPlayer.setDisplay(null);
        }
    }

    private void initGifPlayer() {
        if (Objects.nonNull(mGifPlayer)) {
            IGifPlayer old = mGifPlayer;
            mGifPlayer = null;
            old.release();
        }

        Document document = mDocuments.get(mCurrentIndex);
        AssertUtils.requireNonNull(document);

        String url = document.getVideoPreview().getSrc();

        mGifPlayer = Injection.provideGifPlayerFactory().createGifPlayer(url, true);
        mGifPlayer.addStatusChangeListener(this);
        mGifPlayer.addVideoSizeChangeListener(this);

        try {
            mGifPlayer.play();
        } catch (PlayerPrepareException e) {
            callView(v -> v.showToast(R.string.unable_to_play_file, true));
        }
    }

    private void selectPage(int position) {
        if (mCurrentIndex == position) {
            return;
        }

        mCurrentIndex = position;
        initGifPlayer();
    }

    private void resolveAddDeleteButton() {
        callView(v -> v.setupAddRemoveButton(!isMy()));
    }

    private boolean isMy() {
        return mDocuments.get(mCurrentIndex).getOwnerId() == getAccountId();
    }

    private void resolveAspectRatio() {
        VideoSize size = mGifPlayer.getVideoSize();
        if (size != null) {
            callView(v -> v.setAspectRatioAt(mCurrentIndex, size.getWidth(), size.getHeight()));
        }
    }

    private void resolvePreparingProgress() {
        boolean preparing = !Objects.isNull(mGifPlayer) && mGifPlayer.getPlayerStatus() == IGifPlayer.IStatus.PREPARING;
        callView(v -> v.setPreparingProgressVisible(mCurrentIndex, preparing));
    }

    @Override
    public void onGuiCreated(@NonNull IGifPagerView view) {
        super.onGuiCreated(view);
        view.displayData(mDocuments.size(), mCurrentIndex);

        resolvePreparingProgress();
        resolveAspectRatio();
        resolveAddDeleteButton();
        resolvePlayerDisplay();
        resolveToolbarTitle();
        resolveToolbarSubtitle();
    }

    public void firePageSelected(int position) {
        if (mCurrentIndex == position) {
            return;
        }

        selectPage(position);
        resolveToolbarSubtitle();
        resolvePreparingProgress();
    }

    public void fireAddDeleteButtonClick() {
        Document document = mDocuments.get(mCurrentIndex);
        if (isMy()) {
            delete(document.getId(), document.getOwnerId());
        } else {
            addYourself(document);
        }
    }

    public void fireHolderCreate(int adapterPosition) {
        boolean isProgress = adapterPosition == mCurrentIndex && mGifPlayer.getPlayerStatus() == IGifPlayer.IStatus.PREPARING;

        VideoSize size = mGifPlayer.getVideoSize();
        if (Objects.isNull(size)) {
            size = DEF_SIZE;
        }

        VideoSize finalSize = size;
        callView(v -> v.configHolder(adapterPosition, isProgress, finalSize.getWidth(), finalSize.getWidth()));
    }

    public void fireShareButtonClick() {
        callView(v -> v.shareDocument(getAccountId(), mDocuments.get(mCurrentIndex)));
    }

    public void fireDownloadButtonClick(Context context, View view) {
        if (!AppPerms.hasReadWriteStoragePermission(context)) {
            callView(IBasicDocumentView::requestWriteExternalStoragePermission);
            return;
        }

        downloadImpl(context, view);
    }

    @Override
    public void onWritePermissionResolved(Context context, View view) {
        if (AppPerms.hasReadWriteStoragePermission(context)) {
            downloadImpl(context, view);
        }
    }

    @Override
    public void onGuiPaused() {
        super.onGuiPaused();
        if (Objects.nonNull(mGifPlayer)) {
            mGifPlayer.pause();
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();

        if (Objects.nonNull(mGifPlayer)) {
            try {
                mGifPlayer.play();
            } catch (PlayerPrepareException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyed() {
        if (Objects.nonNull(mGifPlayer)) {
            mGifPlayer.release();
        }
        super.onDestroyed();
    }

    private void downloadImpl(Context context, View view) {
        Document document = mDocuments.get(mCurrentIndex);

        if (DownloadWorkUtils.doDownloadDoc(context, document, false) == 1) {
            Utils.ThemedSnack(view, R.string.audio_force_download, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                    v1 -> DownloadWorkUtils.doDownloadDoc(context, document, true)).show();
        }
    }

    @Override
    public void onPlayerStatusChange(@NonNull IGifPlayer player, int previousStatus, int currentStatus) {
        if (mGifPlayer == player) {
            resolvePreparingProgress();
            resolvePlayerDisplay();
        }
    }

    @Override
    public void onVideoSizeChanged(@NonNull IGifPlayer player, VideoSize size) {
        if (mGifPlayer == player) {
            resolveAspectRatio();
        }
    }
}