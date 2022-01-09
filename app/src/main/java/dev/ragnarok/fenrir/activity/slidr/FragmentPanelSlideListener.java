package dev.ragnarok.fenrir.activity.slidr;


import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import dev.ragnarok.fenrir.activity.slidr.model.SlidrConfig;
import dev.ragnarok.fenrir.activity.slidr.widget.SliderPanel;


class FragmentPanelSlideListener implements SliderPanel.OnPanelSlideListener {

    private final View view;
    private final SlidrConfig config;


    FragmentPanelSlideListener(@NonNull View view, @NonNull SlidrConfig config) {
        this.view = view;
        this.config = config;
    }


    @Override
    public void onStateChanged(int state) {
        if (config.getListener() != null) {
            config.getListener().onSlideStateChanged(state);
        }
    }


    @Override
    public void onClosed() {
        if (config.getListener() != null) {
            if (config.getListener().onSlideClosed()) {
                return;
            }
        }

        // Ensure that we are attached to a FragmentActivity
        if (view.getContext() instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) view.getContext();
            if (activity.getSupportFragmentManager().getBackStackEntryCount() == 0) {
                activity.finish();
                activity.overridePendingTransition(0, 0);
            } else {
                activity.getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onOpened() {
        if (config.getListener() != null) {
            config.getListener().onSlideOpened();
        }
    }


    @Override
    public void onSlideChange(float percent) {
        if (config.getListener() != null) {
            config.getListener().onSlideChange(percent);
        }
    }
}
