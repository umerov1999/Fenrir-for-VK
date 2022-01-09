package dev.ragnarok.fenrir.player;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Objects;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;


public final class MusicPlaybackController {

    public static final Map<Integer, ArrayList<Audio>> Audios = new LinkedHashMap<>();
    public static final List<String> CachedAudios = new LinkedList<>();
    public static final List<String> RemoteAudios = new LinkedList<>();
    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;
    private static final PublishSubject<Integer> SERVICE_BIND_PUBLISHER = PublishSubject.create();
    private static final String TAG = MusicPlaybackController.class.getSimpleName();
    public static IAudioPlayerService mService;
    private static int sForegroundActivities;

    static {
        mConnectionMap = new WeakHashMap<>();
    }

    /* This class is never initiated */
    private MusicPlaybackController() {
    }

    public static void registerBroadcast(@NonNull Context appContext) {
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Objects.isNull(intent) || Objects.isNull(intent.getAction())) return;
                int result = PlayerStatus.SERVICE_KILLED;
                switch (intent.getAction()) {
                    case MusicPlaybackService.PREPARED:
                    case MusicPlaybackService.PLAYSTATE_CHANGED:
                        result = PlayerStatus.UPDATE_PLAY_PAUSE;
                        break;
                    case MusicPlaybackService.SHUFFLEMODE_CHANGED:
                        result = PlayerStatus.SHUFFLEMODE_CHANGED;
                        break;
                    case MusicPlaybackService.REPEATMODE_CHANGED:
                        result = PlayerStatus.REPEATMODE_CHANGED;
                        break;
                    case MusicPlaybackService.META_CHANGED:
                        result = PlayerStatus.UPDATE_TRACK_INFO;
                        break;
                    case MusicPlaybackService.QUEUE_CHANGED:
                        result = PlayerStatus.UPDATE_PLAY_LIST;
                        break;
                }
                SERVICE_BIND_PUBLISHER.onNext(result);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
        filter.addAction(MusicPlaybackService.SHUFFLEMODE_CHANGED);
        filter.addAction(MusicPlaybackService.REPEATMODE_CHANGED);
        filter.addAction(MusicPlaybackService.META_CHANGED);
        filter.addAction(MusicPlaybackService.PREPARED);
        filter.addAction(MusicPlaybackService.QUEUE_CHANGED);

