package dev.ragnarok.fenrir.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IPhotosInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.fragment.search.criteria.PhotoSearchCriteria;
import dev.ragnarok.fenrir.fragment.search.nextfrom.IntNextFrom;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.mvp.view.search.IPhotoSearchView;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class PhotoSearchPresenter extends AbsSearchPresenter<IPhotoSearchView, PhotoSearchCriteria, Photo, IntNextFrom> {

    private final IPhotosInteractor photoInteractor;

    public PhotoSearchPresenter(int accountId, @Nullable PhotoSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        photoInteractor = InteractorFactory.createPhotosInteractor();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Photo>, IntNextFrom>> doSearch(int accountId, PhotoSearchCriteria criteria, IntNextFrom startFrom) {
        int offset = startFrom.getOffset();
        IntNextFrom nextFrom = new IntNextFrom(50 + offset);
        return photoInteractor.search(accountId, criteria, offset, 50)
                .map(photos -> Pair.Companion.create(photos, nextFrom));
    }

    @Override
    PhotoSearchCriteria instantiateEmptyCriteria() {
        return new PhotoSearchCriteria("");
    }

    @Override
    boolean canSearch(PhotoSearchCriteria criteria) {
        return Utils.nonEmpty(criteria.getQuery());
    }

    public void firePhotoClick(Photo wrapper) {
        int Index = 0;
        boolean trig = false;
        ArrayList<Photo> photos_ret = new ArrayList<>(data.size());
        for (int i = 0; i < data.size(); i++) {
            Photo photo = data.get(i);
            photos_ret.add(photo);
            if (!trig && photo.getId() == wrapper.getId() && photo.getOwnerId() == wrapper.getOwnerId()) {
                Index = i;
                trig = true;
            }
        }
        int finalIndex = Index;
        callView(v -> v.displayGallery(getAccountId(), photos_ret, finalIndex));
    }
}
