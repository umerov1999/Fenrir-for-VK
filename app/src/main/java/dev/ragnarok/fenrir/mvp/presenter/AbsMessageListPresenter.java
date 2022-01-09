package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.countOfSelection;
import static dev.ragnarok.fenrir.util.Utils.getSelected;
import static dev.ragnarok.fenrir.util.Utils.safeIsEmpty;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.media.voice.IVoicePlayer;
import dev.ragnarok.fenrir.media.voice.PrepareException;
import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.mvp.presenter.base.PlaceSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.IBasicMessageListView;
import dev.ragnarok.fenrir.util.Lookup;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.Utils;

public abstract class AbsMessageListPresenter<V extends IBasicMessageListView> extends
        PlaceSupportPresenter<V> implements IVoicePlayer.IPlayerStatusListener {

    protected final LastReadId lastReadId = new LastReadId(0, 0);
    private final ArrayList<Message> mData;
    private IVoicePlayer mVoicePlayer;
    private Lookup mVoiceMessageLookup;

    AbsMessageListPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        mData = new ArrayList<>();

        mVoicePlayer = Injection.provideVoicePlayerFactory().createPlayer();
        mVoicePlayer.setCallback(this);
        mVoiceMessageLookup = new Lookup(500);
        mVoiceMessageLookup.setCallback(() -> resolveVoiceMessagePlayingState(true));
    }

    private void syncVoiceLookupState() {
        boolean needLookup = mVoicePlayer.isSupposedToPlay() && getGuiIsReady();

        if (needLookup) {
            mVoiceMessageLookup.start();
        } else {
            mVoiceMessageLookup.stop();
        }
    }

    public void resolveListView() {
        callView(v -> v.displayMessages(mData, lastReadId));
    }

    @NonNull
    public ArrayList<Message> getData() {
        return mData;
    }

    protected int indexOf(int messageId) {
        return Utils.indexOf(mData, messageId);
    }

    @Nullable
    protected Message findById(int messageId) {
        return Utils.findById(mData, messageId);
    }

    protected boolean clearSelection() {
        boolean hasChanges = false;
        for (Message message : mData) {
            if (message.isSelected()) {
                message.setSelected(false);
                hasChanges = true;
            }
        }

        return hasChanges;
    }

    protected void resolveActionMode() {
        int selectionCount = countOfSelection(getData());
        if (selectionCount > 0) {
            callView(v -> v.showActionMode(String.valueOf(selectionCount), false, false, false, false, true));
        } else {
            callView(IBasicMessageListView::finishActionMode);
        }
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);

        syncVoiceLookupState();
        resolveListView();
        resolveActionMode();
    }

    protected void safeNotifyDataChanged() {
        callView(IBasicMessageListView::notifyDataChanged);
    }

    protected void safeNotifyItemChanged(int index) {
        callView(v -> v.notifyItemChanged(index));
    }

    public void fireMessageLongClick(@NonNull Message message, int position) {
        message.setSelected(!message.isSelected());
        resolveActionMode();
        safeNotifyItemChanged(position);
    }

    public void fireMessageClick(@NonNull Message message, int position) {
        int actionModeActive = countOfSelection(getData());

        if (actionModeActive > 0) {
            message.setSelected(!message.isSelected());
            resolveActionMode();
            safeNotifyItemChanged(position);
        } else {
            onMessageClick(message);
        }
    }

    protected void onMessageClick(@NonNull Message message) {

    }

    public final void fireActionModeDestroy() {
        onActionModeDestroy();
    }

    @CallSuper
    protected void onActionModeDestroy() {
        if (clearSelection()) {
            safeNotifyDataChanged();
        }
    }

    public final void fireActionModeDeleteClick() {
        onActionModeDeleteClick();
    }

    protected void onActionModeDeleteClick() {

    }

    public final void fireActionModeSpamClick() {
        onActionModeSpamClick();
    }

    protected void onActionModeSpamClick() {

    }

    public final void fireActionModeCopyClick() {
        onActionModeCopyClick();
    }

    protected void onActionModeCopyClick() {
        List<Message> selected = getSelected(getData(), true);
        if (safeIsEmpty(selected)) return;

        StringBuilder result = new StringBuilder();
        boolean firstTime = true;
        for (Message message : selected) {
            String body = TextUtils.isEmpty(message.getDecryptedBody()) ? message.getBody() : message.getDecryptedBody();
            result.append(!firstTime ? "\n" : "");
            result.append(body);
            if (message.isVoiceMessage()) {
                if (!TextUtils.isEmpty(body)) {
                    result.append("\n\n");
                }
                boolean firstTimeVoice = true;
                for (VoiceMessage vs : message.getAttachments().getVoiceMessages()) {
                    if (!TextUtils.isEmpty(vs.getTranscript())) {
                        result.append(!firstTimeVoice ? "\n" : "");
                        result.append(vs.getTranscript());
                        firstTimeVoice = false;
                    }
                }
            }
            firstTime = false;
        }

        ClipboardManager clipboard = (ClipboardManager) getApplicationContext()
                .getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("messages", result);
        clipboard.setPrimaryClip(clip);
        callView(v -> v.getCustomToast().setDuration(Toast.LENGTH_LONG).showToast(R.string.copied_to_clipboard));
    }

    public final void fireForwardClick() {
        onActionModeForwardClick();
    }

    protected void onActionModeForwardClick() {

    }

    public void fireVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage) {
        try {
            boolean messageChanged = mVoicePlayer.toggle(voiceMessageId, voiceMessage);
            if (messageChanged) {
                resolveVoiceMessagePlayingState();
            } else {
                boolean paused = !mVoicePlayer.isSupposedToPlay();
                float progress = mVoicePlayer.getProgress();
                boolean isSpeed = mVoicePlayer.isPlaybackSpeed();
                callView(v -> v.bindVoiceHolderById(voiceHolderId, true, paused, progress, false, isSpeed));
            }
        } catch (PrepareException ignored) {

        }

        syncVoiceLookupState();
    }

    public void fireVoicePlaybackSpeed() {
        mVoicePlayer.togglePlaybackSpeed();
    }

    private void resolveVoiceMessagePlayingState(boolean anim) {

        Optional<Integer> optionalVoiceMessageId = mVoicePlayer.getPlayingVoiceId();

        if (optionalVoiceMessageId.isEmpty()) {
            callView(IBasicMessageListView::disableVoicePlaying);
        } else {
            float progress = mVoicePlayer.getProgress();
            boolean paused = !mVoicePlayer.isSupposedToPlay();
            boolean isSpeed = mVoicePlayer.isPlaybackSpeed();
            callView(v -> v.configNowVoiceMessagePlaying(optionalVoiceMessageId.get(), progress, paused, anim, isSpeed));
        }
    }

    private void resolveVoiceMessagePlayingState() {
        resolveVoiceMessagePlayingState(false);
    }

    @Override
    public void onGuiDestroyed() {
        syncVoiceLookupState();
        super.onGuiDestroyed();
    }

    public void fireVoiceHolderCreated(int voiceMessageId, int voiceHolderId) {
        Optional<Integer> currentVoiceId = mVoicePlayer.getPlayingVoiceId();
        boolean play = currentVoiceId.nonEmpty() && currentVoiceId.get() == voiceMessageId;
        boolean paused = play && !mVoicePlayer.isSupposedToPlay();
        boolean isSpeed = mVoicePlayer.isPlaybackSpeed();

        callView(v -> v.bindVoiceHolderById(voiceHolderId, play, paused, mVoicePlayer.getProgress(), false, isSpeed));
    }

    @Override
    public void onPlayerStatusChange(int status) {
        //Optional<Integer> voiceMessageId = mVoicePlayer.getPlayingVoiceId();
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();
        mVoicePlayer.setCallback(null);
        mVoicePlayer.release();
        mVoicePlayer = null;

        mVoiceMessageLookup.stop();
        mVoiceMessageLookup.setCallback(null);
        mVoiceMessageLookup = null;
    }
}
