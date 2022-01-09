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
import dev.ragnarok.fenrir.adapter.vkdatabase.ChairsAdapter;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.domain.IDatabaseInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.database.Chair;
import dev.ragnarok.fenrir.util.RxUtils;

public class SelectChairsDialog extends AccountDependencyDialogFragment implements ChairsAdapter.Listener {

    public static final String REQUEST_CODE_CHAIRS = "request_chairs";
    private static final int COUNT_PER_REQUEST = 1000;
    private int mAccountId;
    private int facultyId;
    private ArrayList<Chair> mData;
    private ChairsAdapter mAdapter;
    private IDatabaseInteractor mDatabaseInteractor;

    public static SelectChairsDialog newInstance(int aid, int facultyId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.FACULTY_ID, facultyId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectChairsDialog selectCityDialog = new SelectChairsDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        mDatabaseInteractor = InteractorFactory.createDatabaseInteractor();
        facultyId = requireArguments().getInt(Extra.FACULTY_ID);
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

        mAdapter = new ChairsAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request(0);
        }

        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.chair)
                .setView(root)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    private void request(int offset) {
        appendDisposable(mDatabaseInteractor.getChairs(mAccountId, facultyId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(chairs -> onDataReceived(offset, chairs), throwable -> {
                }));
    }

    private void onDataReceived(int offset, List<Chair> chairs) {
        if (offset == 0) {
            mData.clear();
        }

        mData.addAll(chairs);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(Chair chair) {
        Bundle intent = new Bundle();
        intent.putParcelable(Extra.CHAIR, chair);
        intent.putInt(Extra.ID, chair.getId());
        intent.putString(Extra.TITLE, chair.getTitle());

        if (getArguments() != null) {
            intent.putAll(getArguments());
        }
        getParentFragmentManager().setFragmentResult(REQUEST_CODE_CHAIRS, intent);
        dismiss();
    }
}
