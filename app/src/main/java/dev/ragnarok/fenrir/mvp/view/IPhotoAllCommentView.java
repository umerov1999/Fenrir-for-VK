package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface IPhotoAllCommentView extends IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    void displayData(List<Comment> comments);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void dismissDeepLookingCommentProgress();

    void displayDeepLookingCommentProgress();

    void moveFocusTo(int index, boolean smooth);

    void notifyDataAddedToTop(int count);

    void notifyItemChanged(int index);
}