        appContext.registerReceiver(receiver, filter);
    }

    public static ServiceToken bindToServiceWithoutStart(Activity realActivity, ServiceConnection callback) {
        ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        ServiceBinder binder = new ServiceBinder(callback);

        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MusicPlaybackService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }

        return null;
    }

    /**
     * @param token The {@link ServiceToken} to unbind from
     */
    public static void unbindFromService(ServiceToken token) {
        if (token == null) {
            return;
        }

        ContextWrapper mContextWrapper = token.mWrappedContext;
        ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }

        mContextWrapper.unbindService(mBinder);

        if (mConnectionMap.isEmpty()) {
            mService = null;
        }
    }

    public static Observable<Integer> observeServiceBinding() {
        return SERVICE_BIND_PUBLISHER;
    }

    public static String makeTimeString(Context context, long secs) {
        long hours, mins;

        hours = secs / 3600;
        secs -= hours * 3600;
        mins = secs / 60;
        secs -= mins * 60;

        String durationFormat = context.getResources().getString(
                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
        return String.format(durationFormat, hours, mins, secs);
    }

    /**
     * Changes to the next track
     */
    public static void next() {
        try {
            if (mService != null) {
                mService.next();
            }
        } catch (RemoteException ignored) {
        }
    }

    public static boolean isInitialized() {
        if (mService != null) {
            try {
                return mService.isInitialized();
            } catch (RemoteException ignored) {
            }
        }

        return false;
    }

    public static boolean isPreparing() {
        if (mService != null) {
            try {
                return mService.isPreparing();
            } catch (RemoteException ignored) {
            }
        }

        return false;
    }

    /**
     * Changes to the previous track.
     */
    public static void previous(Context context) {
        Intent previous = new Intent(context, MusicPlaybackService.class);
        previous.setAction(MusicPlaybackService.PREVIOUS_ACTION);
        context.startService(previous);
    }

    /**
     * Plays or pauses the music.
     */
    public static void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void stop() {
        try {
            if (mService != null) {
                mService.stop();
            }
        } catch (Exception ignored) {
        }
    }

    public static void closeMiniPlayer() {
        try {
            if (mService != null) {
                mService.closeMiniPlayer();
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean getMiniPlayerVisibility() {
        if (!Settings.get().other().isShow_mini_player())
            return false;
        try {
            if (mService != null) {
                return mService.getMiniplayerVisibility();
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Cycles through the repeat options.
     */
    public static void cycleRepeat() {
        try {
            if (mService != null) {
                switch (mService.getRepeatMode()) {
                    case MusicPlaybackService.REPEAT_NONE:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_ALL);
                        break;
                    case MusicPlaybackService.REPEAT_ALL:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_CURRENT);
                        if (mService.getShuffleMode() != MusicPlaybackService.SHUFFLE_NONE) {
                            mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
                        }
                        break;
                    default:
                        mService.setRepeatMode(MusicPlaybackService.REPEAT_NONE);
                        break;
                }
            }
        } catch (RemoteException ignored) {
        }
    }

    /**
     * Cycles through the shuffle options.
     */
    public static void cycleShuffle() {
        try {
            if (mService != null) {
                switch (mService.getShuffleMode()) {
                    case MusicPlaybackService.SHUFFLE_NONE:
                        mService.setShuffleMode(MusicPlaybackService.SHUFFLE);
                        if (mService.getRepeatMode() == MusicPlaybackService.REPEAT_CURRENT) {
                            mService.setRepeatMode(MusicPlaybackService.REPEAT_ALL);
                        }
                        break;
                    case MusicPlaybackService.SHUFFLE:
                        mService.setShuffleMode(MusicPlaybackService.SHUFFLE_NONE);
                        break;
                    default:
                        break;
                }
            }
        } catch (RemoteException ignored) {
        }
    }

    public static boolean canPlayAfterCurrent(@NonNull Audio audio) {
        if (mService != null) {
            try {
                return mService.canPlayAfterCurrent(audio);
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    public static void playAfterCurrent(@NonNull Audio audio) {
        if (mService != null) {
            try {
                mService.playAfterCurrent(audio);
            } catch (RemoteException ignored) {
            }
        }
    }

    /**
     * @return True if we're playing music, false otherwise.
     */
    public static boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (RemoteException ignored) {
            }
        }
        return false;
    }

    /**
     * @return The current shuffle mode.
     */
    public static int getShuffleMode() {
        if (mService != null) {
            try {
                return mService.getShuffleMode();
            } catch (RemoteException ignored) {
            }
        }

        return 0;
    }

    /**
     * @return The current repeat mode.
     */
    public static int getRepeatMode() {
        if (mService != null) {
            try {
                return mService.getRepeatMode();
            } catch (RemoteException ignored) {
            }
        }

        return 0;
    }

    @Nullable
    public static Audio getCurrentAudio() {
        if (mService != null) {
            try {
                return mService.getCurrentAudio();
            } catch (RemoteException ignored) {
            }
        }

        return null;
    }

    @Nullable
    public static Integer getCurrentAudioPos() {
        if (mService != null) {
            try {
                int ret = mService.getCurrentAudioPos();
                if (ret < 0) {
                    return null;
                } else {
                    return ret;
                }
            } catch (RemoteException ignored) {
            }
        }

        return null;
    }

    /**
     * @return The current track name.
     */
    @Nullable
    public static String getTrackName() {
        if (mService != null) {
            try {
                return mService.getTrackName();
            } catch (RemoteException ignored) {
            }
        }

        return null;
    }

    /**
     * @return The current album name.
     */
    @Nullable
    public static String getAlbumName() {
        if (mService != null) {
            try {
                return mService.getAlbumName();
            } catch (RemoteException ignored) {
            }
        }

        return null;
    }

    /**
     * @return The current artist name.
     */
    @Nullable
    public static String getArtistName() {
        if (mService != null) {
            try {
                return mService.getArtistName();
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    @Nullable
    public static String getAlbumCoverBig() {
        if (mService != null) {
            try {
                return mService.getAlbumCover();
            } catch (RemoteException ignored) {
            }
        }
        return null;
    }

    /**
     * @return The current song Id.
     */
    public static int getAudioSessionId() {
        if (mService != null) {
            try {
                return mService.getAudioSessionId();
            } catch (RemoteException ignored) {
            }
        }
        return 0;
    }

    /**
     * @return The queue.
     */
    public static List<Audio> getQueue() {
        try {
            if (mService != null) {
                return mService.getQueue();
            }
        } catch (RemoteException ignored) {
        }

        return Collections.emptyList();
    }

    /**
     * Called when one of the lists should refresh or requery.
     */
    public static void refresh() {
        try {
            if (mService != null) {
                mService.refresh();
            }
        } catch (RemoteException ignored) {
        }
    }

    /**
     * Seeks the current track to a desired position
     *
     * @param position The position to seek to
     */
    public static void seek(long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (RemoteException ignored) {
            }
        }
    }

    public static void skip(int position) {
        if (mService != null) {
            try {
                mService.skip(position);
            } catch (RemoteException ignored) {
            }
        }
    }

    /**
     * @return The current position time of the track
     */
    public static long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (RemoteException ignored) {
            }
        }

        return 0;
    }

    /**
     * @return The total length of the current track
     */
    public static long duration() {
        if (mService != null) {
            try {
                return mService.duration();
            } catch (RemoteException ignored) {
            }
        }
        return 0;
    }

    public static int bufferPercent() {
        if (mService != null) {
            try {
                return mService.getBufferPercent();
            } catch (RemoteException ignored) {
            }
        }
        return 0;
    }

    public static long bufferPosition() {
        if (mService != null) {
            try {
                return mService.getBufferPosition();
            } catch (RemoteException ignored) {
            }
        }
        return 0;
    }

    public static boolean isNowPlayingOrPreparingOrPaused(Audio audio) {
        return audio.equals(getCurrentAudio());
    }

    public static Integer PlayerStatus() {
        if (isPreparing() || isPlaying())
            return 1;
        if (getCurrentAudio() != null)
            return 2;
        return 0;
    }

    /**
     * Used to build and show a notification when player is sent into the
     * background
     *
     * @param context The {@link Context} to use.
     */
    public static void notifyForegroundStateChanged(Context context, boolean inForeground) {
        int old = sForegroundActivities;
        if (inForeground) {
            sForegroundActivities++;
        } else {
            sForegroundActivities--;
            if (sForegroundActivities < 0)
                sForegroundActivities = 0;
        }

        if (old == 0 || sForegroundActivities == 0) {
            try {
                boolean nowInForeground = sForegroundActivities != 0;
                Logger.d(TAG, "notifyForegroundStateChanged, nowInForeground: " + nowInForeground);

                Intent intent = new Intent(context, MusicPlaybackService.class);
                intent.setAction(MusicPlaybackService.FOREGROUND_STATE_CHANGED);
                intent.putExtra(MusicPlaybackService.NOW_IN_FOREGROUND, nowInForeground);
                context.startService(intent);
            } catch (IllegalStateException | SecurityException e) {
                e.printStackTrace();
            }
        }
    }

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

    public static final class ServiceBinder implements ServiceConnection {

        private final ServiceConnection mCallback;


        public ServiceBinder(ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = IAudioPlayerService.Stub.asInterface(service);
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
            SERVICE_BIND_PUBLISHER.onNext(PlayerStatus.UPDATE_PLAY_LIST);
            SERVICE_BIND_PUBLISHER.onNext(PlayerStatus.UPDATE_TRACK_INFO);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
            SERVICE_BIND_PUBLISHER.onNext(PlayerStatus.SERVICE_KILLED);
        }
    }

    public static final class ServiceToken {

        public final ContextWrapper mWrappedContext;

        public ServiceToken(ContextWrapper context) {
            mWrappedContext = context;
        }
    }
}
