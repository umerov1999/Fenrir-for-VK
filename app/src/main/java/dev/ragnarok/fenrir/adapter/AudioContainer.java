package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.player.MusicPlaybackController.observeServiceBinding;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.domain.IAudioInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.SearchContentType;
import dev.ragnarok.fenrir.fragment.search.criteria.AudioSearchCriteria;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.menu.options.AudioOption;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Mp3InfoHelper;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.hls.M3U8;
import dev.ragnarok.fenrir.view.WeakViewAnimatorAdapter;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import io.reactivex.rxjava3.disposables.Disposable;

public class AudioContainer extends LinearLayout {
    private final IAudioInteractor mAudioInteractor = InteractorFactory.createAudioInteractor();
    private Disposable mPlayerDisposable = Disposable.disposed();
    private Disposable audioListDisposable = Disposable.disposed();
    private List<Audio> audios = Collections.emptyList();
    private Audio currAudio = MusicPlaybackController.getCurrentAudio();

    public AudioContainer(Context context) {
        super(context);
    }

    public AudioContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AudioContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
            holder.visual.setImageResource(audio.getSongIcon());
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

    private void deleteTrack(int accountId, Audio audio) {
        audioListDisposable = mAudioInteractor.delete(accountId, audio.getId(), audio.getOwnerId()).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> CustomToast.CreateCustomToast(getContext()).showToast(R.string.deleted), t -> Utils.showErrorInAdapter((Activity) getContext(), t));
    }

    private void addTrack(int accountId, Audio audio) {
        audioListDisposable = mAudioInteractor.add(accountId, audio, null).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() ->
                CustomToast.CreateCustomToast(getContext()).showToast(R.string.added), t -> Utils.showErrorInAdapter((Activity) getContext(), t));
    }

    private void getMp3AndBitrate(int accountId, Audio audio) {
        Pair<Boolean, Boolean> mode = audio.needRefresh();
        if (mode.getFirst()) {
            audioListDisposable = mAudioInteractor.getByIdOld(accountId, Collections.singletonList(audio), mode.getSecond()).compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(t -> getBitrate(t.get(0)), e -> getBitrate(audio));
        } else {
            getBitrate(audio);
        }
    }

    private void getBitrate(@NonNull Audio audio) {
        if (isEmpty(audio.getUrl())) {
            return;
        }
        if (audio.isHLS()) {
            audioListDisposable = new M3U8(audio.getUrl()).getLength().compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(r -> CustomToast.CreateCustomToast(getContext()).showToast(Mp3InfoHelper.getBitrate(getContext(), audio.getDuration(), r)),
                            e -> Utils.showErrorInAdapter((Activity) getContext(), e));
        } else {
            audioListDisposable = Mp3InfoHelper.getLength(audio.getUrl()).compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(r -> CustomToast.CreateCustomToast(getContext()).showToast(Mp3InfoHelper.getBitrate(getContext(), audio.getDuration(), r)),
                            e -> Utils.showErrorInAdapter((Activity) getContext(), e));
        }
    }

    private void get_lyrics(Audio audio) {
        audioListDisposable = mAudioInteractor.getLyrics(Settings.get().accounts().getCurrent(), audio.getLyricsId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> onAudioLyricsReceived(t, audio), t -> Utils.showErrorInAdapter((Activity) getContext(), t));
    }

    private void onAudioLyricsReceived(String Text, Audio audio) {
        String title = audio.getArtistAndTitle();

        new MaterialAlertDialogBuilder(getContext())
                .setIcon(R.drawable.dir_song)
                .setMessage(Text)
                .setTitle(title != null ? title : getContext().getString(R.string.get_lyrics))
                .setPositiveButton(R.string.button_ok, null)
                .setNeutralButton(R.string.copy_text, (dialog, which) -> {
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("response", Text);
                    clipboard.setPrimaryClip(clip);
                    CustomToast.CreateCustomToast(getContext()).showToast(R.string.copied_to_clipboard);
                })
                .setCancelable(true)
                .show();
    }

    public void dispose() {
        mPlayerDisposable.dispose();
        audios = Collections.emptyList();
    }

    private void updateDownloadState(@NonNull AudioHolder holder, @NonNull Audio audio) {
        if (audio.getDownloadIndicator() == 2) {
            holder.saved.setImageResource(R.drawable.remote_cloud);
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorSecondary(getContext()));
        } else {
            holder.saved.setImageResource(R.drawable.save);
            Utils.setColorFilter(holder.saved, CurrentTheme.getColorPrimary(getContext()));
        }
        holder.saved.setVisibility(audio.getDownloadIndicator() != 0 ? View.VISIBLE : View.GONE);
    }

    private void doMenu(AudioHolder holder, AttachmentsViewBinder.OnAttachmentsActionCallback mAttachmentsActionCallback, int position, View view, Audio audio, ArrayList<Audio> audios) {
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();

        menus.add(new OptionRequest(AudioOption.play_item_audio, getContext().getString(R.string.play), R.drawable.play, true));
        if (MusicPlaybackController.canPlayAfterCurrent(audio)) {
            menus.add(new OptionRequest(AudioOption.play_item_after_current_audio, getContext().getString(R.string.play_audio_after_current), R.drawable.play_next, false));
        }
        if (audio.getOwnerId() != Settings.get().accounts().getCurrent()) {
            menus.add(new OptionRequest(AudioOption.add_item_audio, getContext().getString(R.string.action_add), R.drawable.list_add, true));
            menus.add(new OptionRequest(AudioOption.add_and_download_button, getContext().getString(R.string.add_and_download_button), R.drawable.add_download, false));
        } else
            menus.add(new OptionRequest(AudioOption.add_item_audio, getContext().getString(R.string.delete), R.drawable.ic_outline_delete, true));
        menus.add(new OptionRequest(AudioOption.share_button, getContext().getString(R.string.share), R.drawable.ic_outline_share, true));
        menus.add(new OptionRequest(AudioOption.save_item_audio, getContext().getString(R.string.save), R.drawable.save, true));
        if (audio.getAlbumId() != 0)
            menus.add(new OptionRequest(AudioOption.open_album, getContext().getString(R.string.open_album), R.drawable.audio_album, false));
        menus.add(new OptionRequest(AudioOption.get_recommendation_by_audio, getContext().getString(R.string.get_recommendation_by_audio), R.drawable.music_mic, false));

        if (!isEmpty(audio.getMain_artists()))
            menus.add(new OptionRequest(AudioOption.goto_artist, getContext().getString(R.string.audio_goto_artist), R.drawable.artist_icon, false));

        if (audio.getLyricsId() != 0)
            menus.add(new OptionRequest(AudioOption.get_lyrics_menu, getContext().getString(R.string.get_lyrics_menu), R.drawable.lyric, false));

        menus.add(new OptionRequest(AudioOption.bitrate_item_audio, getContext().getString(R.string.get_bitrate), R.drawable.high_quality, false));
        menus.add(new OptionRequest(AudioOption.search_by_artist, getContext().getString(R.string.search_by_artist), R.drawable.magnify, true));
        menus.add(new OptionRequest(AudioOption.copy_url, getContext().getString(R.string.copy_url), R.drawable.content_copy, false));


        menus.header(firstNonEmptyString(audio.getArtist(), " ") + " - " + audio.getTitle(), R.drawable.song, audio.getThumb_image_little());
        menus.columns(2);
        menus.show(((FragmentActivity) getContext()).getSupportFragmentManager(), "audio_options", option -> {
            switch (option.getId()) {
                case AudioOption.play_item_audio:
                    mAttachmentsActionCallback.onAudioPlay(position, audios);
                    PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(getContext());
                    break;
                case AudioOption.play_item_after_current_audio:
                    MusicPlaybackController.playAfterCurrent(audio);
                    break;
                case AudioOption.share_button:
                    SendAttachmentsActivity.startForSendAttachments(getContext(), Settings.get().accounts().getCurrent(), audio);
                    break;
                case AudioOption.search_by_artist:
                    PlaceFactory.getSingleTabSearchPlace(Settings.get().accounts().getCurrent(), SearchContentType.AUDIOS, new AudioSearchCriteria(audio.getArtist(), true, false)).tryOpenWith(getContext());
                    break;
                case AudioOption.get_lyrics_menu:
                    get_lyrics(audio);
                    break;
                case AudioOption.get_recommendation_by_audio:
                    PlaceFactory.SearchByAudioPlace(Settings.get().accounts().getCurrent(), audio.getOwnerId(), audio.getId()).tryOpenWith(getContext());
                    break;
                case AudioOption.open_album:
                    PlaceFactory.getAudiosInAlbumPlace(Settings.get().accounts().getCurrent(), audio.getAlbum_owner_id(), audio.getAlbumId(), audio.getAlbum_access_key()).tryOpenWith(getContext());
                    break;
                case AudioOption.copy_url:
                    ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("response", audio.getUrl());
                    clipboard.setPrimaryClip(clip);
                    CustomToast.CreateCustomToast(getContext()).showToast(R.string.copied);
                    break;
                case AudioOption.add_item_audio:
                    boolean myAudio = audio.getOwnerId() == Settings.get().accounts().getCurrent();
                    if (myAudio) {
                        deleteTrack(Settings.get().accounts().getCurrent(), audio);
                    } else {
                        addTrack(Settings.get().accounts().getCurrent(), audio);
                    }
                    break;
                case AudioOption.add_and_download_button:
                    addTrack(Settings.get().accounts().getCurrent(), audio);
                case AudioOption.save_item_audio:
                    if (!AppPerms.hasReadWriteStoragePermission(getContext())) {
                        if (mAttachmentsActionCallback != null) {
                            mAttachmentsActionCallback.onRequestWritePermissions();
                        }
                        break;
                    }
                    audio.setDownloadIndicator(1);
                    updateDownloadState(holder, audio);
                    int ret = DownloadWorkUtils.doDownloadAudio(getContext(), audio, Settings.get().accounts().getCurrent(), false, false);
                    if (ret == 0)
                        CustomToast.CreateCustomToast(getContext()).showToastBottom(R.string.saved_audio);
                    else if (ret == 1 || ret == 2) {
                        Utils.ThemedSnack(view, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                                v1 -> DownloadWorkUtils.doDownloadAudio(getContext(), audio, Settings.get().accounts().getCurrent(), true, false)).show();
                    } else {
                        audio.setDownloadIndicator(0);
                        updateDownloadState(holder, audio);
                        CustomToast.CreateCustomToast(getContext()).showToastBottom(R.string.error_audio);
                    }
                    break;
                case AudioOption.bitrate_item_audio:
                    getMp3AndBitrate(Settings.get().accounts().getCurrent(), audio);
                    break;

                case AudioOption.goto_artist:
                    String[][] artists = Utils.getArrayFromHash(audio.getMain_artists());
                    if (audio.getMain_artists().keySet().size() > 1) {
                        new MaterialAlertDialogBuilder(getContext())
                                .setItems(artists[1], (dialog, which) -> PlaceFactory.getArtistPlace(Settings.get().accounts().getCurrent(), artists[0][which], false).tryOpenWith(getContext())).show();
                    } else {
                        PlaceFactory.getArtistPlace(Settings.get().accounts().getCurrent(), artists[0][0], false).tryOpenWith(getContext());
                    }
                    break;
            }
        });
    }

    private void doPlay(AudioHolder holder, AttachmentsViewBinder.OnAttachmentsActionCallback mAttachmentsActionCallback, int position, Audio audio, ArrayList<Audio> audios) {
        if (MusicPlaybackController.isNowPlayingOrPreparingOrPaused(audio)) {
            if (!Settings.get().other().isUse_stop_audio()) {
                updateAudioStatus(holder, audio);
                MusicPlaybackController.playOrPause();
            } else {
                updateAudioStatus(holder, audio);
                MusicPlaybackController.stop();
            }
        } else {
            updateAudioStatus(holder, audio);
            mAttachmentsActionCallback.onAudioPlay(position, audios);
        }
    }

    public void displayAudios(ArrayList<Audio> audios, AttachmentsViewBinder.OnAttachmentsActionCallback mAttachmentsActionCallback) {
        setVisibility(safeIsEmpty(audios) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(audios)) {
            dispose();
            return;
        }
        this.audios = audios;

        int i = audios.size() - getChildCount();
        for (int j = 0; j < i; j++) {
            addView(LayoutInflater.from(getContext()).inflate(R.layout.item_audio, this, false));
        }

        for (int g = 0; g < getChildCount(); g++) {
            ViewGroup root = (ViewGroup) getChildAt(g);
            if (g < audios.size()) {
                AudioHolder check = (AudioHolder) root.getTag();
                if (check == null) {
                    check = new AudioHolder(root);
                    root.setTag(check);
                }
                AudioHolder holder = check;
                Audio audio = audios.get(g);

                holder.tvTitle.setText(audio.getArtist());
                holder.tvSubtitle.setText(audio.getTitle());

                if (!audio.isLocal() && !audio.isLocalServer() && Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID && !audio.isHLS()) {
                    holder.quality.setVisibility(View.VISIBLE);
                    if (audio.getIsHq()) {
                        holder.quality.setImageResource(R.drawable.high_quality);
                    } else {
                        holder.quality.setImageResource(R.drawable.low_quality);
                    }
                } else {
                    holder.quality.setVisibility(View.GONE);
                }

                updateAudioStatus(holder, audio);
                int finalG = g;

                if (!isEmpty(audio.getThumb_image_little())) {
                    PicassoInstance.with()
                            .load(audio.getThumb_image_little())
                            .placeholder(java.util.Objects.requireNonNull(ResourcesCompat.getDrawable(getContext().getResources(), getAudioCoverSimple(), getContext().getTheme())))
                            .transform(TransformCover())
                            .tag(Constants.PICASSO_TAG)
                            .into(holder.play_cover);
                } else {
                    PicassoInstance.with().cancelRequest(holder.play_cover);
                    holder.play_cover.setImageResource(getAudioCoverSimple());
                }

                holder.ibPlay.setOnLongClickListener(v -> {
                    if (!isEmpty(audio.getThumb_image_very_big())
                            || !isEmpty(audio.getThumb_image_big()) || !isEmpty(audio.getThumb_image_little())) {
                        mAttachmentsActionCallback.onUrlPhotoOpen(firstNonEmptyString(audio.getThumb_image_very_big(),
                                audio.getThumb_image_big(), audio.getThumb_image_little()), audio.getArtist(), audio.getTitle());
                    }
                    return true;
                });

                holder.ibPlay.setOnClickListener(v -> {
                    if (Settings.get().main().isRevert_play_audio()) {
                        doMenu(holder, mAttachmentsActionCallback, finalG, v, audio, audios);
                    } else {
                        doPlay(holder, mAttachmentsActionCallback, finalG, audio, audios);
                    }
                });
                if (audio.getDuration() <= 0)
                    holder.time.setVisibility(View.INVISIBLE);
                else {
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(AppTextUtils.getDurationString(audio.getDuration()));
                }

                updateDownloadState(holder, audio);
                holder.lyric.setVisibility(audio.getLyricsId() != 0 ? View.VISIBLE : View.GONE);

                holder.my.setVisibility(audio.getOwnerId() == Settings.get().accounts().getCurrent() ? View.VISIBLE : View.GONE);
                holder.Track.setOnLongClickListener(v -> {
                    if (!AppPerms.hasReadWriteStoragePermission(getContext())) {
                        if (mAttachmentsActionCallback != null) {
                            mAttachmentsActionCallback.onRequestWritePermissions();
                        }
                        return false;
                    }
                    audio.setDownloadIndicator(1);
                    updateDownloadState(holder, audio);
                    int ret = DownloadWorkUtils.doDownloadAudio(getContext(), audio, Settings.get().accounts().getCurrent(), false, false);
                    if (ret == 0)
                        CustomToast.CreateCustomToast(getContext()).showToastBottom(R.string.saved_audio);
                    else if (ret == 1 || ret == 2) {
                        Utils.ThemedSnack(v, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                                v1 -> DownloadWorkUtils.doDownloadAudio(getContext(), audio, Settings.get().accounts().getCurrent(), true, false)).show();

                    } else {
                        audio.setDownloadIndicator(0);
                        updateDownloadState(holder, audio);
                        CustomToast.CreateCustomToast(getContext()).showToastBottom(R.string.error_audio);
                    }
                    return true;
                });

                holder.Track.setOnClickListener(view -> {
                    holder.cancelSelectionAnimation();
                    holder.startSomeAnimation();
                    if (Settings.get().main().isRevert_play_audio()) {
                        doPlay(holder, mAttachmentsActionCallback, finalG, audio, audios);
                    } else {
                        doMenu(holder, mAttachmentsActionCallback, finalG, view, audio, audios);
                    }
                });
                root.setVisibility(View.VISIBLE);
            } else {
                root.setVisibility(View.GONE);
            }
        }
        mPlayerDisposable.dispose();
        mPlayerDisposable = observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(this::onServiceBindEvent);
    }

    private void onServiceBindEvent(@MusicPlaybackController.PlayerStatus int status) {
        switch (status) {
            case MusicPlaybackController.PlayerStatus.UPDATE_TRACK_INFO:
            case MusicPlaybackController.PlayerStatus.UPDATE_PLAY_PAUSE:
            case MusicPlaybackController.PlayerStatus.SERVICE_KILLED:
                currAudio = MusicPlaybackController.getCurrentAudio();
                if (getChildCount() < audios.size())
                    return;
                for (int g = 0; g < audios.size(); g++) {
                    ViewGroup root = (ViewGroup) getChildAt(g);
                    AudioHolder holder = new AudioHolder(root);
                    updateAudioStatus(holder, audios.get(g));
                }
                break;
            case MusicPlaybackController.PlayerStatus.REPEATMODE_CHANGED:
            case MusicPlaybackController.PlayerStatus.SHUFFLEMODE_CHANGED:
            case MusicPlaybackController.PlayerStatus.UPDATE_PLAY_LIST:
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        currAudio = MusicPlaybackController.getCurrentAudio();
        if (!isEmpty(audios)) {
            mPlayerDisposable = observeServiceBinding()
                    .compose(RxUtils.applyObservableIOToMainSchedulers())
                    .subscribe(this::onServiceBindEvent);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayerDisposable.dispose();
        audioListDisposable.dispose();
    }

    private class AudioHolder {
        final TextView tvTitle;
        final TextView tvSubtitle;
        final View ibPlay;
        final ImageView play_cover;
        final TextView time;
        final ImageView saved;
        final ImageView lyric;
        final ImageView my;
        final ImageView quality;
        final View Track;
        final MaterialCardView selectionView;
        final MaterialCardView isSelectedView;
        final Animator.AnimatorListener animationAdapter;
        final RLottieImageView visual;
        ObjectAnimator animator;

        AudioHolder(View root) {
            tvTitle = root.findViewById(R.id.dialog_title);
            tvSubtitle = root.findViewById(R.id.dialog_message);
            ibPlay = root.findViewById(R.id.item_audio_play);
            play_cover = root.findViewById(R.id.item_audio_play_cover);
            time = root.findViewById(R.id.item_audio_time);
            saved = root.findViewById(R.id.saved);
            lyric = root.findViewById(R.id.lyric);
            Track = root.findViewById(R.id.track_option);
            my = root.findViewById(R.id.my);
            selectionView = root.findViewById(R.id.item_audio_selection);
            isSelectedView = root.findViewById(R.id.item_audio_select_add);
            isSelectedView.setVisibility(View.GONE);
            quality = root.findViewById(R.id.quality);
            visual = root.findViewById(R.id.item_audio_visual);
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

        void startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(getContext()));
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
