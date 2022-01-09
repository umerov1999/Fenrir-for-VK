package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.Injection.provideMainThreadScheduler;
import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Utils.findIndexById;
import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.domain.IDocsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.DocFilter;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.LocalPhoto;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.model.menu.options.DocsOption;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IDocListView;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceUtil;
import dev.ragnarok.fenrir.upload.IUploadManager;
import dev.ragnarok.fenrir.upload.Upload;
import dev.ragnarok.fenrir.upload.UploadDestination;
import dev.ragnarok.fenrir.upload.UploadIntent;
import dev.ragnarok.fenrir.upload.UploadResult;
import dev.ragnarok.fenrir.upload.UploadUtils;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DisposableHolder;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;


public class DocsListPresenter extends AccountDependencyPresenter<IDocListView> {

    public static final String ACTION_SELECT = "dev.ragnarok.fenrir.select.docs";
    public static final String ACTION_SHOW = "dev.ragnarok.fenrir.show.docs";
    private static final String SAVE_FILTER = "save_filter";
    private final int mOwnerId;
    private final DisposableHolder<Integer> mLoader = new DisposableHolder<>();
    private final List<Document> mDocuments;
    private final String mAction;
    private final List<DocFilter> filters;
    private final IDocsInteractor docsInteractor;
    private final IUploadManager uploadManager;
    private final UploadDestination destination;
    private final List<Upload> uploadsData;
    private final DisposableHolder<Integer> requestHolder = new DisposableHolder<>();
    private boolean requestNow;
    private boolean cacheLoadingNow;

    public DocsListPresenter(int accountId, int ownerId, @Nullable String action, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        docsInteractor = InteractorFactory.createDocsInteractor();
        uploadManager = Injection.provideUploadManager();

        mOwnerId = ownerId;

        mDocuments = new ArrayList<>();
        uploadsData = new ArrayList<>(0);
        mAction = action;

        destination = UploadDestination.forDocuments(ownerId);

        appendDisposable(uploadManager.get(getAccountId(), destination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUploadsDataReceived));

        appendDisposable(uploadManager.observeAdding()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadsAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadDeleted));

