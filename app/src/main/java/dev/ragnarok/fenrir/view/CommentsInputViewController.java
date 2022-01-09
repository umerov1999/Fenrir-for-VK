package dev.ragnarok.fenrir.view;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.view.emoji.EmojiconsPopup;

public class CommentsInputViewController {

    private final Context mActivity;
    private final OnInputActionCallback callback;
    private final TextInputEditText mInputField;
    private final LinearLayout rlEmojiContainer;
    private final ImageView ibEmoji;
    private final ImageView ibAttach;
    private final TextView tvAttCount;
    private final ImageView mButtonSend;
    private final TextWatcherAdapter mTextWatcher;
    private final int mIconColorActive;
    private final int mIconColorInactive;
    private EmojiconsPopup emojiPopup;
    private boolean emojiOnScreen;
    private boolean emojiNeed;
    private boolean mCanSendNormalMessage;
    private boolean sendOnEnter;

    public CommentsInputViewController(@NonNull Activity activity, @NonNull View rootView, @NonNull OnInputActionCallback callback) {
        this.callback = callback;
        mActivity = activity.getApplicationContext();

        mInputField = rootView.findViewById(R.id.fragment_input_text);
        mTextWatcher = new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callback.onInputTextChanged(s.toString());
            }
        };

        mIconColorActive = CurrentTheme.getColorPrimary(activity);
        mIconColorInactive = CurrentTheme.getColorOnSurface(activity);


        mButtonSend = rootView.findViewById(R.id.buttonSend);
        mButtonSend.setOnClickListener(v -> onSendButtonClick());
        mButtonSend.setOnLongClickListener(v -> callback.onSendLongClick());

        tvAttCount = rootView.findViewById(R.id.fragment_input_att_count);

        rlEmojiContainer = rootView.findViewById(R.id.fragment_input_emoji_container);

        ibAttach = rootView.findViewById(R.id.buttonAttach);
        ibEmoji = rootView.findViewById(R.id.buttonEmoji);

        ibAttach.setOnClickListener(view -> callback.onAttachClick());
        ibEmoji.setOnClickListener(view -> onEmojiButtonClick());

        mInputField.addTextChangedListener(mTextWatcher);
        mInputField.setOnClickListener(view -> showEmoji(false));

        mInputField.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (sendOnEnter && i == EditorInfo.IME_ACTION_SEND) {
                callback.onSendClicked();
                return true;
            }
            return false;
        });

        emojiPopup = new EmojiconsPopup(rootView, activity);
        setupEmojiView();
    }

    public void destroyView() {
        emojiPopup.destroy();
        emojiPopup = null;
    }

    private void onEmojiButtonClick() {
        if (emojiPopup.isKeyBoardOpen()) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mInputField.getWindowToken(), 0);

            emojiNeed = true;
            //ibEmoji.setImageResource(R.drawable.keyboard_arrow_down);
        } else {
            showEmoji(!emojiOnScreen);
            //ibEmoji.setImageResource(R.drawable.emoticon);
        }
    }

    public void setSendOnEnter(boolean sendOnEnter) {
        this.sendOnEnter = sendOnEnter;
        if (sendOnEnter) {
            mInputField.setImeOptions(EditorInfo.IME_ACTION_SEND | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
            mInputField.setSingleLine();
        }
    }

    private void showEmoji(boolean visible) {
        if (emojiOnScreen == visible) {
            return;
        }

        if (visible && rlEmojiContainer.getChildCount() == 0) {
            View emojiView = emojiPopup.getEmojiView(rlEmojiContainer);
            rlEmojiContainer.addView(emojiView);
        }

        rlEmojiContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        emojiOnScreen = visible;
    }

    private void setupEmojiView() {
        emojiPopup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {
            @Override
            public void onKeyboardOpen() {
                if (emojiOnScreen) {
                    showEmoji(false);
                }
                ibEmoji.setImageResource(emojiOnScreen ? R.drawable.keyboard_arrow_down : R.drawable.emoticon);
            }

            @Override
            public void onKeyboardClose() {
                if (emojiNeed) {
                    showEmoji(true);
                    emojiNeed = false;
                }

                ibEmoji.setImageResource(emojiOnScreen ? R.drawable.keyboard_arrow_down : R.drawable.emoticon);
            }
        });

        emojiPopup.setOnEmojiconClickedListener(emojicon -> EmojiconsPopup.input(mInputField, emojicon));
        emojiPopup.setOnEmojiconBackspaceClickedListener(v -> {
            KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
            mInputField.dispatchKeyEvent(event);
        });
    }

    private String getText() {
        return mInputField.getText().toString();
    }

    private String getTrimmedText() {
        return getText().trim();
    }

    public void setTextQuietly(String text) {
        mInputField.removeTextChangedListener(mTextWatcher);
        mInputField.setText(text);
        mInputField.addTextChangedListener(mTextWatcher);
    }

    public TextInputEditText getInputField() {
        return mInputField;
    }

    public void setAttachmentsCount(int count) {
        tvAttCount.setText(String.valueOf(count));
        tvAttCount.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
        tvAttCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, count > 9 ? 10 : 12);

        int color = count > 0 ? mIconColorActive : mIconColorInactive;
        tvAttCount.setTextColor(color);
        ibAttach.getDrawable().setTint(color);
    }

    public boolean onBackPressed() {
        if (emojiOnScreen) {
            showEmoji(false);
            return false;
        }

        return true;
    }

    public void setOnSickerClickListener(EmojiconsPopup.OnStickerClickedListener sickerClickListener) {
        if (emojiPopup != null) {
            emojiPopup.setOnStickerClickedListener(sickerClickListener);
        }
    }

    private void onSendButtonClick() {
        callback.onSendClicked();
    }

    private void resolveSendButton() {
        mButtonSend.getDrawable().setTint(mCanSendNormalMessage ? mIconColorActive : mIconColorInactive);
    }

    public void setCanSendNormalMessage(boolean canSend) {
        mCanSendNormalMessage = canSend;
        resolveSendButton();
    }

    public EmojiconsPopup getEmojiPopup() {
        return emojiPopup;
    }

    public interface OnInputActionCallback {
        void onInputTextChanged(String s);

        void onSendClicked();

        boolean onSendLongClick();

        void onAttachClick();
    }
}