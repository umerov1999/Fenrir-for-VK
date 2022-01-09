package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.player.MusicPlaybackController.observeServiceBinding;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso3.Transformation;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.ILocalServerInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.link.VkLinkParser;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.menu.options.AudioLocalServerOption;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class AudioLocalServerRecyclerAdapter extends RecyclerView.Adapter<AudioLocalServerRecyclerAdapter.AudioHolder> {

    private final Context mContext;
    private final ILocalServerInteractor mAudioInteractor;
    private ClickListener mClickListener;
    private Disposable mPlayerDisposable = Disposable.disposed();
    private Disposable audioListDisposable = Disposable.disposed();
    private List<Audio> data;
    private Audio currAudio;

    public AudioLocalServerRecyclerAdapter(Context context, List<Audio> data) {
        this.data = data;
        mContext = context;
        currAudio = MusicPlaybackController.getCurrentAudio();
        mAudioInteractor = InteractorFactory.createLocalServerInteractor();
    }

    public void setItems(List<Audio> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    private Single<Integer> doBitrate(String url) {
        return Single.create(v -> {
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(url, new HashMap<>());
                String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                if (bitrate != null) {
                    v.onSuccess((int) (Long.parseLong(bitrate) / 1000));
                } else {
                    v.onError(new Throwable("Can't receipt bitrate "));
                }
            } catch (RuntimeException e) {
                v.onError(e);
            }
        });
    }

    private void getBitrate(String url, int size) {
        if (Utils.isEmpty(url)) {
            return;
        }
        audioListDisposable = doBitrate(url).compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(r -> CustomToast.CreateCustomToast(mContext).showToast(mContext.getResources().getString(R.string.bitrate, r, Utils.BytesToSize(size))),
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

    private void updateDownloadState(@NonNull AudioHolder holder, @NonNull Audio audio) {
        if (audio.getDownloadIndicator() == 2) {
            holder.saved.setImageResource(R.drawable.remote_cloud);
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorSecondary(mContext));
        } else {
            holder.saved.setImageResource(R.drawable.save);
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(mContext));
        }
        holder.saved.setVisibility(audio.getDownloadIndicator() != 0 ? View.VISIBLE : View.GONE);
    }

    private void doMenu(AudioHolder holder, int position, View view, Audio audio) {
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();

        menus.add(new OptionRequest(AudioLocalServerOption.save_item_audio, mContext.getString(R.string.download), R.drawable.save, true));
        menus.add(new OptionRequest(AudioLocalServerOption.play_item_audio, mContext.getString(R.string.play), R.drawable.play, true));
        if (MusicPlaybackController.canPlayAfterCurrent(audio)) {
            menus.add(new OptionRequest(AudioLocalServerOption.play_item_after_current_audio, mContext.getString(R.string.play_audio_after_current), R.drawable.play_next, false));
        }
        menus.add(new OptionRequest(AudioLocalServerOption.bitrate_item_audio, mContext.getString(R.string.get_bitrate), R.drawable.high_quality, false));
        menus.add(new OptionRequest(AudioLocalServerOption.delete_item_audio, mContext.getString(R.string.delete), R.drawable.ic_outline_delete, true));
        menus.add(new OptionRequest(AudioLocalServerOption.update_time_item_audio, mContext.getString(R.string.update_time), R.drawable.ic_recent, false));
        menus.add(new OptionRequest(AudioLocalServerOption.edit_item_audio, mContext.getString(R.string.edit), R.drawable.about_writed, true));

        menus.header(firstNonEmptyString(audio.getArtist(), " ") + " - " + audio.getTitle(), R.drawable.song, audio.getThumb_image_little());
        menus.columns(2);
        menus.show(((FragmentActivity) mContext).getSupportFragmentManager(), "audio_options", option -> {
            switch (option.getId()) {
                case AudioLocalServerOption.save_item_audio:
                    if (!AppPerms.hasReadWriteStoragePermission(mContext)) {
                        if (mClickListener != null) {
                            mClickListener.onRequestWritePermissions();
                        }
                        break;
                    }
                    audio.setDownloadIndicator(1);
                    updateDownloadState(holder, audio);
                    int ret = DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), false, true);
                    if (ret == 0)
                        CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.saved_audio);
                    else if (ret == 1 || ret == 2) {
                        Utils.ThemedSnack(view, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                                v1 -> DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), true, true)).show();
                    } else {
                        audio.setDownloadIndicator(0);
                        updateDownloadState(holder, audio);
                        CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.error_audio);
                    }
                    break;
                case AudioLocalServerOption.play_item_audio:
                    if (mClickListener != null) {
                        mClickListener.onClick(position, audio);
                        if (Settings.get().other().isShow_mini_player())
                            PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(mContext);
                    }
                    break;
                case AudioLocalServerOption.play_item_after_current_audio:
                    MusicPlaybackController.playAfterCurrent(audio);
                    break;
                case AudioLocalServerOption.bitrate_item_audio:
                    getBitrate(audio.getUrl(), audio.getDuration());
                    break;
                case AudioLocalServerOption.update_time_item_audio:
                    String hash = VkLinkParser.parseLocalServerURL(audio.getUrl());
                    if (Utils.isEmpty(hash)) {
                        break;
                    }
                    audioListDisposable = mAudioInteractor.update_time(hash).compose(RxUtils.applySingleIOToMainSchedulers()).subscribe(t -> CustomToast.CreateCustomToast(mContext).showToast(R.string.success), t -> Utils.showErrorInAdapter((Activity) mContext, t));
                    break;
                case AudioLocalServerOption.edit_item_audio:
                    String hash2 = VkLinkParser.parseLocalServerURL(audio.getUrl());
                    if (Utils.isEmpty(hash2)) {
                        break;
                    }
                    audioListDisposable = mAudioInteractor.get_file_name(hash2).compose(RxUtils.applySingleIOToMainSchedulers()).subscribe(t -> {
                        View root = View.inflate(mContext, R.layout.entry_file_name, null);
                        ((TextInputEditText) root.findViewById(R.id.edit_file_name)).setText(t);
                        new MaterialAlertDialogBuilder(mContext)
                                .setTitle(R.string.change_name)
                                .setCancelable(true)
                                .setView(root)
                                .setPositiveButton(R.string.button_ok, (dialog, which) -> audioListDisposable = mAudioInteractor.update_file_name(hash2, ((TextInputEditText) root.findViewById(R.id.edit_file_name)).getText().toString().trim())
                                        .compose(RxUtils.applySingleIOToMainSchedulers())
                                        .subscribe(t1 -> CustomToast.CreateCustomToast(mContext).showToast(R.string.success), o -> Utils.showErrorInAdapter((Activity) mContext, o)))
                                .setNegativeButton(R.string.button_cancel, null)
                                .show();
                    }, t -> Utils.showErrorInAdapter((Activity) mContext, t));
                    break;
                case AudioLocalServerOption.delete_item_audio:
                    new MaterialAlertDialogBuilder(mContext)
                            .setMessage(R.string.do_delete)
                            .setTitle(R.string.confirmation)
                            .setCancelable(true)
                            .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                                String hash1 = VkLinkParser.parseLocalServerURL(audio.getUrl());
                                if (Utils.isEmpty(hash1)) {
                                    return;
                                }
                                audioListDisposable = mAudioInteractor.delete_media(hash1).compose(RxUtils.applySingleIOToMainSchedulers()).subscribe(t -> CustomToast.CreateCustomToast(mContext).showToast(R.string.success), o -> Utils.showErrorInAdapter((Activity) mContext, o));
                            })
                            .setNegativeButton(R.string.button_cancel, null)
                            .show();
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
        return new AudioHolder(LayoutInflater.from(mContext).inflate(R.layout.item_audio_local_server, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AudioHolder holder, int position) {
        Audio audio = data.get(position);

        holder.cancelSelectionAnimation();
        if (audio.isAnimationNow()) {
            holder.startSelectionAnimation();
            audio.setAnimationNow(false);
        }

        holder.artist.setText(audio.getArtist());
        holder.title.setText(audio.getTitle());

        if (audio.getDuration() <= 0)
            holder.time.setVisibility(View.INVISIBLE);
        else {
            holder.time.setVisibility(View.VISIBLE);
            holder.time.setText(Utils.BytesToSize(audio.getDuration()));
        }

        updateDownloadState(holder, audio);
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

        holder.play.setOnLongClickListener(v -> {
            if ((!Utils.isEmpty(audio.getThumb_image_very_big())
                    || !Utils.isEmpty(audio.getThumb_image_big()) || !Utils.isEmpty(audio.getThumb_image_little())) && !Utils.isEmpty(audio.getArtist()) && !Utils.isEmpty(audio.getTitle())) {
                mClickListener.onUrlPhotoOpen(firstNonEmptyString(audio.getThumb_image_very_big(),
                        audio.getThumb_image_big(), audio.getThumb_image_little()), audio.getArtist(), audio.getTitle());
            }
            return true;
        });

        holder.play.setOnClickListener(v -> {
            if (Settings.get().main().isRevert_play_audio()) {
                doMenu(holder, position, v, audio);
            } else {
                doPlay(position, audio);
            }
        });

        holder.Track.setOnLongClickListener(v -> {
            if (!AppPerms.hasReadWriteStoragePermission(mContext)) {
                if (mClickListener != null) {
                    mClickListener.onRequestWritePermissions();
                }
                return false;
            }
            audio.setDownloadIndicator(1);
            updateDownloadState(holder, audio);
            int ret = DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), false, true);
            if (ret == 0)
                CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.saved_audio);
            else if (ret == 1 || ret == 2) {
                Utils.ThemedSnack(v, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                        v1 -> DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), true, true)).show();
            } else {
                audio.setDownloadIndicator(0);
                updateDownloadState(holder, audio);
                CustomToast.CreateCustomToast(mContext).showToastBottom(R.string.error_audio);
            }
            return true;
        });

        holder.Track.setOnClickListener(view -> {
            holder.cancelSelectionAnimation();
            holder.startSomeAnimation();
            if (Settings.get().main().isRevert_play_audio()) {
                doPlay(position, audio);
            } else {
                doMenu(holder, position, view, audio);
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

        void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix);

        void onRequestWritePermissions();
    }

    class AudioHolder extends RecyclerView.ViewHolder {

        final TextView artist;
        final TextView title;
        final View play;
        final ImageView play_cover;
        final View Track;
        final ImageView saved;
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
            saved = itemView.findViewById(R.id.saved);
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
