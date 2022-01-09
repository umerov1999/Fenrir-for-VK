package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.model.DocFilter;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.upload.Upload;


public interface IDocListView extends IAccountDependencyView, IMvpView, IErrorView {

    void displayData(List<Document> documents, boolean asImages);

    void showRefreshing(boolean refreshing);

    void notifyDataSetChanged();

    void notifyDataAdd(int position, int count);

    void notifyDataRemoved(int position);

    void openDocument(int accountId, @NonNull Document document);

    void returnSelection(ArrayList<Document> docs);

    void goToGifPlayer(int accountId, @NonNull ArrayList<Document> gifs, int selected);

    void requestReadExternalStoragePermission();

    void startSelectUploadFileActivity(int accountId);

    void setUploadDataVisible(boolean visible);

    void displayUploads(List<Upload> data);

    void notifyUploadDataChanged();

    void notifyUploadItemsAdded(int position, int count);

    void notifyUploadItemChanged(int position);

    void notifyUploadItemRemoved(int position);

    void notifyUploadProgressChanged(int position, int progress, boolean smoothly);

    void displayFilterData(List<DocFilter> filters);

    void notifyFiltersChanged();

    void setAdapterType(boolean imagesOnly);
}
