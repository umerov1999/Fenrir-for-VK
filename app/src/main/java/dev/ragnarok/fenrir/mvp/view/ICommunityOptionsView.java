package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.Day;
import dev.ragnarok.fenrir.model.IdOption;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;


public interface ICommunityOptionsView extends IMvpView, IAccountDependencyView, IErrorView, IProgressView {
    void displayName(String name);

    void displayDescription(String description);

    void setCommunityTypeVisible(boolean visible);

    void displayAddress(String address);

    void setCategoryVisible(boolean visible);

    void displayCategory(String categoryText);

    void showSelectOptionDialog(int requestCode, List<IdOption> data);

    void setSubjectRootVisible(boolean visible);

    void setSubjectVisible(int index, boolean visible);

    void displaySubjectValue(int index, String value);

    void displayWebsite(String website);

    void setPublicDateVisible(boolean visible);

    void dislayPublicDate(Day day);

    void setFeedbackCommentsRootVisible(boolean visible);

    void setFeedbackCommentsChecked(boolean checked);

    void setObsceneFilterChecked(boolean checked);

    void setObsceneStopWordsChecked(boolean checked);

    void setObsceneStopWordsVisible(boolean visible);

    void displayObsceneStopWords(String words);
}