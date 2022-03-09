package dev.ragnarok.fenrir.media.music;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({PlayerStatus.SERVICE_KILLED,
        PlayerStatus.SHUFFLEMODE_CHANGED,
        PlayerStatus.REPEATMODE_CHANGED,
        PlayerStatus.UPDATE_TRACK_INFO,
        PlayerStatus.UPDATE_PLAY_PAUSE,
        PlayerStatus.UPDATE_PLAY_LIST})
@Retention(RetentionPolicy.SOURCE)
public @interface PlayerStatus {
    int SERVICE_KILLED = 0;
    int SHUFFLEMODE_CHANGED = 1;
    int REPEATMODE_CHANGED = 2;
    int UPDATE_TRACK_INFO = 3;
    int UPDATE_PLAY_PAUSE = 4;
    int UPDATE_PLAY_LIST = 5;
}
