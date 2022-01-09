package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudiosRecommendationView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class AudiosRecommendationPresenter extends AccountDependencyPresenter<IAudiosRecommendationView> {
    private static final int REC_COUNT = 1000;
    private final IAudioInteractor audioInteractor;
    private final ArrayList<Audio> audios;
    private final int ownerId;
    private final int option_menu_id;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private final boolean top;
    private boolean loadingNow;
    private boolean doAudioLoadTabs;

    public AudiosRecommendationPresenter(int accountId, int ownerId, boolean top, int option_menu_id, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        audioInteractor = InteractorFactory.createAudioInteractor();
        audios = new ArrayList<>();
        this.ownerId = ownerId;
        this.option_menu_id = option_menu_id;
        this.top = top;
    }

    public void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();

        if (doAudioLoadTabs) {
            return;
        } else {
            doAudioLoadTabs = true;
        }
        fireRefresh();
    }

    private void resolveRefreshingView() {
        callResumedView(v -> v.displayRefreshing(loadingNow));
    }

    private void onEndlessListReceived(List<Audio> data) {
        audios.clear();
        audios.addAll(data);
        setLoadingNow(false);
        callView(IAudiosRecommendationView::notifyListChanged);
    }

    public void playAudio(Context context, int position) {
        MusicPlaybackService.startForPlayList(context, audios, position, false);
        if (!Settings.get().other().isShow_mini_player())
            PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(context);
    }

    public void fireDelete(int position) {
        audios.remove(position);
        callView(v -> v.notifyItemRemoved(position));
    }

    public void getListByGenre(boolean foreign, int genre) {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.getPopular(getAccountId(), foreign ? 1 : 0, genre, REC_COUNT)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onEndlessListReceived, this::onListGetError));
    }

    public void getRecommendations() {
        setLoadingNow(true);
        if (option_menu_id != 0) {
            audioListDisposable.add(audioInteractor.getRecommendationsByAudio(getAccountId(), ownerId + "_" + option_menu_id, REC_COUNT)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onEndlessListReceived));
        } else {
            audioListDisposable.add(audioInteractor.getRecommendations(getAccountId(), ownerId, REC_COUNT)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onEndlessListReceived, this::onListGetError));
        }
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);

        callResumedView(v -> showError(v, getCauseIfRuntime(t)));
    }

    public void fireSelectAll() {
        for (Audio i : audios) {
            i.setIsSelected(true);
        }
        callView(IAudiosRecommendationView::notifyListChanged);
    }

    public ArrayList<Audio> getSelected(boolean noDownloaded) {
        ArrayList<Audio> ret = new ArrayList<>();
        for (Audio i : audios) {
            if (i.isSelected()) {
                if (noDownloaded) {
                    if (DownloadWorkUtils.TrackIsDownloaded(i) == 0 && !Utils.isEmpty(i.getUrl()) && !i.getUrl().contains("file://") && !i.getUrl().contains("content://")) {
                        ret.add(i);
                    }
                } else {
                    ret.add(i);
                }
            }
        }
        return ret;
    }

    public int getAudioPos(Audio audio) {
        if (!Utils.isEmpty(audios) && audio != null) {
            int pos = 0;
            for (Audio i : audios) {
                if (i.getId() == audio.getId() && i.getOwnerId() == audio.getOwnerId()) {
                    i.setAnimationNow(true);
                    int finalPos = pos;
                    callView(v -> v.notifyItemChanged(finalPos));
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    public void fireUpdateSelectMode() {
        for (Audio i : audios) {
            if (i.isSelected()) {
                i.setIsSelected(false);
            }
        }
        callView(IAudiosRecommendationView::notifyListChanged);
    }

    public void fireRefresh() {
        audioListDisposable.clear();
        if (top) {
            getListByGenre(false, option_menu_id);
        } else {
            getRecommendations();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IAudiosRecommendationView view) {
        super.onGuiCreated(view);
        view.displayList(audios);
    }
}
