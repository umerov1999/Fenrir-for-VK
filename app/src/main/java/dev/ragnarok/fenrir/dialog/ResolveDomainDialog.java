package dev.ragnarok.fenrir.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.domain.IUtilsInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.spots.SpotsDialog;
import dev.ragnarok.fenrir.util.Optional;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;

public class ResolveDomainDialog extends AccountDependencyDialogFragment {

    private int mAccountId;
    private String url;
    private String domain;
    private IUtilsInteractor mUtilsInteractor;

    public static Bundle buildArgs(int aid, String url, String domain) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putString(Extra.URL, url);
        args.putString(Extra.DOMAIN, domain);
        return args;
    }

    public static ResolveDomainDialog newInstance(int aid, String url, String domain) {
        return newInstance(buildArgs(aid, url, domain));
    }

    public static ResolveDomainDialog newInstance(Bundle args) {
        ResolveDomainDialog domainDialog = new ResolveDomainDialog();
        domainDialog.setArguments(args);
        return domainDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        mUtilsInteractor = InteractorFactory.createUtilsInteractor();
        url = requireArguments().getString(Extra.URL);
        domain = requireArguments().getString(Extra.DOMAIN);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog progressDialog = new SpotsDialog.Builder().setContext(requireActivity()).setMessage(getString(R.string.please_wait)).setCancelable(true).setCancelListener(this).build();

        request();
        return progressDialog;
    }

    private void request() {
        appendDisposable(mUtilsInteractor.resolveDomain(mAccountId, domain)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onResolveResult, this::onResolveError));
    }

    private void onResolveError(Throwable t) {
        showErrorAlert(Utils.getCauseIfRuntime(t).getMessage());
    }

    private void onResolveResult(Optional<Owner> optionalOwner) {
        if (optionalOwner.isEmpty()) {
            PlaceFactory.getExternalLinkPlace(getAccountId(), url).tryOpenWith(requireActivity());
        } else {
            PlaceFactory.getOwnerWallPlace(mAccountId, optionalOwner.get()).tryOpenWith(requireActivity());
        }

        dismissAllowingStateLoss();
    }

    private void showErrorAlert(String error) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.error)
                .setMessage(error).setPositiveButton(R.string.try_again, (dialog, which) -> request())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dismiss())
                .show();
    }
}