package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.IdOption;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityOptionsView;


public class CommunityOptionsPresenter extends AccountDependencyPresenter<ICommunityOptionsView> {

    private static final int REQUEST_CATEGORY = 1;
    private static final int REQUEST_DAY = 2;
    private static final int REQUEST_MONTH = 3;
    private static final int REQUEST_YEAR = 4;
    private final VKApiCommunity community;
    private final GroupSettings settings;

    public CommunityOptionsPresenter(int accountId, VKApiCommunity community, GroupSettings settings, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.community = community;
        this.settings = settings;
    }

    @Override
    public void onGuiCreated(@NonNull ICommunityOptionsView view) {
        super.onGuiCreated(view);

        view.displayName(settings.getTitle());
        view.displayDescription(settings.getDescription());
        view.setCommunityTypeVisible(community.type == VKApiCommunity.Type.GROUP);
        view.displayAddress(settings.getAddress());
        view.displayWebsite(settings.getWebsite());

        view.setFeedbackCommentsRootVisible(community.type == VKApiCommunity.Type.PAGE);
        view.setFeedbackCommentsChecked(settings.isFeedbackCommentsEnabled());

        view.setObsceneFilterChecked(settings.isObsceneFilterEnabled());
        view.setObsceneStopWordsChecked(settings.isObsceneStopwordsEnabled());
        view.displayObsceneStopWords(settings.getObsceneWords());

        resolveObsceneWordsEditorVisibility();
        resolvePublicDateView();
        resolveCategoryView();
        resolveSubjectView();
    }

    private void resolveObsceneWordsEditorVisibility() {
        callView(v -> v.setObsceneStopWordsVisible(settings.isObsceneStopwordsEnabled()));
    }

    private void resolvePublicDateView() {
        callView(v -> {
            v.setPublicDateVisible(community.type == VKApiCommunity.Type.PAGE);
            v.dislayPublicDate(settings.getDateCreated());
        });
    }

    private void resolveCategoryView() {
        boolean available = community.type == VKApiCommunity.Type.PAGE;
        callView(v -> v.setCategoryVisible(available));

        if (available) {
            callView(v -> v.displayCategory(nonNull(settings.getCategory()) ? settings.getCategory().getTitle() : null));
        }
    }

    private void resolveSubjectView() {
        boolean available = community.type == VKApiCommunity.Type.GROUP;

        callView(v -> v.setSubjectRootVisible(available));

        if (available) {
            IdOption category = settings.getCategory();

            callView(v -> v.displaySubjectValue(0, nonNull(category) ? category.getTitle() : null));

            boolean subAvailable = nonNull(category) && nonEmpty(category.getChilds());

            callView(v -> v.setSubjectVisible(1, subAvailable));

            if (subAvailable) {
                IdOption sub = settings.getSubcategory();
                callView(v -> v.displaySubjectValue(1, nonNull(sub) ? sub.getTitle() : null));
            }
        }
    }

    public void onCategoryClick() {
        callView(v -> v.showSelectOptionDialog(REQUEST_CATEGORY, settings.getAvailableCategories()));
    }

    public void fireOptionSelected(int requestCode, IdOption option) {
        switch (requestCode) {
            case REQUEST_CATEGORY:
                settings.setCategory(option);
                resolveCategoryView();
                break;

            case REQUEST_DAY:
                settings.getDateCreated().setDay(option.getId());
                resolvePublicDateView();
                break;

            case REQUEST_MONTH:
                settings.getDateCreated().setMonth(option.getId());
                resolvePublicDateView();
                break;

            case REQUEST_YEAR:
                settings.getDateCreated().setYear(option.getId());
                resolvePublicDateView();
                break;
        }
    }

    public void fireDayClick() {
        List<IdOption> options = new ArrayList<>(32);
        options.add(new IdOption(0, getString(R.string.not_selected)));
        for (int i = 1; i <= 31; i++) {
            options.add(new IdOption(i, String.valueOf(i)));
        }

        callView(v -> v.showSelectOptionDialog(REQUEST_DAY, options));
    }

    public void fireMonthClick() {
        List<IdOption> options = new ArrayList<>(13);
        options.add(new IdOption(0, getString(R.string.not_selected)));
        options.add(new IdOption(1, getString(R.string.january)));
        options.add(new IdOption(1, getString(R.string.january)));
        options.add(new IdOption(2, getString(R.string.february)));
        options.add(new IdOption(3, getString(R.string.march)));
        options.add(new IdOption(4, getString(R.string.april)));
        options.add(new IdOption(5, getString(R.string.may)));
        options.add(new IdOption(6, getString(R.string.june)));
        options.add(new IdOption(7, getString(R.string.july)));
        options.add(new IdOption(8, getString(R.string.august)));
        options.add(new IdOption(9, getString(R.string.september)));
        options.add(new IdOption(10, getString(R.string.october)));
        options.add(new IdOption(11, getString(R.string.november)));
        options.add(new IdOption(12, getString(R.string.december)));
        callView(v -> v.showSelectOptionDialog(REQUEST_MONTH, options));
    }

    public void fireYearClick() {
        List<IdOption> options = new ArrayList<>();
        options.add(new IdOption(0, getString(R.string.not_selected)));
        for (int i = Calendar.getInstance().get(Calendar.YEAR); i >= 1800; i--) {
            options.add(new IdOption(i, String.valueOf(i)));
        }

        callView(v -> v.showSelectOptionDialog(REQUEST_YEAR, options));
    }
}