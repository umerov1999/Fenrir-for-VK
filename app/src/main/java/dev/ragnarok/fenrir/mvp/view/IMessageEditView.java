package dev.ragnarok.fenrir.mvp.view;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IMessageEditView extends IMvpView, IErrorView {

    void displayAttachments(List<AttachmentEntry> entries);

    void notifyDataAdded(int positionStart, int count);

    void addPhoto(int accountId, int ownerId);

    void notifyEntryRemoved(int index);

    void displaySelectUploadPhotoSizeDialog(List<LocalPhoto> photos);

    void changePercentageSmoothly(int dataPosition, int progress);

    void notifyItemChanged(int index);

    void setEmptyViewVisible(boolean visible);

    void requestCameraPermission();

    void startCamera(@NonNull Uri fileUri);

    void startAddDocumentActivity(int accountId);

    void startAddVideoActivity(int accountId, int ownerId);
}