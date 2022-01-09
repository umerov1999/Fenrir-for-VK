package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface ILocalPhotoAlbumsView extends IMvpView {

    void displayData(@NonNull List<LocalImageAlbum> data);

    void setEmptyTextVisible(boolean visible);

    void displayProgress(boolean loading);

    void openAlbum(@NonNull LocalImageAlbum album);

    void notifyDataChanged();

    void requestReadExternalStoragePermission();
}
