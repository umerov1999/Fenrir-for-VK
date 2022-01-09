package dev.ragnarok.fenrir.media.exo;

import com.google.android.exoplayer2.ExoPlayer;

public class ExoUtil {
    public static void pausePlayer(ExoPlayer player) {
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    public static void startPlayer(ExoPlayer player) {
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }
}