        appendDisposable(uploadManager.observeResults()
                .filter(pair -> destination.compareTo(pair.getFirst().getDestination()))
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onProgressUpdates));

        int filter = isNull(savedInstanceState) ? DocFilter.Type.ALL : savedInstanceState.getInt(SAVE_FILTER);
        filters = createFilters(filter);

        loadAll();
        requestAll();
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_FILTER, getSelectedFilter());
    }

    private List<DocFilter> createFilters(int selectedType) {
        List<DocFilter> data = new ArrayList<>();
        data.add(new DocFilter(DocFilter.Type.ALL, R.string.doc_filter_all));
        data.add(new DocFilter(DocFilter.Type.TEXT, R.string.doc_filter_text));
        data.add(new DocFilter(DocFilter.Type.ARCHIVE, R.string.doc_filter_archive));
        data.add(new DocFilter(DocFilter.Type.GIF, R.string.doc_filter_gif));
        data.add(new DocFilter(DocFilter.Type.IMAGE, R.string.doc_filter_image));
        data.add(new DocFilter(DocFilter.Type.AUDIO, R.string.doc_filter_audio));
        data.add(new DocFilter(DocFilter.Type.VIDEO, R.string.doc_filter_video));
        data.add(new DocFilter(DocFilter.Type.BOOKS, R.string.doc_filter_books));
        data.add(new DocFilter(DocFilter.Type.OTHER, R.string.doc_filter_other));

        for (DocFilter filter : data) {
            filter.setActive(selectedType == filter.getType());
        }

        return data;
    }

    private void onUploadsDataReceived(List<Upload> data) {
        uploadsData.clear();
        uploadsData.addAll(data);

        callView(IDocListView::notifyDataSetChanged);
        resolveUploadDataVisibility();
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        mDocuments.add(0, (Document) pair.getSecond().getResult());
        callView(IDocListView::notifyDataSetChanged);
    }

    private void onProgressUpdates(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findIndexById(uploadsData, update.getId());
            if (index != -1) {
                callView(view -> view.notifyUploadProgressChanged(index, update.getProgress(), true));
            }
        }
    }

    public void fireMenuClick(Context context, int index, @NonNull Document doc) {
        ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
        menus.add(new OptionRequest(DocsOption.open_item_doc, context.getString(R.string.open), R.drawable.view, true));
        menus.add(new OptionRequest(DocsOption.share_item_doc, context.getString(R.string.share), R.drawable.share, true));
        menus.add(new OptionRequest(DocsOption.go_to_owner_doc, context.getString(R.string.goto_user), R.drawable.person, false));
        if (isMy()) {
            menus.add(new OptionRequest(DocsOption.delete_item_doc, context.getString(R.string.delete), R.drawable.ic_outline_delete, true));
        } else {
            menus.add(new OptionRequest(DocsOption.add_item_doc, context.getString(R.string.action_add), R.drawable.plus, true));
        }
        menus.header(doc.getTitle(), R.drawable.book, doc.getPreviewWithSize(PhotoSize.X, true));
        menus.columns(2);
        menus.show(((FragmentActivity) context).getSupportFragmentManager(), "docs_options", option -> {
            switch (option.getId()) {
                case DocsOption.open_item_doc:
                    fireDocClick(doc);
                    break;
                case DocsOption.share_item_doc:
                    share(context, doc);
                    break;
                case DocsOption.add_item_doc:
                    IDocsInteractor docsInteractor = InteractorFactory.createDocsInteractor();
                    String accessKey = doc.getAccessKey();
                    appendDisposable(docsInteractor.add(getAccountId(), doc.getId(), doc.getOwnerId(), accessKey)
                            .compose(RxUtils.applySingleIOToMainSchedulers())
                            .subscribe(id -> CustomToast.CreateCustomToast(context).setDuration(Toast.LENGTH_LONG).showToastSuccessBottom(R.string.added), t -> callView(v -> showError(v, getCauseIfRuntime(t)))));
                    break;
                case DocsOption.delete_item_doc:
                    new MaterialAlertDialogBuilder(context)
                            .setTitle(R.string.remove_confirm)
                            .setMessage(R.string.doc_remove_confirm_message)
                            .setPositiveButton(R.string.button_yes, (dialog, which) -> doRemove(doc, index))
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                    break;
                case DocsOption.go_to_owner_doc:
                    PlaceFactory.getOwnerWallPlace(getAccountId(), doc.getOwnerId(), null).tryOpenWith(context);
                    break;
            }
        });
    }

    private void doRemove(@NonNull Document doc, int index) {
        appendDisposable(docsInteractor.delete(getAccountId(), doc.getId(), doc.getOwnerId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> {
                    mDocuments.remove(index);
                    callView(v -> v.notifyDataRemoved(index));
                }, t -> {/*TODO*/}));
    }

    private void share(Context context, Document document) {
        String[] items = {
                getString(R.string.share_link),
                getString(R.string.repost_send_message),
                getString(R.string.repost_to_wall)
        };

        new MaterialAlertDialogBuilder(context)
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.shareLink((Activity) context, String.format("vk.com/doc%s_%s", document.getOwnerId(), document.getId()), document.getTitle());
                            break;
                        case 1:
                            SendAttachmentsActivity.startForSendAttachments(context, getAccountId(), document);
                            break;
                        case 2:
                            postToMyWall(context, document);
                            break;
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.share_document_title)
                .show();
    }

    private void postToMyWall(Context context, Document document) {
        List<AbsModel> models = Collections.singletonList(document);
        PlaceUtil.goToPostCreation((Activity) context, getAccountId(), getAccountId(), EditingPostType.TEMP, models);
    }

    private boolean isMy() {
        return getAccountId() == mOwnerId;
    }

    private void onUploadStatusUpdate(Upload upload) {
        int index = findIndexById(uploadsData, upload.getId());
        if (index != -1) {
            callView(view -> view.notifyUploadItemChanged(index));
        }
    }

    private void onUploadsAdded(List<Upload> added) {
        for (Upload u : added) {
            if (destination.compareTo(u.getDestination())) {
                int index = uploadsData.size();
                uploadsData.add(u);
                callView(view -> view.notifyUploadItemsAdded(index, 1));
            }
        }

        resolveUploadDataVisibility();
    }

    private void onUploadDeleted(int[] ids) {
        for (int id : ids) {
            int index = findIndexById(uploadsData, id);
            if (index != -1) {
                uploadsData.remove(index);
                callView(view -> view.notifyUploadItemRemoved(index));
            }
        }

        resolveUploadDataVisibility();
    }

    private void resolveUploadDataVisibility() {
        callView(v -> v.setUploadDataVisible(!uploadsData.isEmpty()));
    }

    private void setCacheLoadingNow(boolean cacheLoadingNow) {
        this.cacheLoadingNow = cacheLoadingNow;
        resolveRefreshingView();
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private int getSelectedFilter() {
        for (DocFilter filter : filters) {
            if (filter.isActive()) {
                return filter.getType();
            }
        }

        return DocFilter.Type.ALL;
    }

    private void requestAll() {
        setRequestNow(true);

        int filter = getSelectedFilter();
        int accountId = getAccountId();

        requestHolder.append(docsInteractor.request(accountId, mOwnerId, filter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onNetDataReceived, throwable -> onRequestError(getCauseIfRuntime(throwable))));
    }

    private void onRequestError(Throwable throwable) {
        setRequestNow(false);
        callView(v -> showError(v, throwable));
    }

    private void onCacheDataReceived(List<Document> data) {
        setCacheLoadingNow(false);

        mDocuments.clear();
        mDocuments.addAll(data);

        safelyNotifyDataSetChanged();
    }

    private void onNetDataReceived(List<Document> data) {
        // cancel db loading if active
        mLoader.dispose();

        cacheLoadingNow = false;
        requestNow = false;

        resolveRefreshingView();

        mDocuments.clear();
        mDocuments.addAll(data);

        safelyNotifyDataSetChanged();
    }

    @Override
    public void onGuiCreated(@NonNull IDocListView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayUploads(uploadsData);
        viewHost.displayFilterData(filters);

        resolveUploadDataVisibility();
        resolveRefreshingView();
        resolveDocsListData();
    }

    private void loadAll() {
        setCacheLoadingNow(true);

        int accountId = getAccountId();
        int filter = getSelectedFilter();

        mLoader.append(docsInteractor.getCacheData(accountId, mOwnerId, filter)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCacheDataReceived, throwable -> onLoadError(getCauseIfRuntime(throwable))));
    }

    private void resolveRefreshingView() {
        callView(v -> v.showRefreshing(isNowLoading()));
    }

    private boolean isNowLoading() {
        return cacheLoadingNow || requestNow;
    }

    private void safelyNotifyDataSetChanged() {
        resolveDocsListData();
    }

    private void resolveDocsListData() {
        callView(v -> v.displayData(mDocuments, isImagesOnly()));
    }

    private boolean isImagesOnly() {
        return Utils.intValueIn(getSelectedFilter(), DocFilter.Type.IMAGE, DocFilter.Type.GIF);
    }

    private void onLoadError(Throwable throwable) {
        throwable.printStackTrace();
        setCacheLoadingNow(false);

        callView(v -> showError(v, throwable));

        resolveRefreshingView();
    }

    @Override
    public void onDestroyed() {
        mLoader.dispose();
        requestHolder.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {
        mLoader.dispose();
        cacheLoadingNow = false;

        requestAll();
    }

    public void fireButtonAddClick() {
        if (AppPerms.hasReadStoragePermission(getApplicationContext())) {
            callView(v -> v.startSelectUploadFileActivity(getAccountId()));
        } else {
            callView(IDocListView::requestReadExternalStoragePermission);
        }
    }

    public void fireDocClick(@NonNull Document doc) {
        if (ACTION_SELECT.equals(mAction)) {
            ArrayList<Document> selected = new ArrayList<>(1);
            selected.add(doc);

            callView(v -> v.returnSelection(selected));
        } else {
            if (doc.isGif() && doc.hasValidGifVideoLink()) {
                ArrayList<Document> gifs = new ArrayList<>();
                int selectedIndex = 0;
                for (int i = 0; i < mDocuments.size(); i++) {
                    Document d = mDocuments.get(i);

                    if (d.isGif() && d.hasValidGifVideoLink()) {
                        gifs.add(d);
                    }

                    if (d == doc) {
                        selectedIndex = gifs.size() - 1;
                    }
                }

                int finalSelectedIndex = selectedIndex;
                callView(v -> v.goToGifPlayer(getAccountId(), gifs, finalSelectedIndex));
            } else {
                callView(v -> v.openDocument(getAccountId(), doc));
            }
        }
    }

    public void fireReadPermissionResolved() {
        if (AppPerms.hasReadStoragePermission(getApplicationContext())) {
            callView(v -> v.startSelectUploadFileActivity(getAccountId()));
        }
    }

    public void fireFileForUploadSelected(String file) {
        UploadIntent intent = new UploadIntent(getAccountId(), destination)
                .setAutoCommit(true)
                .setFileUri(Uri.parse(file));

        uploadManager.enqueue(Collections.singletonList(intent));
    }

    public void fireRemoveClick(Upload upload) {
        uploadManager.cancel(upload.getId());
    }

    public void fireFilterClick(DocFilter entry) {
        for (DocFilter filter : filters) {
            filter.setActive(entry.getType() == filter.getType());
        }

        callView(IDocListView::notifyFiltersChanged);

        loadAll();
        requestAll();
    }

    public void pleaseNotifyViewAboutAdapterType() {
        callView(v -> v.setAdapterType(isImagesOnly()));
    }

    public void fireLocalPhotosForUploadSelected(ArrayList<LocalPhoto> photos) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, Upload.IMAGE_SIZE_FULL, true);
        uploadManager.enqueue(intents);
    }
}
