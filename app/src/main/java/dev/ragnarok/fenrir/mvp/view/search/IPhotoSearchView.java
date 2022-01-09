package dev.ragnarok.fenrir.mvp.view.search;

import java.util.ArrayList;

import dev.ragnarok.fenrir.model.Photo;

public interface IPhotoSearchView extends IBaseSearchView<Photo> {
    void displayGallery(int accountId, ArrayList<Photo> photos, int position);
}
