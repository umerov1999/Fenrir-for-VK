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
import dev.ragnarok.fenrir.adapter.vkdatabase.CitiesAdapter;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.domain.IDatabaseInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.model.City;
import dev.ragnarok.fenrir.util.RxUtils;

public class SelectCityDialog extends AccountDependencyDialogFragment implements CitiesAdapter.Listener {

    public static final String REQUEST_CODE_CITY = "request_city";
    private static final int COUNT_PER_REQUEST = 1000;
    private static final int RUN_SEARCH_DELAY = 1000;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int accountId;
    private int countryId;
    private ArrayList<City> mData;
    private CitiesAdapter mAdapter;
    private String filter;
    private IDatabaseInteractor databaseInteractor;
    private final Runnable mRunSearchRunnable = () -> request(0);

    public static SelectCityDialog newInstance(int aid, int countryId, Bundle additional) {
        Bundle args = additional == null ? new Bundle() : additional;
        args.putInt(Extra.COUNTRY_ID, countryId);
        args.putInt(Extra.ACCOUNT_ID, aid);
        SelectCityDialog selectCityDialog = new SelectCityDialog();
        selectCityDialog.setArguments(args);
        return selectCityDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        databaseInteractor = InteractorFactory.createDatabaseInteractor();
        countryId = requireArguments().getInt(Extra.COUNTRY_ID);
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
                mHandler.removeCallbacks(mRunSearchRunnable);
                mHandler.postDelayed(mRunSearchRunnable, RUN_SEARCH_DELAY);
            }
        });

        RecyclerView mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));

        boolean firstRun = false;
        if (mData == null) {
            mData = new ArrayList<>();
            firstRun = true;
        }

        mAdapter = new CitiesAdapter(requireActivity(), mData);
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);

        if (firstRun) {
            request(0);
        }

        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.city)
                .setView(root)
                .setNegativeButton(R.string.button_cancel, null)
                .create();
    }

    private void request(int offset) {
        appendDisposable(databaseInteractor.getCities(accountId, countryId, filter, true, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(cities -> onRequestFinised(cities, offset), this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        // TODO: 04.10.2017
    }

    private void onRequestFinised(List<City> cities, int offset) {
        if (offset == 0) {
            mData.clear();
        }

        mData.addAll(cities);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunSearchRunnable);
    }

    @Override
    public void onClick(City city) {
        Bundle intent = new Bundle();
        intent.putParcelable(Extra.CITY, city);
        intent.putInt(Extra.ID, city.getId());
        intent.putString(Extra.TITLE, city.getTitle());

        if (getArguments() != null) {
            intent.putAll(getArguments());
        }

        getParentFragmentManager().setFragmentResult(REQUEST_CODE_CITY, intent);
        dismiss();
    }
}