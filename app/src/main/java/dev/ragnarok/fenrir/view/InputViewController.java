package dev.ragnarok.fenrir.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.emoji.BotKeyboardView;
import dev.ragnarok.fenrir.view.emoji.EmojiconEditText;
import dev.ragnarok.fenrir.view.emoji.EmojiconsPopup;

public class InputViewController {

    private final Context mActivity;
    private final OnInputActionCallback callback;
    private final EmojiconEditText mInputField;
    private final LinearLayout rlEmojiContainer;
    private final ImageView ibEmoji;
    private final ImageView ibAttach;
    private final ViewGroup vgInputViewHolder;
    private final ViewGroup vgMessageInput;
    private final ViewGroup vgVoiceInput;
    private final TextView tvAttCount;
    private final ImageView mButtonSend;
    private final ImageView mRecordResumePause;
    private final TextWatcherAdapter mTextWatcher;
    private final int mIconColorActive;
    private final int mIconColorInactive;
    private final TextView mRecordingDuration;
    private final ImageView currentKeyboardShow;
    private BotKeyboardView botKeyboard;
    private EmojiconsPopup emojiPopup;
    private boolean emojiOnScreen;
    private boolean emojiNeed;
    private boolean keyboardOnScreen;
    private boolean sendOnEnter;
    private int mCurrentMode = Mode.NORMAL;
    private boolean canEditingSave;
    private boolean canNormalSend;
    private boolean canStartRecording;
    private RecordActionsCallback mRecordActionsCallback;

