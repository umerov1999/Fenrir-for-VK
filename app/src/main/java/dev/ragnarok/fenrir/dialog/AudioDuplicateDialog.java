package dev.ragnarok.fenrir.dialog;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.BaseMvpDialogFragment;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AudioDuplicatePresenter;
import dev.ragnarok.fenrir.mvp.view.IAudioDuplicateView;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.picasso.transforms.PolyTransformation;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class AudioDuplicateDialog extends BaseMvpDialogFragment<AudioDuplicatePresenter, IAudioDuplicateView>
        implements IAudioDuplicateView {

    public static final String REQUEST_CODE_AUDIO_DUPLICATE = "request_audio_duplicate";

    private AudioHolder newAudio;
    private AudioHolder oldAudio;
    private TextView newBitrate;
    private TextView oldBitrate;
    private MaterialButton bBitrate;

    private static Audio getAudioContent(Context context, String filePath, int accountId) {
        String[] AUDIO_PROJECTION = {BaseColumns._ID, MediaStore.MediaColumns.DURATION, MediaStore.MediaColumns.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_PROJECTION,
                MediaStore.MediaColumns.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
            String data = PicassoInstance.buildUriForPicassoNew(Content_Local.AUDIO, id).toString();

            if (Utils.isEmpty(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)))) {
                cursor.close();
                return null;
            }
            String TrackName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)).replace(".mp3", "");
            String Artist = "";
            String[] arr = TrackName.split(" - ");
            if (arr.length > 1) {
                Artist = arr[0];
                TrackName = TrackName.replace(Artist + " - ", "");
            }

            int dur = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.DURATION));
            if (dur != 0) {
                dur /= 1000;
            }

            Audio ret = new Audio().setId(data.hashCode()).setOwnerId(accountId).setDuration(dur)
                    .setUrl(data).setTitle(TrackName).setArtist(Artist);
            cursor.close();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return ret.setThumb_image_big(data).setThumb_image_little(data);
            } else {
                String uri = PicassoInstance.buildUriForPicasso(Content_Local.AUDIO, id).toString();
                return ret.setThumb_image_big(uri).setThumb_image_little(uri);
            }
        }
        return null;
    }

    public static @Nullable
    AudioDuplicateDialog newInstance(@NonNull Context context, int aid, Audio new_audio, String old_audio) {
        Audio old = getAudioContent(context, old_audio, aid);
        if (old == null) {
            return null;
        }
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putParcelable(Extra.NEW, new_audio);
        args.putParcelable(Extra.OLD, old);
        AudioDuplicateDialog dialog = new AudioDuplicateDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(requireActivity(), R.layout.dialog_audio_duplicate, null);

        Dialog dialog = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.save)
                .setIcon(R.drawable.dir_song)
                .setView(view)
                .setPositiveButton(R.string.dual_track, (dialog1, which) -> returnSelection(true))
                .setNegativeButton(R.string.new_track, (dialog12, which) -> returnSelection(false))
                .setNeutralButton(R.string.button_cancel, null)
                .create();

        newAudio = new AudioHolder(view.findViewById(R.id.item_new_audio));
        oldAudio = new AudioHolder(view.findViewById(R.id.item_old_audio));
        newBitrate = view.findViewById(R.id.item_new_bitrate);
        oldBitrate = view.findViewById(R.id.item_old_bitrate);
        bBitrate = view.findViewById(R.id.item_get_bitrate);
        bBitrate.setOnClickListener(v -> {
            callPresenter(p -> p.getBitrateAll(requireActivity()));
            bBitrate.setVisibility(View.GONE);
        });

        fireViewCreated();
        return dialog;
    }

    private void updateAudioStatus(AudioHolder holder, Audio audio) {
        if (!audio.equals(MusicPlaybackController.getCurrentAudio())) {
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

    private Transformation TransformCover() {
        return Settings.get().main().isAudio_round_icon() ? new RoundTransformation() : new PolyTransformation();
    }

    @DrawableRes
    private int getAudioCoverSimple() {
        return Settings.get().main().isAudio_round_icon() ? R.drawable.audio_button : R.drawable.audio_button_material;
    }

    private void bind(AudioHolder holder, Audio audio) {
        holder.artist.setText(audio.getArtist());
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
                    .placeholder(Objects.requireNonNull(ResourcesCompat.getDrawable(requireActivity().getResources(), getAudioCoverSimple(), requireActivity().getTheme())))
                    .transform(TransformCover())
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.play_cover);
        } else {
            PicassoInstance.with().cancelRequest(holder.play_cover);
            holder.play_cover.setImageResource(getAudioCoverSimple());
        }
        holder.play.setOnClickListener(v -> {
            if (MusicPlaybackController.isNowPlayingOrPreparingOrPaused(audio)) {
                if (!Settings.get().other().isUse_stop_audio()) {
                    MusicPlaybackController.playOrPause();
                } else {
                    MusicPlaybackController.stop();
                }
            } else {
                MusicPlaybackService.startForPlayList(requireActivity(), new ArrayList<>(Collections.singletonList(audio)), 0, false);
            }
        });
    }

    @Override
    public void displayData(Audio new_audio, Audio old_audio) {
        if (nonNull(newAudio) && nonNull(oldAudio)) {
            bind(newAudio, new_audio);
            bind(oldAudio, old_audio);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setOldBitrate(@Nullable Integer bitrate) {
        if (bitrate != null) {
            oldBitrate.setVisibility(View.VISIBLE);
            oldBitrate.setText(bitrate + " kbps");
        } else {
            oldBitrate.setVisibility(View.GONE);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void setNewBitrate(@Nullable Integer bitrate) {
        if (bitrate != null) {
            newBitrate.setVisibility(View.VISIBLE);
            newBitrate.setText(bitrate + " kbps");
        } else {
            newBitrate.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateShowBitrate(boolean needShow) {
        bBitrate.setVisibility(needShow ? View.VISIBLE : View.GONE);
    }

    private void returnSelection(boolean type) {
        Bundle intent = new Bundle();
        intent.putBoolean(Extra.TYPE, type);

        if (getArguments() != null) {
            intent.putAll(getArguments());
        }
        getParentFragmentManager().setFragmentResult(REQUEST_CODE_AUDIO_DUPLICATE, intent);
        dismiss();
    }

    @NonNull
    @Override
    public IPresenterFactory<AudioDuplicatePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudioDuplicatePresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.NEW),
                requireArguments().getParcelable(Extra.OLD),
                saveInstanceState
        );
    }

    static class AudioHolder {

        final TextView artist;
        final TextView title;
        final View play;
        final ImageView play_cover;
        final View Track;
        final MaterialCardView selectionView;
        final RLottieImageView visual;
        final TextView time;

        AudioHolder(View itemView) {
            artist = itemView.findViewById(R.id.dialog_title);
            title = itemView.findViewById(R.id.dialog_message);
            time = itemView.findViewById(R.id.item_audio_time);
            play = itemView.findViewById(R.id.item_audio_play);
            play_cover = itemView.findViewById(R.id.item_audio_play_cover);
            Track = itemView.findViewById(R.id.track_option);
            selectionView = itemView.findViewById(R.id.item_audio_selection);
            visual = itemView.findViewById(R.id.item_audio_visual);
        }
    }
}
