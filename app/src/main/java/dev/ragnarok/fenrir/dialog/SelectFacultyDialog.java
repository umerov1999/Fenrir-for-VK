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
import dev.ragnarok.fenrir.adapter.vkdatabase.FacultiesAdapter;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.domain.IDatabaseInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.database.Faculty;
import dev.ragnarok.fenrir.util.RxUtils;

public class SelectFacultyDialog extends AccountDependencyDialogFragment implements FacultiesAdapter.Listener {

    public static final String REQUEST_CODE_FACULTY = "request_faculty";
    private static final int COUNT_PER_REQUEST = 1000;
    private int mAccountId;
    private int universityId;
    private IDatabaseInteractor mDatabaseInteractor;
    private ArrayList<Faculty> mData;
    private FacultiesAdapter mAdapter;

    public static SelectFacultyDialog newInstance(int aid, int universityId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.UNIVERSITY_ID, universityId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectFacultyDialog selectCityDialog = new SelectFacultyDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        mDatabaseInteractor = InteractorFactory.createDatabaseInteractor();
        universityId = requireArguments().getInt(Extra.UNIVERSITY_ID);
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

        mAdapter = new FacultiesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request(0);
        }

        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.faculty)
                .setView(root)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    private void request(int offset) {
        appendDisposable(mDatabaseInteractor.getFaculties(mAccountId, universityId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(faculties -> onDataReceived(offset, faculties), t -> {/* TODO: 04.10.2017*/ }));
    }

    private void onDataReceived(int offset, List<Faculty> faculties) {
        if (offset == 0) {
            mData.clear();
        }

        mData.addAll(faculties);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(Faculty faculty) {
        Bundle intent = new Bundle();
        intent.putParcelable(Extra.FACULTY, faculty);
        intent.putInt(Extra.ID, faculty.getId());
        intent.putString(Extra.TITLE, faculty.getTitle());

        if (getArguments() != null) {
            intent.putAll(getArguments());
        }

        getParentFragmentManager().setFragmentResult(REQUEST_CODE_FACULTY, intent);
        dismiss();
    }
}
