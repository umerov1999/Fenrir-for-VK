package dev.ragnarok.fenrir.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.vkdatabase.UniversitiesAdapter;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.domain.IDatabaseInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.model.database.University;
import dev.ragnarok.fenrir.util.RxUtils;

public class SelectUniversityDialog extends AccountDependencyDialogFragment implements UniversitiesAdapter.Listener {

    public static final String REQUEST_CODE_UNIVERSITY = "request_university";
    private static final int COUNT_PER_REQUEST = 1000;
    private static final int RUN_SEARCH_DELAY = 1000;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mAccountId;
    private int countryId;
    private IDatabaseInteractor mDatabaseInteractor;
    private ArrayList<University> mData;
    private UniversitiesAdapter mAdapter;
    private String filter;
    private final Runnable runSearchRunnable = () -> request(0);

    public static SelectUniversityDialog newInstance(int aid, int countryId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.COUNTRY_ID, countryId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectUniversityDialog selectCityDialog = new SelectUniversityDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        countryId = requireArguments().getInt(Extra.COUNTRY_ID);
        mDatabaseInteractor = InteractorFactory.createDatabaseInteractor();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = View.inflate(requireActivity(), R.layout.dialog_country_or_city_select, null);

        TextInputEditText input = root.findViewById(R.id.input);
        input.setText(filter);
        input.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                filter = s.toString();
                mHandler.removeCallbacks(runSearchRunnable);
                mHandler.postDelayed(runSearchRunnable, RUN_SEARCH_DELAY);
            }
        });

        RecyclerView mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new UniversitiesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request(0);
        }

        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.college_or_university)
                .setView(root)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    private void request(int offset) {
        appendDisposable(mDatabaseInteractor.getUniversities(mAccountId, filter, null, countryId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(universities -> onDataReceived(offset, universities), t -> {/*todo*/}));
    }

    private void onDataReceived(int offset, List<University> universities) {
        if (offset == 0) {
            mData.clear();
        }

        mData.addAll(universities);
        mAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(runSearchRunnable);
    }

    @Override
    public void onClick(University university) {
        Bundle intent = new Bundle();
        intent.putParcelable(Extra.UNIVERSITY, university);
        intent.putInt(Extra.ID, university.getId());
        intent.putString(Extra.TITLE, university.getTitle());

        if (getArguments() != null) {
            intent.putAll(getArguments());
        }

        getParentFragmentManager().setFragmentResult(REQUEST_CODE_UNIVERSITY, intent);
        dismiss();
    }
}