package dev.ragnarok.fenrir.player.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.widget.AppCompatImageButton;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.player.MusicPlaybackService;

public class ShuffleButton extends AppCompatImageButton implements OnClickListener {
    public ShuffleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MusicPlaybackController.cycleShuffle();
        updateShuffleState();
    }

    public void updateShuffleState() {
        switch (MusicPlaybackController.getShuffleMode()) {
            case MusicPlaybackService.SHUFFLE:
                setImageResource(R.drawable.shuffle);
                break;
            case MusicPlaybackService.SHUFFLE_NONE:
                setImageResource(R.drawable.shuffle_disabled);
                break;
            default:
                break;
        }
    }

}
