package dev.ragnarok.fenrir.settings;

import static dev.ragnarok.fenrir.model.SideSwitchableCategory.BOOKMARKS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.DIALOGS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.DOCS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.FEED;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.FEEDBACK;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.FRIENDS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.GROUPS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.MUSIC;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.NEWSFEED_COMMENTS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.PHOTOS;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.SEARCH;
import static dev.ragnarok.fenrir.model.SideSwitchableCategory.VIDEOS;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import dev.ragnarok.fenrir.model.SideSwitchableCategory;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

class SideDrawerSettings implements ISettings.ISideDrawerSettings {

    private final Context app;
    private final PublishSubject<Object> publishSubject;

    SideDrawerSettings(Context context) {
        app = context.getApplicationContext();
        publishSubject = PublishSubject.create();
    }

    private static int findCategoryIndex(int[] array, @SideSwitchableCategory int category) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == category) {
                return i;
            }
        }

        throw new IllegalStateException("Invalid category " + category);
    }

    private static String keyForCategory(@SideSwitchableCategory int category) {
        return "side_drawer_category_" + category;
    }

    @Override
    public boolean isCategoryEnabled(@SideSwitchableCategory int category) {
        return PreferenceManager.getDefaultSharedPreferences(app)
                .getBoolean(keyForCategory(category), true);
    }

    @Override
    public void setCategoriesOrder(@SideSwitchableCategory int[] order, boolean[] active) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(app);

        for (int i = 0; i < order.length; i++) {
            @SideSwitchableCategory
            int category = order[i];
            preferences.edit()
                    .putBoolean(keyForCategory(category), active[i])
                    .apply();
        }

        StringBuilder builder = new StringBuilder();
        for (int i : order) {
            builder.append(i).append("-");
        }

        preferences.edit()
                .putString("side_drawer_categories_order", builder.toString().trim())
                .apply();

        publishSubject.onNext(Void.class);
    }

    @Override
    public int[] getCategoriesOrder() {
        @SideSwitchableCategory
        int[] all = {FRIENDS, DIALOGS, FEED, FEEDBACK, NEWSFEED_COMMENTS, GROUPS, PHOTOS, VIDEOS, MUSIC, DOCS, BOOKMARKS, SEARCH};
        String line = PreferenceManager.getDefaultSharedPreferences(app).getString("side_drawer_categories_order", null);

        String[] parts = isEmpty(line) ? new String[0] : line.split("-");

        int[] positions = new int[parts.length];

        try {
            for (int i = 0; i < parts.length; i++) {
                positions[i] = Integer.parseInt(parts[i]);
            }
        } catch (Exception ignored) {/*ignore*/}

        for (int i = 0; i < positions.length; i++) {
            int category = positions[i];
            // категория "category" должна быть в положении "i"
            if (i >= all.length)
                break;
            if (all[i] != category) {
                try {
                    int currentCategoryIndex = findCategoryIndex(all, category);
                    all[currentCategoryIndex] = all[i];
                    all[i] = category;
                } catch (Exception ignored) {/*ignore*/}
            }
        }

        return all;
    }

    @Override
    public Observable<Object> observeChanges() {
        return publishSubject;
    }
}
