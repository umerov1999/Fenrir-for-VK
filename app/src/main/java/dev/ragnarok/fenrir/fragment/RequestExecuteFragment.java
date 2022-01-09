package dev.ragnarok.fenrir.fragment;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.RequestExecutePresenter;
import dev.ragnarok.fenrir.mvp.view.IRequestExecuteView;
import dev.ragnarok.fenrir.util.AppPerms;

public class RequestExecuteFragment extends BaseMvpFragment<RequestExecutePresenter, IRequestExecuteView> implements IRequestExecuteView {

    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> callPresenter(RequestExecutePresenter::fireWritePermissionResolved));
    private TextInputEditText mResposeBody;

    public static RequestExecuteFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        RequestExecuteFragment fragment = new RequestExecuteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request_executor, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mResposeBody = root.findViewById(R.id.response_body);

        MaterialAutoCompleteTextView methodEditText = root.findViewById(R.id.method);
        methodEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireMethodEdit(s));
            }
        });

        TextInputEditText bodyEditText = root.findViewById(R.id.body);
        bodyEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireBodyEdit(s));
            }
        });

        root.findViewById(R.id.button_copy).setOnClickListener(v -> callPresenter(RequestExecutePresenter::fireCopyClick));
        root.findViewById(R.id.button_save).setOnClickListener(v -> callPresenter(RequestExecutePresenter::fireSaveClick));
        root.findViewById(R.id.button_execute).setOnClickListener(v -> callPresenter(RequestExecutePresenter::fireExecuteClick));
        return root;
    }

    @NonNull
    @Override
    public IPresenterFactory<RequestExecutePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new RequestExecutePresenter(requireArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.request_executor_title);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayBody(String body) {
        safelySetText(mResposeBody, body);
    }

    @Override
    public void hideKeyboard() {
        ActivityUtils.hideSoftKeyboard(requireActivity());
    }

    @Override
    public void requestWriteExternalStoragePermission() {
        requestWritePermission.launch();
    }
}