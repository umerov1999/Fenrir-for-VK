package dev.ragnarok.fenrir.fragment.sheet;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dev.ragnarok.fenrir.mvp.compat.ViewHostDelegate;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.core.IPresenter;
import dev.ragnarok.fenrir.mvp.core.PresenterAction;
import dev.ragnarok.fenrir.mvp.core.RetPresenterAction;

public abstract class AbsPresenterBottomSheetFragment<P extends IPresenter<V>, V extends IMvpView>
        extends BottomSheetDialogFragment implements ViewHostDelegate.IFactoryProvider<P, V> {

    private final ViewHostDelegate<P, V> delegate = new ViewHostDelegate<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate.onCreate(requireActivity(), getPresenterViewHost(), this, LoaderManager.getInstance(this), savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fireViewCreated();
    }

    public void fireViewCreated() {
        delegate.onViewCreated();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        delegate.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        delegate.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        delegate.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        delegate.onDestroyView();
    }

    @Override
    public void onDestroy() {
        delegate.onDestroy();
        super.onDestroy();
    }

    // Override in case of fragment not implementing IPresenter<View> interface
    @SuppressWarnings("unchecked")
    @NonNull
    protected V getPresenterViewHost() {
        return (V) this;
    }

    protected P getPresenter() {
        return delegate.getPresenter();
    }

    protected void callPresenter(PresenterAction<P, V> action) {
        delegate.callPresenter(action);
    }

    protected <T> T callPresenter(RetPresenterAction<P, V, T> action, T onDefault) {
        return delegate.callPresenter(action, onDefault);
    }
}