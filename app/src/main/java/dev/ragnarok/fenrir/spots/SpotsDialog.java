package dev.ragnarok.fenrir.spots;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class SpotsDialog extends AlertDialog {

    private static final int DELAY = 150;
    private static final int DURATION = 1500;
    private int size;
    private AnimatedView[] spots;
    private AnimatorPlayer animator;
    private CharSequence message;

    private SpotsDialog(Context context, String message, boolean cancelable, OnCancelListener cancelListener) {
        super(context);
        this.message = message;
        setCancelable(cancelable);
        if (cancelListener != null) setOnCancelListener(cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Objects.requireNonNull(getWindow()).setBackgroundDrawableResource(android.R.color.transparent);
        } catch (NullPointerException ignored) {
        }
        setContentView(R.layout.dmax_spots_dialog);
        setCanceledOnTouchOutside(false);

        initMessage();
        initProgress();
    }

    @Override
    protected void onStart() {
        super.onStart();

        for (AnimatedView view : spots) view.setVisibility(View.VISIBLE);

        animator = new AnimatorPlayer(createAnimations());
        animator.play();
    }

    @Override
    protected void onStop() {
        super.onStop();

        animator.stop();
    }

    @Override
    public void setMessage(CharSequence message) {
        this.message = message;
        if (isShowing()) initMessage();
    }

    private void initMessage() {
        if (!Utils.isEmpty(message)) {
            ((MaterialTextView) findViewById(R.id.dmax_spots_title)).setText(message);
        }
    }

    private void initProgress() {
        FrameLayout progress = findViewById(R.id.dmax_spots_progress);
        size = 10;

        spots = new AnimatedView[size];
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.spot_size);
        int progressWidth = getContext().getResources().getDimensionPixelSize(R.dimen.progress_width);
        for (int i = 0; i < spots.length; i++) {
            AnimatedView v = new AnimatedView(getContext());
            v.setBackgroundResource(R.drawable.dmax_spots_spot);
            v.setTarget(progressWidth);
            v.setXFactor(-1f);
            v.setVisibility(View.INVISIBLE);
            progress.addView(v, size, size);
            spots[i] = v;
        }
    }

    private Animator[] createAnimations() {
        Animator[] animators = new Animator[size];
        for (int i = 0; i < spots.length; i++) {
            AnimatedView animatedView = spots[i];
            @SuppressLint("Recycle") Animator move = ObjectAnimator.ofFloat(animatedView, "xFactor", 0, 1);
            move.setDuration(DURATION);
            move.setInterpolator(new HesitateInterpolator());
            move.setStartDelay((long) DELAY * i);
            move.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animatedView.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    animatedView.setVisibility(View.VISIBLE);
                }
            });
            animators[i] = move;
        }
        return animators;
    }

    public static class Builder {

        private Context context;
        private String message;
        private int messageId;
        private boolean cancelable = true; // default dialog behaviour
        private OnCancelListener cancelListener;

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(@StringRes int messageId) {
            this.messageId = messageId;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }

        public Builder setCancelListener(OnCancelListener cancelListener) {
            this.cancelListener = cancelListener;
            return this;
        }

        public AlertDialog build() {
            if (!Settings.get().other().isNew_loading_dialog() || !Utils.hasMarshmallow() || !FenrirNative.isNativeLoaded()) {
                return new SpotsDialog(
                        context,
                        messageId != 0 ? context.getString(messageId) : message,
                        cancelable,
                        cancelListener
                );
            }
            View root = View.inflate(context, R.layout.dialog_progress, null);
            ((MaterialTextView) root.findViewById(R.id.item_progress_text)).setText(messageId != 0 ? context.getString(messageId) : message);
            RLottieImageView anim = root.findViewById(R.id.lottie_animation);
            anim.fromRes(R.raw.s_loading, Utils.dp(180), Utils.dp(180), new int[]{0x333333, CurrentTheme.getColorPrimary(context), 0x777777, CurrentTheme.getColorSecondary(context)});
            anim.playAnimation();
            return new MaterialAlertDialogBuilder(context).setView(root)
                    .setCancelable(cancelable)
                    .setOnCancelListener(cancelListener).create();
        }
    }
}
