package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.player.MusicPlaybackController.observeServiceBinding;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;

import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioDuplicateView;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.util.Mp3InfoHelper;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.hls.M3U8;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;


public class AudioDuplicatePresenter extends RxSupportPresenter<IAudioDuplicateView> {

    private final int accountId;

    private final Audio new_audio;
    private final Audio old_audio;
    private final IAudioInteractor mAudioInteractor = InteractorFactory.createAudioInteractor();
    private final Disposable mPlayerDisposable;
    private Integer oldBitrate;
    private Integer newBitrate;
    private boolean needShowBitrateButton = true;
    private Disposable audioListDisposable = Disposable.disposed();

    public AudioDuplicatePresenter(int accountId, Audio new_audio, Audio old_audio, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.accountId = accountId;
        this.new_audio = new_audio;
        this.old_audio = old_audio;
        mPlayerDisposable = observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(this::onServiceBindEvent);
    }

    private void getMp3AndBitrate() {
        Pair<Boolean, Boolean> mode = new_audio.needRefresh();
        if (mode.getFirst()) {
            audioListDisposable = mAudioInteractor.getByIdOld(accountId, Collections.singletonList(new_audio), mode.getSecond()).compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> getBitrate(t.get(0)), e -> getBitrate(new_audio));
        } else {
            getBitrate(new_audio);
        }
    }

    private void getBitrate(@NonNull Audio audio) {
        if (Utils.isEmpty(audio.getUrl())) {
            return;
        }
        if (audio.isHLS()) {
            audioListDisposable = new M3U8(audio.getUrl()).getLength().compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(r -> {
                        newBitrate = Mp3InfoHelper.getBitrate(audio.getDuration(), r);
                        callView(o -> o.setNewBitrate(newBitrate));
                    }, this::onDataGetError);
        } else {
            audioListDisposable = Mp3InfoHelper.getLength(audio.getUrl()).compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(r -> {
                        newBitrate = Mp3InfoHelper.getBitrate(audio.getDuration(), r);
                        callView(o -> o.setNewBitrate(newBitrate));
                    }, this::onDataGetError);
        }
    }

    private Single<Integer> doLocalBitrate(Context context, String url) {
        return Single.create(v -> {
            try {
                Cursor cursor = context.getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[]{MediaStore.MediaColumns.DATA},
                        BaseColumns._ID + "=? ",
                        new String[]{Uri.parse(url).getLastPathSegment()}, null);
                if (cursor != null && cursor.moveToFirst()) {
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    String fl = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                    retriever.setDataSource(fl);
                    cursor.close();
                    String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                    if (bitrate != null) {
                        v.onSuccess((int) (Long.parseLong(bitrate) / 1000));
                    } else {
                        v.onError(new Throwable("Can't receipt bitrate "));
                    }
                } else {
                    v.onError(new Throwable("Can't receipt bitrate "));
                }
            } catch (RuntimeException e) {
                v.onError(e);
            }
        });
    }

    public void getBitrateAll(@NonNull Context context) {
        if (Utils.isEmpty(old_audio.getUrl())) {
            return;
        }
        needShowBitrateButton = false;
        callView(v -> v.updateShowBitrate(needShowBitrateButton));
        audioListDisposable = doLocalBitrate(context, old_audio.getUrl()).compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(r -> {
                            oldBitrate = r;
                            callView(o -> o.setOldBitrate(oldBitrate));
                            getMp3AndBitrate();
                        },
                        this::onDataGetError);
    }

    private void onServiceBindEvent(@MusicPlaybackController.PlayerStatus int status) {
        switch (status) {
            case MusicPlaybackController.PlayerStatus.UPDATE_TRACK_INFO:
            case MusicPlaybackController.PlayerStatus.SERVICE_KILLED:
            case MusicPlaybackController.PlayerStatus.UPDATE_PLAY_PAUSE:
                callView(v -> v.displayData(new_audio, old_audio));
                break;
            case MusicPlaybackController.PlayerStatus.REPEATMODE_CHANGED:
            case MusicPlaybackController.PlayerStatus.SHUFFLEMODE_CHANGED:
            case MusicPlaybackController.PlayerStatus.UPDATE_PLAY_LIST:
                break;
        }
    }

    @Override
    public void onDestroyed() {
        mPlayerDisposable.dispose();
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    public void onGuiCreated(@NonNull IAudioDuplicateView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(new_audio, old_audio);
        viewHost.setNewBitrate(newBitrate);
        viewHost.setOldBitrate(oldBitrate);
        viewHost.updateShowBitrate(needShowBitrateButton);
    }

    private void onDataGetError(Throwable t) {
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }
}
