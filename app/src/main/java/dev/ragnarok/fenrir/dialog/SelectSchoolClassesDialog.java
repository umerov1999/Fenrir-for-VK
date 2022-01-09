package dev.ragnarok.fenrir.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.vkdatabase.SchoolClassesAdapter;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.domain.IDatabaseInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.database.SchoolClazz;
import dev.ragnarok.fenrir.util.RxUtils;

public class SelectSchoolClassesDialog extends AccountDependencyDialogFragment implements SchoolClassesAdapter.Listener {

    public static final String REQUEST_CODE_SCHOOL_CLASSES = "request_school_classes";
    private int mAccountId;
    private int countryId;
    private IDatabaseInteractor mDatabaseInteractor;
    private ArrayList<SchoolClazz> mData;
    private SchoolClassesAdapter mAdapter;

    public static SelectSchoolClassesDialog newInstance(int aid, int countryId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.COUNTRY_ID, countryId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectSchoolClassesDialog selectCityDialog = new SelectSchoolClassesDialog();
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
        View root = View.inflate(requireActivity(), R.layout.dialog_simple_recycler_view, null);
        RecyclerView mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new SchoolClassesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request();
        }

        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.school_class)
                .setView(root)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    private void request() {
        appendDisposable(mDatabaseInteractor.getSchoolClasses(mAccountId, countryId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, t -> {/*todo*/}));
    }

    private void onDataReceived(List<SchoolClazz> clazzes) {
        mData.clear();
        mData.addAll(clazzes);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(SchoolClazz schoolClazz) {
        Bundle intent = new Bundle();
        intent.putParcelable(Extra.SCHOOL_CLASS, schoolClazz);
        intent.putInt(Extra.ID, schoolClazz.getId());
        intent.putString(Extra.TITLE, schoolClazz.getTitle());

        if (getArguments() != null) {
            intent.putAll(getArguments());
        }

        getParentFragmentManager().setFragmentResult(REQUEST_CODE_SCHOOL_CLASSES, intent);
        dismiss();
    }
}
