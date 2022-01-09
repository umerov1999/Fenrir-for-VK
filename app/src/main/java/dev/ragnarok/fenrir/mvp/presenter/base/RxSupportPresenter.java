package dev.ragnarok.fenrir.mvp.presenter.base;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.App;
import dev.ragnarok.fenrir.BuildConfig;
import dev.ragnarok.fenrir.Injection;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.mvp.core.AbsPresenter;
import dev.ragnarok.fenrir.mvp.core.IMvpView;
import dev.ragnarok.fenrir.mvp.view.IErrorView;
import dev.ragnarok.fenrir.service.ErrorLocalizer;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.InstancesCounter;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public abstract class RxSupportPresenter<V extends IMvpView> extends AbsPresenter<V> {

    private static final String SAVE_INSTANCE_ID = "save_instance_id";
    private static final String SAVE_TEMP_DATA_USAGE = "save_temp_data_usage";
    private static final InstancesCounter instancesCounter = new InstancesCounter();
    private final int instanceId;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private boolean tempDataUsage;
    private int viewCreationCounter;

    public RxSupportPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);

        if (nonNull(savedInstanceState)) {
            instanceId = savedInstanceState.getInt(SAVE_INSTANCE_ID);
            instancesCounter.fireExists(getClass(), instanceId);
            tempDataUsage = savedInstanceState.getBoolean(SAVE_TEMP_DATA_USAGE);
        } else {
            instanceId = instancesCounter.incrementAndGet(getClass());
        }
    }

    protected void fireTempDataUsage() {
        tempDataUsage = true;
    }

    @Override
    public void onGuiCreated(@NonNull V view) {
        viewCreationCounter++;
        super.onGuiCreated(view);
    }

    public int getViewCreationCount() {
        return viewCreationCounter;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_INSTANCE_ID, instanceId);
        outState.putBoolean(SAVE_TEMP_DATA_USAGE, tempDataUsage);
    }

    protected int getInstanceId() {
        return instanceId;
    }

    @Override
    public void onDestroyed() {
        compositeDisposable.dispose();

        if (tempDataUsage) {
            RxUtils.subscribeOnIOAndIgnore(Stores.getInstance()
                    .tempStore()
                    .delete(getInstanceId()));

            tempDataUsage = false;
        }

        super.onDestroyed();
    }

    public void appendDisposable(@NonNull Disposable disposable) {
        compositeDisposable.add(disposable);
    }

    protected CompositeDisposable getCompositeDisposable() {
        return compositeDisposable;
    }

    protected void showError(IErrorView view, Throwable throwable) {
        if (isNull(view)) {
            return;
        }

        throwable = Utils.getCauseIfRuntime(throwable);

        if (BuildConfig.DEBUG) {
            throwable.printStackTrace();
        }

        if (Settings.get().other().isDeveloper_mode()) {
            view.showThrowable(throwable);
        } else {
            view.showError(ErrorLocalizer.localizeThrowable(getApplicationContext(), throwable));
        }
    }

    protected Context getApplicationContext() {
        return Injection.provideApplicationContext();
    }

    protected String getString(@StringRes int res) {
        return App.getInstance().getString(res);
    }

    protected String getString(@StringRes int res, Object... params) {
        return App.getInstance().getString(res, params);
    }
}