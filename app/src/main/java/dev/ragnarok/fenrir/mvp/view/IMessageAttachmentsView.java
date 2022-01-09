package dev.ragnarok.fenrir.mvp.view;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.AttachmentEntry;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.ModelsBundle;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface IMessageAttachmentsView extends IMvpView, IErrorView {

    void displayAttachments(List<AttachmentEntry> entries);

    void notifyDataAdded(int positionStart, int count);

    void addPhoto(int accountId, int ownerId);

    void notifyEntryRemoved(int index);

    void displaySelectUploadPhotoSizeDialog(List<LocalPhoto> photos);

    void displayCropPhotoDialog(Uri uri);

    void displaySelectUploadFileSizeDialog(String file);

    void changePercentageSmoothly(int id, int progress);

    void notifyItemChanged(int index);

    void setEmptyViewVisible(boolean visible);

    void requestCameraPermission();

    void startCamera(@NonNull Uri fileUri);

    void syncAccompanyingWithParent(ModelsBundle accompanying);

    void startAddDocumentActivity(int accountId);

    void startAddVideoActivity(int accountId, int ownerId);

    void startAddAudioActivity(int accountId);
}