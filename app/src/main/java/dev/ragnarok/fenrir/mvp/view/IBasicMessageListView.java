package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IBasicMessageListView extends IMvpView, IAttachmentsPlacesView, IAccountDependencyView, IToastView {
    void notifyMessagesUpAdded(int position, int count);

    void notifyDataChanged();

    void notifyItemChanged(int index);

    void notifyMessagesDownAdded(int count);

    void configNowVoiceMessagePlaying(int id, float progress, boolean paused, boolean amin, boolean speed);

    void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin, boolean speed);

    void disableVoicePlaying();

    void showActionMode(String title, Boolean canEdit, Boolean canPin, Boolean canStar, Boolean doStar, Boolean canSpam);

    void finishActionMode();

    void displayMessages(@NonNull List<Message> mData, @NonNull LastReadId lastReadId);
}
