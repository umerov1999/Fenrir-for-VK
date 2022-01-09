package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.LayoutRes;

import dev.ragnarok.fenrir.R;

public class KeyboardView extends FrameLayout {

    private OnKeyboardClickListener mOnKeyboardClickListener;

    public KeyboardView(Context context) {
        super(context);
        init(context, null);
    }

    public KeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public KeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        @LayoutRes
        int layoutRes;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.KeyboardView, 0, 0);
        try {
            String theme = a.getString(R.styleable.KeyboardView_keyboard_theme);
            if (TextUtils.isEmpty(theme)) {
                theme = "fullscreen";
            }

            layoutRes = "dialog".equals(theme) ? R.layout.dialog_pin_keyboard : R.layout.fragment_pin_keyboard;
        } finally {
            a.recycle();
        }

        LayoutInflater.from(context).inflate(layoutRes, this, true);

        View[] digitsButtons = new View[10];
        digitsButtons[0] = findViewById(R.id.button0);
        digitsButtons[1] = findViewById(R.id.button1);
        digitsButtons[2] = findViewById(R.id.button2);
        digitsButtons[3] = findViewById(R.id.button3);
        digitsButtons[4] = findViewById(R.id.button4);
        digitsButtons[5] = findViewById(R.id.button5);
        digitsButtons[6] = findViewById(R.id.button6);
        digitsButtons[7] = findViewById(R.id.button7);
        digitsButtons[8] = findViewById(R.id.button8);
        digitsButtons[9] = findViewById(R.id.button9);

        for (int i = 0; i < digitsButtons.length; i++) {
            int finalI = i;
            digitsButtons[i].setOnClickListener(v -> onDigitButtonClick(finalI));
        }

        findViewById(R.id.buttonBackspace).setOnClickListener(v -> {
            if (mOnKeyboardClickListener != null) {
                mOnKeyboardClickListener.onBackspaceClick();
            }
        });

        findViewById(R.id.buttonFingerprint).setOnClickListener(v -> {
            if (mOnKeyboardClickListener != null) {
                mOnKeyboardClickListener.onFingerPrintClick();
            }
        });
    }

    private void onDigitButtonClick(int num) {
        if (mOnKeyboardClickListener != null) {
            mOnKeyboardClickListener.onButtonClick(num);
        }
    }

    public void setOnKeyboardClickListener(OnKeyboardClickListener onKeyboardClickListener) {
        mOnKeyboardClickListener = onKeyboardClickListener;
    }

    public interface OnKeyboardClickListener {
        void onButtonClick(int number);

        void onBackspaceClick();

        void onFingerPrintClick();
    }
}
