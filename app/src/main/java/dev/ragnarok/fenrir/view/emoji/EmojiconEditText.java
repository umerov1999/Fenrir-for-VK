package dev.ragnarok.fenrir.view.emoji;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.settings.Settings;

public class EmojiconEditText extends TextInputEditText {

    private int mEmojiconSize;

    public EmojiconEditText(Context context) {
        this(context, null);
    }

    public EmojiconEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mEmojiconSize = (int) getTextSize();

        @SuppressLint("CustomViewStyleable") TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.Emojicon);
        mEmojiconSize = (int) a.getDimension(R.styleable.Emojicon_emojiconSize, getTextSize());
        a.recycle();
        setText(getText());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (!isInEditMode() && !Settings.get().ui().isSystemEmoji()) {
            EmojiconHandler.addEmojis(getContext(), getText(), mEmojiconSize);
        }
    }

    /**
     * Set the size of emojicon in pixels.
     */
    public void setEmojiconSize(int pixels) {
        mEmojiconSize = pixels;
    }
}
