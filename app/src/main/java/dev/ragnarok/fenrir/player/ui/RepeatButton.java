package dev.ragnarok.fenrir.player.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.player.MusicPlaybackController;
import dev.ragnarok.fenrir.player.MusicPlaybackService;

public class RepeatButton extends AppCompatImageButton implements OnClickListener {

    public RepeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MusicPlaybackController.cycleRepeat();
        updateRepeatState();
    }

    public void updateRepeatState() {
        switch (MusicPlaybackController.getRepeatMode()) {
            case MusicPlaybackService.REPEAT_ALL:
                setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.repeat));
                break;
            case MusicPlaybackService.REPEAT_CURRENT:
                setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.repeat_once));
                break;
            case MusicPlaybackService.REPEAT_NONE:
                setImageDrawable(AppCompatResources.getDrawable(getContext(), R.drawable.repeat_off));
                break;
            default:
                break;
        }
    }
}
