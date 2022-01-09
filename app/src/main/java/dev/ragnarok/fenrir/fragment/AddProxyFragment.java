package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AddProxyPresenter;
import dev.ragnarok.fenrir.mvp.view.IAddProxyView;

public class AddProxyFragment extends BaseMvpFragment<AddProxyPresenter, IAddProxyView> implements IAddProxyView {

    private CheckBox mAuth;
    private View mAuthFieldsRoot;

    public static AddProxyFragment newInstance() {
        Bundle args = new Bundle();
        AddProxyFragment fragment = new AddProxyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_proxy_add, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mAuthFieldsRoot = root.findViewById(R.id.auth_fields_root);

        TextInputEditText mAddress = root.findViewById(R.id.address);
        mAddress.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireAddressEdit(s));
            }
        });

        TextInputEditText mPort = root.findViewById(R.id.port);
        mPort.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.firePortEdit(s));
            }
        });

        mAuth = root.findViewById(R.id.authorization);
        mAuth.setOnCheckedChangeListener((buttonView, isChecked) -> callPresenter(p -> p.fireAuthChecked(isChecked)));

        TextInputEditText mUsername = root.findViewById(R.id.username);
        mUsername.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireUsernameEdit(s));
            }
        });

        TextInputEditText mPassword = root.findViewById(R.id.password);
        mPassword.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.firePassEdit(s));
            }
        });

        root.findViewById(R.id.button_save).setOnClickListener(v -> callPresenter(AddProxyPresenter::fireSaveClick));
        return root;
    }

    @Override
    public void setAuthFieldsEnabled(boolean enabled) {
        if (nonNull(mAuthFieldsRoot)) {
            mAuthFieldsRoot.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setAuthChecked(boolean checked) {
        if (nonNull(mAuth)) {
            mAuth.setChecked(checked);
        }
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }

    @NonNull
    @Override
    public IPresenterFactory<AddProxyPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AddProxyPresenter(saveInstanceState);
    }
}