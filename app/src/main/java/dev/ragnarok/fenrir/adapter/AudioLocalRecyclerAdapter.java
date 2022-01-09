package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.player.MusicPlaybackController.observeServiceBinding;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso3.Transformation;

import java.io.File;
import java.util.List;
import java.util.Objects;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.menu.options.AudioLocalOption;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class AudioLocalRecyclerAdapter extends RecyclerView.Adapter<AudioLocalRecyclerAdapter.AudioHolder> {

    private final Context mContext;
    private ClickListener mClickListener;
    private Disposable mPlayerDisposable = Disposable.disposed();
    private Disposable audioListDisposable = Disposable.disposed();
    private List<Audio> data;
    private Audio currAudio;

    public AudioLocalRecyclerAdapter(Context context, List<Audio> data) {
        this.data = data;
        mContext = context;
        currAudio = MusicPlaybackController.getCurrentAudio();
    }

    public void setItems(List<Audio> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    private Single<Pair<Integer, Long>> doLocalBitrate(String url) {
        return Single.create(v -> {
            try {
                Cursor cursor = mContext.getContentResolver().query(
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
                        v.onSuccess(new Pair<>((int) (Long.parseLong(bitrate) / 1000), new File(fl).length()));
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

    private void getLocalBitrate(String url) {
        if (Utils.isEmpty(url)) {
            return;
        }
        audioListDisposable = doLocalBitrate(url).compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(r -> CustomToast.CreateCustomToast(mContext).showToast(mContext.getResources().getString(R.string.bitrate, r.getFirst(), Utils.BytesToSize(r.getSecond()))),
                        e -> Utils.showErrorInAdapter((Activity) mContext, e));
    }

    @DrawableRes
    private int getAudioCoverSimple() {
        return Settings.get().main().isAudio_round_icon() ? R.drawable.audio_button : R.drawable.audio_button_material;
    }

    private Transformation TransformCover() {
        return Settings.get().main().isAudio_round_icon() ? new RoundTransformation() : new PolyTransformation();
    }

    private void updateAudioStatus(AudioHolder holder, Audio audio) {
        if (!audio.equals(currAudio)) {
            holder.visual.setImageResource(Utils.isEmpty(audio.getUrl()) ? R.drawable.audio_died : R.drawable.song);
            holder.play_cover.clearColorFilter();
            return;
        }
        switch (MusicPlaybackController.PlayerStatus()) {
            case 1:
                Utils.doWavesLottie(holder.visual, true);
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"));
                break;
            case 2:
                Utils.doWavesLottie(holder.visual, false);
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"));
                break;

        }
    }

    private void doMenu(int position, View view, Audio audio) {
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();

        menus.add(new OptionRequest(AudioLocalOption.upload_item_audio, mContext.getString(R.string.upload), R.drawable.web, true));
        menus.add(new OptionRequest(AudioLocalOption.play_item_audio, mContext.getString(R.string.play), R.drawable.play, true));
        if (Settings.get().other().getLocalServer().enabled) {
            menus.add(new OptionRequest(AudioLocalOption.play_via_local_server, mContext.getString(R.string.play_remote), R.drawable.remote_cloud, false));
        }
        if (MusicPlaybackController.canPlayAfterCurrent(audio)) {
            menus.add(new OptionRequest(AudioLocalOption.play_item_after_current_audio, mContext.getString(R.string.play_audio_after_current), R.drawable.play_next, false));
        }
        menus.add(new OptionRequest(AudioLocalOption.bitrate_item_audio, mContext.getString(R.string.get_bitrate), R.drawable.high_quality, false));
        menus.add(new OptionRequest(AudioLocalOption.delete_item_audio, mContext.getString(R.string.delete), R.drawable.ic_outline_delete, true));


        menus.header(firstNonEmptyString(audio.getArtist(), " ") + " - " + audio.getTitle(), R.drawable.song, audio.getThumb_image_little());
        menus.columns(2);
        menus.show(((FragmentActivity) mContext).getSupportFragmentManager(), "audio_options", option -> {
            switch (option.getId()) {
                case AudioLocalOption.upload_item_audio:
                    if (mClickListener != null) {
                        mClickListener.onUpload(position, audio);
                    }
                    break;
                case AudioLocalOption.play_via_local_server:
                    if (mClickListener != null) {
                        mClickListener.onRemotePlay(position, audio);
                    }
                    break;
                case AudioLocalOption.play_item_audio:
                    if (mClickListener != null) {
                        mClickListener.onClick(position, audio);
                        if (Settings.get().other().isShow_mini_player())
                            PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(mContext);
                    }
                    break;
                case AudioLocalOption.play_item_after_current_audio:
                    MusicPlaybackController.playAfterCurrent(audio);
                    break;
                case AudioLocalOption.bitrate_item_audio:
                    getLocalBitrate(audio.getUrl());
                    break;
                case AudioLocalOption.delete_item_audio:
                    try {
                        if (mContext.getContentResolver().delete(Uri.parse(audio.getUrl()), null, null) == 1) {
                            Snackbar.make(view, R.string.success, BaseTransientBottomBar.LENGTH_LONG).show();
                            if (mClickListener != null) {
                                mClickListener.onDelete(position);
                            }
                        }
                    } catch (Exception e) {
                        CustomToast.CreateCustomToast(mContext).showToastError(e.getLocalizedMessage());
                    }
                    break;
                default:
                    break;
            }
        });
    }

    private void doPlay(int position, Audio audio) {
        if (MusicPlaybackController.isNowPlayingOrPreparingOrPaused(audio)) {
            if (!Settings.get().other().isUse_stop_audio()) {
                MusicPlaybackController.playOrPause();
            } else {
                MusicPlaybackController.stop();
            }
        } else {
            if (mClickListener != null) {
                mClickListener.onClick(position, audio);
            }
        }
    }

    @NonNull
    @Override
    public AudioHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AudioHolder(LayoutInflater.from(mContext).inflate(R.layout.item_local_audio, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int position) {
        Audio audio = data.get(position);

        holder.cancelSelectionAnimation();
        if (audio.isAnimationNow()) {
            holder.startSelectionAnimation();
            audio.setAnimationNow(false);
        }

        if (!Utils.isEmpty(audio.getArtist())) {
            holder.artist.setText(audio.getArtist());
        } else {
            holder.artist.setText(mContext.getString(R.string.not_set));
        }
        holder.title.setText(audio.getTitle());

        if (audio.getDuration() <= 0)
            holder.time.setVisibility(View.INVISIBLE);
        else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(AppTextUtils.getDurationString(audio.getDuration()));
        }

        updateAudioStatus(holder, audio);

        if (!Utils.isEmpty(audio.getThumb_image_little())) {
            PicassoInstance.with()
                    .load(audio.getThumb_image_little())
                    .placeholder(Objects.requireNonNull(ResourcesCompat.getDrawable(mContext.getResources(), getAudioCoverSimple(), mContext.getTheme())))
                    .transform(TransformCover())
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.play_cover);
        } else {
            PicassoInstance.with().cancelRequest(holder.play_cover);
            holder.play_cover.setImageResource(getAudioCoverSimple());
        }

        holder.play.setOnClickListener(v -> {
            if (Settings.get().main().isRevert_play_audio()) {
                doMenu(position, v, audio);
            } else {
                doPlay(position, audio);
            }
        });
        holder.Track.setOnClickListener(view -> {
            holder.cancelSelectionAnimation();
            holder.startSomeAnimation();
            if (Settings.get().main().isRevert_play_audio()) {
                doPlay(position, audio);
            } else {
                doMenu(position, view, audio);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mPlayerDisposable = observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(this::onServiceBindEvent);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mPlayerDisposable.dispose();
        audioListDisposable.dispose();
    }

    private void onServiceBindEvent(@MusicPlaybackController.PlayerStatus int status) {
        switch (status) {
            case MusicPlaybackController.PlayerStatus.UPDATE_TRACK_INFO:
            case MusicPlaybackController.PlayerStatus.SERVICE_KILLED:
            case MusicPlaybackController.PlayerStatus.UPDATE_PLAY_PAUSE:
                updateAudio(currAudio);
                currAudio = MusicPlaybackController.getCurrentAudio();
                updateAudio(currAudio);
                break;
            case MusicPlaybackController.PlayerStatus.REPEATMODE_CHANGED:
            case MusicPlaybackController.PlayerStatus.SHUFFLEMODE_CHANGED:
            case MusicPlaybackController.PlayerStatus.UPDATE_PLAY_LIST:
                break;
        }
    }

    private void updateAudio(Audio audio) {
        int pos = data.indexOf(audio);
        if (pos != -1) {
            notifyItemChanged(pos);
        }
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(int position, Audio audio);

        void onDelete(int position);

        void onUpload(int position, Audio audio);

        void onRemotePlay(int position, Audio audio);
    }

    class AudioHolder extends RecyclerView.ViewHolder {

        final TextView artist;
        final TextView title;
        final View play;
        final ImageView play_cover;
        final View Track;
        final MaterialCardView selectionView;
        final Animator.AnimatorListener animationAdapter;
        final RLottieImageView visual;
        final TextView time;
        ObjectAnimator animator;

        AudioHolder(View itemView) {
            super(itemView);
            artist = itemView.findViewById(R.id.dialog_title);
            title = itemView.findViewById(R.id.dialog_message);
            time = itemView.findViewById(R.id.item_audio_time);
            play = itemView.findViewById(R.id.item_audio_play);
            play_cover = itemView.findViewById(R.id.item_audio_play_cover);
            Track = itemView.findViewById(R.id.track_option);
            selectionView = itemView.findViewById(R.id.item_audio_selection);
            visual = itemView.findViewById(R.id.item_audio_visual);
            animationAdapter = new WeakViewAnimatorAdapter<View>(selectionView) {
                @Override
                public void onAnimationEnd(View view) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(View view) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationCancel(View view) {
                    view.setVisibility(View.GONE);
                }
            };
        }

        void startSelectionAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorPrimary(mContext));
            selectionView.setAlpha(0.5f);

            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f);
            animator.setDuration(1500);
            animator.addListener(animationAdapter);
            animator.start();
        }

        void startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(mContext));
            selectionView.setAlpha(0.5f);

            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f);
            animator.setDuration(500);
            animator.addListener(animationAdapter);
            animator.start();
        }

        void cancelSelectionAnimation() {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }

            selectionView.setVisibility(View.INVISIBLE);
        }
    }
}
