package dev.ragnarok.fenrir.fragment.conversation;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.fave.FavePhotosAdapter;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.TmpSource;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.conversations.ChatAttachmentPhotoPresenter;
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentPhotosView;
import dev.ragnarok.fenrir.place.PlaceFactory;

public class ConversationPhotosFragment extends AbsChatAttachmentsFragment<Photo, ChatAttachmentPhotoPresenter,
        IChatAttachmentPhotosView> implements FavePhotosAdapter.PhotoSelectionListener, FavePhotosAdapter.PhotoConversationListener, IChatAttachmentPhotosView {

    private final ActivityResultLauncher<Intent> requestPhotoUpdate = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    int ps = result.getData().getExtras().getInt(Extra.POSITION);
                    ((FavePhotosAdapter) getAdapter()).updateCurrentPosition(ps);
                    mRecyclerView.scrollToPosition(ps);
                }
            });

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = getResources().getInteger(R.integer.photos_column_count);
        return new GridLayoutManager(requireActivity(), columns);
    }

    @Override
    public RecyclerView.Adapter<?> createAdapter() {
        FavePhotosAdapter apiPhotoFavePhotosAdapter = new FavePhotosAdapter(requireActivity(), Collections.emptyList());
        apiPhotoFavePhotosAdapter.setPhotoSelectionListener(this);
        apiPhotoFavePhotosAdapter.setPhotoConversationListener(this);
        return apiPhotoFavePhotosAdapter;
    }

    @Override
    public void onPhotoClicked(int position, Photo photo) {
        callPresenter(p -> p.firePhotoClick(position, photo));
    }

    @NonNull
    @Override
    public IPresenterFactory<ChatAttachmentPhotoPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = requireArguments().getInt(Extra.PEER_ID);
            return new ChatAttachmentPhotoPresenter(peerId, accountId, saveInstanceState);
        };
    }

    @Override
    public void displayAttachments(List<Photo> data) {
        FavePhotosAdapter adapter = (FavePhotosAdapter) getAdapter();
        adapter.setData(data);
    }

    @Override
    public void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index) {
        PlaceFactory.getTmpSourceGalleryPlace(accountId, source, index).setActivityResultLauncher(requestPhotoUpdate).tryOpenWith(requireActivity());
    }

    @Override
    public void goToTempPhotosGallery(int accountId, long ptr, int index) {
        PlaceFactory.getTmpSourceGalleryPlace(accountId, ptr, index).setActivityResultLauncher(requestPhotoUpdate).tryOpenWith(requireActivity());
    }

    @Override
    public void onGoPhotoConversation(@NonNull Photo photo) {
        callPresenter(p -> p.fireGoToMessagesLookup(photo.getMsgPeerId(), photo.getMsgId()));
    }
}