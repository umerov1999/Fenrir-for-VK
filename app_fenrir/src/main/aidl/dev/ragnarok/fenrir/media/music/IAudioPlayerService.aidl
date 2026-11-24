package dev.ragnarok.fenrir.media.music;

import dev.ragnarok.fenrir.model.Audio;
import java.util.List;

interface IAudioPlayerService {
    void openFile(in Audio audio);
    void open(in List<Audio> list, int position);
    void playAfterCurrent(in Audio audio);
    boolean canPlayAfterCurrent();
    void skip(int position);
    void stop();
    void pause();
    void play();
    void prev();
    void next();
    void closeMiniPlayer();
    boolean getMiniplayerVisibility();
    void setShuffleMode(int shuffleMode);
    void setRepeatMode(int repeatMode);
    void refresh();
    boolean isPlaying();
    boolean isPreparing();
    boolean isInitialized();
    List<Audio> getQueue();
    int getQueueCount();
    long duration();
    long position();
    long seek(long pos);
    Audio getCurrentAudio();
    int getCurrentAudioPos();
    String getArtistName();
    String getTrackName();
    String getAlbumName();
    String getPath();
    String getAlbumCover();
    int getShuffleMode();
    int getRepeatMode();
    int getAudioSessionId();
    int getBufferPercent();
    long getBufferPosition();
    void doNotDestroyWhenActivityRecreated();
}