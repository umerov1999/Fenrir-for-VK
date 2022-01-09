package dev.ragnarok.fenrir.fragment.base;

import androidx.fragment.app.Fragment;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class BaseFragment extends Fragment {

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected void appendDisposable(Disposable disposable) {
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }
}