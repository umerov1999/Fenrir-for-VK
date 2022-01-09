package dev.ragnarok.fenrir.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.media.gif.IGifPlayer;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;

public interface IStoryPagerView extends IMvpView, IErrorView, IToastView, IAccountDependencyView {

    void displayData(int pageCount, int selectedIndex);

    void setAspectRatioAt(int position, int w, int h);

    void setPreparingProgressVisible(int position, boolean preparing);

    void attachDisplayToPlayer(int adapterPosition, IGifPlayer gifPlayer);

    void setToolbarTitle(@StringRes int titleRes, Object... params);

    void setToolbarSubtitle(@NonNull Story story, int account_id);

    void onShare(@NonNull Story story, int account_id);

    void configHolder(int adapterPosition, boolean progress, int aspectRatioW, int aspectRatioH);

    void onNext();

    void requestWriteExternalStoragePermission();
}
