package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IAudioDuplicateView extends IMvpView, IErrorView {
    void displayData(Audio new_audio, Audio old_audio);

    void setOldBitrate(@Nullable Integer bitrate);

    void setNewBitrate(@Nullable Integer bitrate);

    void updateShowBitrate(boolean needShow);
}
