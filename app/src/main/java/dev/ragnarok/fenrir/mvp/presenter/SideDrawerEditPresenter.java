package dev.ragnarok.fenrir.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.SideDrawerCategory;
import dev.ragnarok.fenrir.model.SideSwitchableCategory;
import dev.ragnarok.fenrir.mvp.core.AbsPresenter;
import dev.ragnarok.fenrir.mvp.view.ISideDrawerEditView;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;


public class SideDrawerEditPresenter extends AbsPresenter<ISideDrawerEditView> {

    private final List<SideDrawerCategory> data;

    public SideDrawerEditPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        data = createInitialData();
    }

    @StringRes
    private static int getTitleResCategory(@SideSwitchableCategory int type) {
        switch (type) {
            case SideSwitchableCategory.FRIENDS:
                return R.string.friends;
            case SideSwitchableCategory.NEWSFEED_COMMENTS:
                return R.string.drawer_newsfeed_comments;
            case SideSwitchableCategory.GROUPS:
                return R.string.groups;
            case SideSwitchableCategory.PHOTOS:
                return R.string.photos;
            case SideSwitchableCategory.VIDEOS:
                return R.string.videos;
            case SideSwitchableCategory.MUSIC:
                return R.string.music;
            case SideSwitchableCategory.DOCS:
                return R.string.documents;
            case SideSwitchableCategory.BOOKMARKS:
                return R.string.bookmarks;
            case SideSwitchableCategory.DIALOGS:
                return R.string.dialogs;
            case SideSwitchableCategory.FEED:
                return R.string.feed;
            case SideSwitchableCategory.FEEDBACK:
                return R.string.drawer_feedback;
            case SideSwitchableCategory.SEARCH:
                return R.string.search;
        }

        throw new IllegalArgumentException();
    }

    private ArrayList<SideDrawerCategory> createInitialData() {
        ArrayList<SideDrawerCategory> categories = new ArrayList<>();

        ISettings.ISideDrawerSettings settings = Settings.get().sideDrawerSettings();

        @SideSwitchableCategory
        int[] items = settings.getCategoriesOrder();

        for (int category : items) {
            SideDrawerCategory c = new SideDrawerCategory(category, getTitleResCategory(category));
            c.setChecked(settings.isCategoryEnabled(category));
            categories.add(c);
        }

        return categories;
    }

    @Override
    public void onGuiCreated(@NonNull ISideDrawerEditView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(data);
    }

    private void save() {
        @SideSwitchableCategory
        int[] postions = new int[data.size()];
        boolean[] active = new boolean[data.size()];

        for (int i = 0; i < data.size(); i++) {
            SideDrawerCategory category = data.get(i);

            postions[i] = category.getKey();
            active[i] = category.isChecked();
        }

        Settings.get().sideDrawerSettings().setCategoriesOrder(postions, active);
    }

    public void fireSaveClick() {
        save();
        callView(ISideDrawerEditView::goBackAndApplyChanges);
    }

    public void fireItemMoved(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(data, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(data, i, i - 1);
            }
        }
    }
}
