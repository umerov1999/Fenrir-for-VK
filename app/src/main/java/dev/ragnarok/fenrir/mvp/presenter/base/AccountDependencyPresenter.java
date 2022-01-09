package dev.ragnarok.fenrir.mvp.presenter.base;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.base.IAccountDependencyView;
import dev.ragnarok.fenrir.settings.Settings;


public abstract class AccountDependencyPresenter<V extends IMvpView & IAccountDependencyView> extends RxSupportPresenter<V> {

    private static final String SAVE_ACCOUNT_ID = "save_account_id";
    private static final String SAVE_INVALID_ACCOUNT_CONTEXT = "save_invalid_account_context";

    private int mAccountId;
    private boolean mSupportAccountHotSwap;
    private boolean mInvalidAccountContext;

    public AccountDependencyPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        if (savedInstanceState != null) {
            mAccountId = savedInstanceState.getInt(SAVE_ACCOUNT_ID);
            mInvalidAccountContext = savedInstanceState.getBoolean(SAVE_INVALID_ACCOUNT_CONTEXT);
        } else {
            mAccountId = accountId;
        }

        appendDisposable(Settings.get()
                .accounts()
                .observeChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onAccountChange));
    }

    private void onAccountChange(int newAccountId) {
        int oldAccountId = mAccountId;
        if (oldAccountId == newAccountId) {
            mInvalidAccountContext = false;
            return;
        }

        if (mSupportAccountHotSwap) {
            beforeAccountChange(oldAccountId, newAccountId);
            mAccountId = newAccountId;
            afterAccountChange(oldAccountId, newAccountId);
        } else {
            mInvalidAccountContext = true;
            callView(IAccountDependencyView::displayAccountNotSupported);
        }
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        super.onGuiCreated(view);

        if (mInvalidAccountContext) {
            view.displayAccountNotSupported();
        }
    }

    @CallSuper
    protected void afterAccountChange(int oldAccountId, int newAccountId) {

    }

    @CallSuper
    protected void beforeAccountChange(int oldAccountId, int newAccountId) {

    }

    public int getAccountId() {
        return mAccountId;
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();
    }

    protected void setSupportAccountHotSwap(boolean supportAccountHotSwap) {
        mSupportAccountHotSwap = supportAccountHotSwap;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_ACCOUNT_ID, mAccountId);
        outState.putBoolean(SAVE_INVALID_ACCOUNT_CONTEXT, mInvalidAccountContext);
    }
}