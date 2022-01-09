package dev.ragnarok.fenrir.mvp.view.search;

import dev.ragnarok.fenrir.model.Audio;

public interface IAudioSearchView extends IBaseSearchView<Audio> {
    void notifyAudioChanged(int index);
}