    public InputViewController(@NonNull Activity activity, @NonNull View rootView, @NonNull OnInputActionCallback callback) {
        this.callback = callback;
        mActivity = activity.getApplicationContext();

        mRecordingDuration = rootView.findViewById(R.id.recording_duration);

        mInputField = rootView.findViewById(R.id.fragment_input_text);
        mTextWatcher = new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callback.onInputTextChanged(s.toString());
            }
        };

        vgInputViewHolder = rootView.findViewById(R.id.fragment_input_container);
        vgMessageInput = rootView.findViewById(R.id.message_input_container);
        vgVoiceInput = rootView.findViewById(R.id.voice_input_container);

        mIconColorActive = CurrentTheme.getColorPrimary(activity);
        mIconColorInactive = CurrentTheme.getColorOnSurface(activity);

        mButtonSend = rootView.findViewById(R.id.buttonSend);
        mButtonSend.setOnClickListener(v -> onSendButtonClick());
        mButtonSend.setOnLongClickListener(v -> {
            if (canStartRecording && mCurrentMode == Mode.NORMAL && mRecordActionsCallback != null) {
                mRecordActionsCallback.onRecordCustomClick();
                return true;
            }
            return false;
        });

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
                callback.onSendClicked(getTrimmedText());
                return true;
            }
            return false;
        });


        emojiPopup = new EmojiconsPopup(rootView, activity);
        setupEmojiView();

        rootView.findViewById(R.id.cancel_voice_message).setOnClickListener(v -> cancelVoiceMessageRecording());

        mRecordResumePause = rootView.findViewById(R.id.pause_voice_message);
        mRecordResumePause.setOnClickListener(v -> onResumePauseButtonClick());

        botKeyboard = rootView.findViewById(R.id.fragment_input_keyboard_container);
        currentKeyboardShow = rootView.findViewById(R.id.buttonBotKeyboard);
        currentKeyboardShow.setOnClickListener(v -> {
            if (keyboardOnScreen) {
                closeBotKeyboard();
            } else {
                keyboardOnScreen = true;
                botKeyboard.setVisibility(View.VISIBLE);
                Utils.setColorFilter(currentKeyboardShow, CurrentTheme.getColorPrimary(currentKeyboardShow.getContext()));
                if (emojiOnScreen) {
                    showEmoji(false);
                    ibEmoji.setImageResource(emojiOnScreen ? R.drawable.keyboard_arrow_down : R.drawable.emoticon);
                }
                if (emojiPopup.isKeyBoardOpen()) {
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mInputField.getWindowToken(), 0);
                }
            }
        });
        resolveModeViews();
    }

    public void setKeyboardBotClickListener(BotKeyboardView.BotKeyboardViewDelegate listener) {
        botKeyboard.setDelegate(listener);
    }

    public void setKeyboardBotLongClickListener(View.OnLongClickListener listener) {
        currentKeyboardShow.setOnLongClickListener(listener);
    }

    public void closeBotKeyboard() {
        if (!keyboardOnScreen) {
            return;
        }
        keyboardOnScreen = false;
        botKeyboard.setVisibility(View.GONE);
        currentKeyboardShow.clearColorFilter();
    }

    public boolean updateBotKeyboard(Keyboard currentKeyboard, boolean show) {
        boolean ret;
        if (Objects.isNull(currentKeyboard) || currentKeyboard.getButtons().size() <= 0) {
            botKeyboard.setVisibility(View.GONE);
            ret = botKeyboard.setButtons(null, false);
            keyboardOnScreen = false;
            currentKeyboardShow.setVisibility(View.GONE);
        } else {
            botKeyboard.setVisibility(show ? (Settings.get().main().isShow_bot_keyboard() ? View.VISIBLE : View.GONE) : View.GONE);
            ret = botKeyboard.setButtons(currentKeyboard.getButtons(), currentKeyboard.getOne_time() && !currentKeyboard.getInline());
            keyboardOnScreen = show;
            currentKeyboardShow.setVisibility(Settings.get().main().isShow_bot_keyboard() ? View.VISIBLE : View.GONE);
            if (show) {
                Utils.setColorFilter(currentKeyboardShow, CurrentTheme.getColorPrimary(currentKeyboardShow.getContext()));
            } else {
                currentKeyboardShow.clearColorFilter();
            }
        }
        return ret;
    }

    public void storeEmoji() {
        if (emojiPopup != null) {
            emojiPopup.storeState();
        }
    }

    public void destroyView() {
        emojiPopup.destroy();
        emojiPopup = null;
        botKeyboard.destroy();
        botKeyboard = null;
    }

    private void onResumePauseButtonClick() {
        if (mRecordActionsCallback != null) {
            mRecordActionsCallback.onResumePauseClick();
        }
    }

    private void cancelVoiceMessageRecording() {
        if (mRecordActionsCallback != null) {
            mRecordActionsCallback.onRecordCancel();
        }
    }

    private void onEmojiButtonClick() {
        if (emojiPopup.isKeyBoardOpen()) {
            InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mInputField.getWindowToken(), 0);
            emojiNeed = true;
            closeBotKeyboard();
            //ibEmoji.setImageResource(R.drawable.keyboard_arrow_down);
        } else {
            if (!emojiOnScreen) {
                closeBotKeyboard();
            }
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

    public void showEmoji(boolean visible) {
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
                closeBotKeyboard();
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
        mInputField.requestFocus();
        if (!Utils.isEmpty(text))
            mInputField.setSelection(text.length());
        mInputField.addTextChangedListener(mTextWatcher);
    }

    @SuppressLint("SetTextI18n")
    public void AppendTextQuietly(String text) {
        if (text != null) {
            mInputField.removeTextChangedListener(mTextWatcher);
            String txt = Utils.firstNonEmptyString(mInputField.getText().toString(), " ");
            if (txt.lastIndexOf('@') != -1) {
                txt = txt.substring(0, txt.length() - 1);
            }
            mInputField.setText(txt + " " + text);
            mInputField.requestFocus();
            if (!Utils.isEmpty(text))
                mInputField.setSelection(mInputField.getText().length());
            callback.onInputTextChanged(txt + " " + text);
            mInputField.addTextChangedListener(mTextWatcher);
        }
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
        if (mCurrentMode == Mode.VOICE_RECORD) {
            cancelVoiceMessageRecording();
            return false;
        }

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

    public void setOnMySickerClickListener(EmojiconsPopup.OnMyStickerClickedListener sickerMyClickListener) {
        if (emojiPopup != null) {
            emojiPopup.setMyOnStickerClickedListener(sickerMyClickListener);
        }
    }

    private void switchModeTo(int mode) {
        if (mCurrentMode != mode) {
            mCurrentMode = mode;
            resolveModeViews();
        }
    }

    private void onSendButtonClick() {
        switch (mCurrentMode) {
            case Mode.NORMAL:
                if (canNormalSend) {
                    callback.onSendClicked(getTrimmedText());
                } else if (canStartRecording) {
                    if (mRecordActionsCallback != null) {
                        mRecordActionsCallback.onSwithToRecordMode();
                    }
                }
                break;

            case Mode.VOICE_RECORD:
                if (mRecordActionsCallback != null) {
                    mRecordActionsCallback.onRecordSendClick();
                }
                break;
            case Mode.EDITING:
                callback.onSaveClick();
                break;
        }
    }

    private void resolveModeViews() {
        switch (mCurrentMode) {
            case Mode.NORMAL:
//                vgInputViewHolder.setVisibility(View.VISIBLE);
                vgVoiceInput.setVisibility(View.GONE);
                vgMessageInput.setVisibility(View.VISIBLE);
                break;
            case Mode.VOICE_RECORD:
//                vgInputViewHolder.setVisibility(View.VISIBLE);
                vgVoiceInput.setVisibility(View.VISIBLE);
                vgMessageInput.setVisibility(View.GONE);
                break;
            case Mode.DISABLED:
                vgInputViewHolder.setVisibility(View.GONE);
                break;
        }
    }

    private void resolveSendButton() {
        switch (mCurrentMode) {
            case Mode.VOICE_RECORD:
                mButtonSend.setImageResource(R.drawable.check);
                setupPrimaryButton(true);
                break;
            case Mode.NORMAL:
                mButtonSend.setImageResource(!canNormalSend && canStartRecording ? R.drawable.voice : R.drawable.send);
                setupPrimaryButton(canNormalSend || canStartRecording);
                break;
            case Mode.EDITING:
                mButtonSend.setImageResource(R.drawable.check);
                setupPrimaryButton(canEditingSave);
                break;
        }
    }

    private void setupPrimaryButton(boolean active) {
        mButtonSend.getDrawable().setTint(active ? mIconColorActive : mIconColorInactive);
    }

    public void setupRecordPauseButton(boolean visible, boolean isRecording) {
        mRecordResumePause.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        mRecordResumePause.setImageResource(visible ? (isRecording ? R.drawable.pause : R.drawable.play) : R.drawable.pause_disabled);
    }

    public void switchModeToEditing(boolean canSave) {
        switchModeTo(Mode.EDITING);
        canEditingSave = canSave;
        resolveSendButton();
    }

    public void switchModeToNormal(boolean canSend, boolean canStartRecoring) {
        switchModeTo(Mode.NORMAL);
        canNormalSend = canSend;
        canStartRecording = canStartRecoring;
        resolveSendButton();
    }

    public void switchModeToRecording() {
        switchModeTo(Mode.VOICE_RECORD);
        resolveSendButton();
    }

    public void switchModeToDisabled() {
        switchModeTo(Mode.DISABLED);
    }

    public void setRecordActionsCallback(RecordActionsCallback recordActionsCallback) {
        mRecordActionsCallback = recordActionsCallback;
    }

    public void setRecordingDuration(long time) {
        String str = AppTextUtils.getDurationString((int) (time / 1000));
        mRecordingDuration.setText(mActivity.getString(R.string.recording_time, str));
    }

    public interface OnInputActionCallback {
        void onInputTextChanged(String s);

        void onSendClicked(String body);

        void onAttachClick();

        void onSaveClick();
    }

    public interface RecordActionsCallback {
        void onRecordCancel();

        void onSwithToRecordMode();

        void onRecordSendClick();

        void onRecordCustomClick();

        void onResumePauseClick();
    }

    public static final class Mode {
        public static final int NORMAL = 1;
        public static final int VOICE_RECORD = 2;
        public static final int EDITING = 3;
        public static final int DISABLED = 4;
    }
}
