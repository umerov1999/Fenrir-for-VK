package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.Manifest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.domain.IDocsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.base.BaseFragment;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.PhotoSize;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.place.PlaceUtil;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.CircleCounterButton;
import dev.ragnarok.fenrir.view.TouchImageView;

public class DocPreviewFragment extends BaseFragment implements View.OnClickListener {

    private static final String SAVE_DELETED = "deleted";
    private int accountId;
    private View rootView;
    private int ownerId;
    private int documentId;
    private String documentAccessKey;
    private Document document;
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            this::doDownloadDoc);
    private TouchImageView preview;
    private ImageView ivDocIcon;
    private TextView tvTitle;
    private TextView tvSubtitle;
    private boolean mLoadingNow;
    private boolean deleted;
    private IDocsInteractor docsInteractor;

    public static Bundle buildArgs(int accountId, int docId, int docOwnerId, @Nullable String accessKey, @Nullable Document document) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.DOC_ID, docId);
        args.putInt(Extra.OWNER_ID, docOwnerId);

        if (!Utils.isEmpty(accessKey)) {
            args.putString(Extra.ACCESS_KEY, accessKey);
        }

        if (document != null) {
            args.putParcelable(Extra.DOC, document);
        }

        return args;
    }

    public static DocPreviewFragment newInstance(Bundle arsg) {
        DocPreviewFragment fragment = new DocPreviewFragment();
        fragment.setArguments(arsg);
        return fragment;
    }

    private void doDownloadDoc() {
        if (DownloadWorkUtils.doDownloadDoc(requireActivity(), document, false) == 1) {
            Utils.ThemedSnack(requireView(), R.string.audio_force_download, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                    v1 -> DownloadWorkUtils.doDownloadDoc(requireActivity(), document, true)).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        docsInteractor = InteractorFactory.createDocsInteractor();

        if (savedInstanceState != null) {
            restoreFromInstanceState(savedInstanceState);
        }

        ownerId = requireArguments().getInt(Extra.OWNER_ID);
        documentId = requireArguments().getInt(Extra.DOC_ID);

        if (requireArguments().containsKey(Extra.DOC)) {
            document = requireArguments().getParcelable(Extra.DOC);
        }

        if (requireArguments().containsKey(Extra.ACCESS_KEY)) {
            documentAccessKey = requireArguments().getString(Extra.ACCESS_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_document_preview, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(rootView.findViewById(R.id.toolbar));
        preview = rootView.findViewById(R.id.fragment_document_preview);
        ivDocIcon = rootView.findViewById(R.id.no_preview_icon);

        tvTitle = rootView.findViewById(R.id.fragment_document_title);
        tvSubtitle = rootView.findViewById(R.id.fragment_document_subtitle);

        CircleCounterButton deleteOrAddButton = rootView.findViewById(R.id.add_or_delete_button);

        deleteOrAddButton.setOnClickListener(this);
        rootView.findViewById(R.id.download_button).setOnClickListener(this);
        rootView.findViewById(R.id.share_button).setOnClickListener(this);

        deleteOrAddButton.setIcon(isMy() ? R.drawable.ic_outline_delete : R.drawable.plus);

        return rootView;
    }

    private boolean isMy() {
        return accountId == ownerId;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Objects.isNull(document) && !mLoadingNow) {
            requestVideoInfo();
        }

        resolveAllViews();
    }

    private void resolveAllViews() {
        if (!isAdded()) return;

        if (document == null) {
            rootView.findViewById(R.id.content_root).setVisibility(View.GONE);
            rootView.findViewById(R.id.loading_root).setVisibility(View.VISIBLE);

            rootView.findViewById(R.id.progressBar).setVisibility(mLoadingNow ? View.VISIBLE : View.GONE);
            rootView.findViewById(R.id.post_loading_text).setVisibility(mLoadingNow ? View.VISIBLE : View.GONE);
            rootView.findViewById(R.id.try_again_button).setVisibility(mLoadingNow ? View.GONE : View.VISIBLE);

            return;
        }

        rootView.findViewById(R.id.content_root).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.loading_root).setVisibility(View.GONE);

        if (nonNull(document.getGraffiti())) {
            ivDocIcon.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);

            String graffitiUrl = document.getGraffiti().getSrc();

            if (nonEmpty(graffitiUrl)) {
                PicassoInstance.with()
                        .load(graffitiUrl)
                        .into(preview);
            }
        } else if (document.getType() == 4 && nonNull(document.getUrl())) {
            ivDocIcon.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);

            String previewUrl = document.getUrl();

            if (!TextUtils.isEmpty(previewUrl)) {
                PicassoInstance.with()
                        .load(previewUrl)
                        .into(preview);
            }
        } else if (nonNull(document.getPhotoPreview())) {
            ivDocIcon.setVisibility(View.GONE);
            preview.setVisibility(View.VISIBLE);

            String previewUrl = document.getPhotoPreview().getUrlForSize(PhotoSize.X, true);

            if (!TextUtils.isEmpty(previewUrl)) {
                PicassoInstance.with()
                        .load(previewUrl)
                        .into(preview);
            }
        } else {
            preview.setVisibility(View.GONE);
            ivDocIcon.setVisibility(View.VISIBLE);
        }

        tvTitle.setText(document.getTitle());
        tvSubtitle.setText(AppTextUtils.getSizeString(document.getSize()));

        resolveButtons();
    }

    private void resolveButtons() {
        if (!isAdded()) {
            return;
        }

        rootView.findViewById(R.id.add_or_delete_button).setVisibility(deleted ? View.INVISIBLE : View.VISIBLE);
        rootView.findViewById(R.id.share_button).setVisibility(deleted ? View.INVISIBLE : View.VISIBLE);
    }

    private void requestVideoInfo() {
        mLoadingNow = true;
        appendDisposable(docsInteractor.findById(accountId, ownerId, documentId, documentAccessKey)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDocumentInfoReceived, this::onDocumentInfoGetError));
    }

    private void onDocumentInfoGetError(Throwable t) {
        mLoadingNow = false;
        // TODO: 06.10.2017
    }

    private void onDocumentInfoReceived(Document document) {
        mLoadingNow = false;
        this.document = document;

        requireArguments().putParcelable(Extra.DOC, document);

        resolveAllViews();
        resolveActionBar();
    }

    @Override
    public void onResume() {
        super.onResume();
        resolveActionBar();

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    private void resolveActionBar() {
        if (!isAdded()) return;

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.attachment_document);
            actionBar.setSubtitle(document == null ? null : document.getTitle());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVE_DELETED, deleted);
    }

    private void restoreFromInstanceState(Bundle state) {
        deleted = state.getBoolean(SAVE_DELETED);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.add_or_delete_button) {
            if (isMy()) {
                remove();
            } else {
                addYourSelf();
            }
        } else if (v.getId() == R.id.share_button) {
            share();
        } else if (v.getId() == R.id.download_button) {
            download();
        }
    }

    private void doRemove() {
        appendDisposable(docsInteractor.delete(accountId, documentId, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onDeleteSuccess, t -> {/*TODO*/}));
    }

    private void onDeleteSuccess() {
        if (nonNull(rootView)) {
            Snackbar.make(rootView, R.string.deleted, BaseTransientBottomBar.LENGTH_LONG).show();
        }

        deleted = true;
        resolveButtons();
    }

    private void remove() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.remove_confirm)
                .setMessage(R.string.doc_remove_confirm_message)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> doRemove())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void share() {
        String[] items = {
                getString(R.string.share_link),
                getString(R.string.repost_send_message),
                getString(R.string.repost_to_wall)
        };

        new MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.shareLink(requireActivity(), genLink(), document.getTitle());
                            break;
                        case 1:
                            SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, document);
                            break;
                        case 2:
                            postToMyWall();
                            break;
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.share_document_title)
                .show();
    }

    private void postToMyWall() {
        List<AbsModel> models = Collections.singletonList(document);
        PlaceUtil.goToPostCreation(requireActivity(), accountId, accountId, EditingPostType.TEMP, models);
    }

    private String genLink() {
        return String.format("vk.com/doc%s_%s", ownerId, documentId);
    }

    private void download() {
        if (!AppPerms.hasReadWriteStoragePermission(requireActivity())) {
            requestWritePermission.launch();
            return;
        }

        doDownloadDoc();
    }

    private void openOwnerWall() {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(requireActivity());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.string.goto_user).setOnMenuItemClickListener(item -> {
            openOwnerWall();
            return true;
        });
    }

    private void doAddYourSelf() {
        IDocsInteractor docsInteractor = InteractorFactory.createDocsInteractor();

        String accessKey = nonNull(document) ? document.getAccessKey() : null;

        appendDisposable(docsInteractor.add(accountId, documentId, ownerId, accessKey)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(id -> onDocumentAdded(), this::onDocAddError));
    }

    private void onDocAddError(Throwable t) {
        t.printStackTrace();
    }

    private void onDocumentAdded() {
        if (nonNull(rootView)) {
            Snackbar.make(rootView, R.string.added, BaseTransientBottomBar.LENGTH_LONG).show();
        }

        deleted = false;
        resolveButtons();
    }

    private void addYourSelf() {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.add_document_to_yourself_commit)
                .setPositiveButton(R.string.button_yes, (dialog, which) -> doAddYourSelf())
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }
}
