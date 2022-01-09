package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Day;
import dev.ragnarok.fenrir.model.GroupSettings;
import dev.ragnarok.fenrir.model.IdOption;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommunityOptionsPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityOptionsView;
import dev.ragnarok.fenrir.util.Month;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.view.MySpinnerView;

public class CommunityOptionsFragment extends BaseMvpFragment<CommunityOptionsPresenter, ICommunityOptionsView>
        implements ICommunityOptionsView {

    private TextInputEditText mName;
    private TextInputEditText mDescription;
    private View mCommunityTypeRoot;
    private TextInputEditText mAddress;
    private View mCategoryRoot;
    private MySpinnerView mCategory;
    private View mSubjectRoot;
    private MySpinnerView[] mSubjects;
    private TextInputEditText mWebsite;
    private View mPublicDateRoot;
    private TextView mDay;
    private TextView mMonth;
    private TextView mYear;
    private View mFeedbackCommentsRoot;
    private CheckBox mFeedbackComments;
    private CheckBox mObsceneFilter;
    private CheckBox mObsceneStopWords;
    private TextInputEditText mObsceneStopWordsEditText;

    public static CommunityOptionsFragment newInstance(int accountId, Community community, GroupSettings settings) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.GROUP, community);
        args.putParcelable(Extra.SETTINGS, settings);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        CommunityOptionsFragment fragment = new CommunityOptionsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_options, container, false);

        mName = root.findViewById(R.id.name);
        mDescription = root.findViewById(R.id.description);
        mCommunityTypeRoot = root.findViewById(R.id.community_type_root);
        mAddress = root.findViewById(R.id.link);

        mCategoryRoot = root.findViewById(R.id.category_root);
        mCategory = root.findViewById(R.id.spinner_category);
        mCategory.setIconOnClickListener(v -> callPresenter(CommunityOptionsPresenter::onCategoryClick));

        mSubjectRoot = root.findViewById(R.id.subject_root);
        mSubjects = new MySpinnerView[2];
        mSubjects[0] = root.findViewById(R.id.subject_0);
        mSubjects[1] = root.findViewById(R.id.subject_1);

        mWebsite = root.findViewById(R.id.website);

        mPublicDateRoot = root.findViewById(R.id.public_date_root);
        mDay = root.findViewById(R.id.day);
        mDay.setOnClickListener(v -> callPresenter(CommunityOptionsPresenter::fireDayClick));

        mMonth = root.findViewById(R.id.month);
        mMonth.setOnClickListener(v -> callPresenter(CommunityOptionsPresenter::fireMonthClick));

        mYear = root.findViewById(R.id.year);
        mYear.setOnClickListener(v -> callPresenter(CommunityOptionsPresenter::fireYearClick));

        mFeedbackCommentsRoot = root.findViewById(R.id.feedback_comments_root);
        mFeedbackComments = root.findViewById(R.id.feedback_comments);

        mObsceneFilter = root.findViewById(R.id.obscene_filter);
        mObsceneStopWords = root.findViewById(R.id.obscene_stopwords);
        mObsceneStopWordsEditText = root.findViewById(R.id.obscene_stopwords_values);
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<CommunityOptionsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            VKApiCommunity community = requireArguments().getParcelable(Extra.GROUP);
            GroupSettings settings = requireArguments().getParcelable(Extra.SETTINGS);
            return new CommunityOptionsPresenter(accountId, community, settings, saveInstanceState);
        };
    }

    @Override
    public void displayName(String title) {
        safelySetText(mName, title);
    }

    @Override
    public void displayDescription(String description) {
        safelySetText(mDescription, description);
    }

    @Override
    public void setCommunityTypeVisible(boolean visible) {
        safelySetVisibleOrGone(mCommunityTypeRoot, visible);
    }

    @Override
    public void displayAddress(String address) {
        safelySetText(mAddress, address);
    }

    @Override
    public void setCategoryVisible(boolean visible) {
        safelySetVisibleOrGone(mCategoryRoot, visible);
    }

    @Override
    public void displayCategory(String categoryText) {
        if (Objects.nonNull(mCategory)) {
            mCategory.setValue(categoryText);
        }
    }

    @Override
    public void showSelectOptionDialog(int requestCode, List<IdOption> data) {
        String[] strings = new String[data.size()];
        for (int i = 0; i < data.size(); i++) {
            strings[i] = data.get(i).getTitle();
        }

        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_from_list_title)
                .setItems(strings, (dialog, which) -> callPresenter(p -> p.fireOptionSelected(requestCode, data.get(which))))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void setSubjectRootVisible(boolean visible) {
        safelySetVisibleOrGone(mSubjectRoot, visible);
    }

    @Override
    public void setSubjectVisible(int index, boolean visible) {
        if (Objects.nonNull(mSubjects)) {
            safelySetVisibleOrGone(mSubjects[index], visible);
        }
    }

    @Override
    public void displaySubjectValue(int index, String value) {
        if (Objects.nonNull(mSubjects)) {
            mSubjects[index].setValue(value);
        }
    }

    @Override
    public void displayWebsite(String website) {
        safelySetText(mWebsite, website);
    }

    @Override
    public void setPublicDateVisible(boolean visible) {
        safelySetVisibleOrGone(mPublicDateRoot, visible);
    }

    @Override
    public void dislayPublicDate(Day day) {
        if (day.getDay() > 0) {
            safelySetText(mDay, String.valueOf(day.getDay()));
        } else {
            safelySetText(mDay, R.string.day);
        }

        if (day.getYear() > 0) {
            safelySetText(mYear, String.valueOf(day.getYear()));
        } else {
            safelySetText(mYear, R.string.year);
        }

        if (day.getMonth() > 0) {
            safelySetText(mMonth, Month.getMonthTitle(day.getMonth()));
        } else {
            safelySetText(mMonth, R.string.month);
        }
    }

    @Override
    public void setFeedbackCommentsRootVisible(boolean visible) {
        safelySetVisibleOrGone(mFeedbackCommentsRoot, visible);
    }

    @Override
    public void setFeedbackCommentsChecked(boolean checked) {
        safelySetCheched(mFeedbackComments, checked);
    }

    @Override
    public void setObsceneFilterChecked(boolean checked) {
        safelySetCheched(mObsceneFilter, checked);
    }

    @Override
    public void setObsceneStopWordsChecked(boolean checked) {
        safelySetCheched(mObsceneStopWords, checked);
    }

    @Override
    public void setObsceneStopWordsVisible(boolean visible) {
        safelySetVisibleOrGone(mObsceneStopWordsEditText, visible);
    }

    @Override
    public void displayObsceneStopWords(String words) {
        safelySetText(mObsceneStopWordsEditText, words);
    }
}