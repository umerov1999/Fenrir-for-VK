package dev.ragnarok.fenrir.util;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public abstract class FindAtWithContent<T> {
    private final List<T> cached;
    private final CompositeDisposable disposable;
    private final int visibleCount;
    private final int searchCount;
    private String q;
    private boolean ended;
    private boolean needSearchInCache;
    private int offset;

    public FindAtWithContent(CompositeDisposable disposable, int visibleCount, int searchCount) {
        cached = new ArrayList<>();
        this.disposable = disposable;
        this.visibleCount = visibleCount;
        this.searchCount = searchCount;
        needSearchInCache = true;
    }

    protected abstract Single<List<T>> search(int offset, int count);

    protected abstract void onError(@NonNull Throwable e);

    protected abstract void onResult(@NonNull List<T> data);

    protected abstract void updateLoading(boolean loading);

    protected abstract void clean();

    protected abstract boolean compare(@NonNull T data, @NonNull String q);

    protected abstract void onReset(@NonNull List<T> data, int offset, boolean isEnd);

    public void do_search() {
        do_search(q);
    }

    public void cancel() {
        if (q != null) {
            q = null;
            onReset(cached, offset, ended);
        }
    }

    public void do_search(String q) {
        if (Utils.isEmpty(q)) {
            this.q = q;
            return;
        } else if (!q.equalsIgnoreCase(this.q)) {
            needSearchInCache = true;
            this.q = q;
            clean();
        }
        if (needSearchInCache) {
            needSearchInCache = false;
            List<T> result = new ArrayList<>();
            for (T i : cached) {
                if (compare(i, q)) {
                    result.add(i);
                }
            }
            if (!Utils.isEmpty(result)) {
                onResult(result);
            }
        }
        if (!ended) {
            updateLoading(true);
            progress(0);
        }
    }

    private void progress(int searched) {
        disposable.add(search(offset, searchCount).compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(r -> {
                    offset += searchCount;
                    if (Utils.isEmpty(r)) {
                        ended = true;
                        updateLoading(false);
                    } else {
                        cached.addAll(r);
                        List<T> result = new ArrayList<>();
                        for (T i : r) {
                            if (compare(i, q)) {
                                result.add(i);
                            }
                        }
                        if (!Utils.isEmpty(result)) {
                            onResult(result);
                        }
                        if (searched + result.size() >= visibleCount) {
                            updateLoading(false);
                        } else {
                            progress(searched + result.size());
                        }
                    }
                }, this::onError));
    }

    public void reset() {
        ended = false;
        cached.clear();
        needSearchInCache = true;
        String tmp = q;
        q = null;
        offset = 0;
        do_search(tmp);
    }

    public void insertCache(List<T> data, int offset) {
        if (Utils.isEmpty(data) || !Utils.isEmpty(cached)) {
            return;
        }
        this.offset = offset;
        cached.addAll(data);
    }

    public boolean isSearchMode() {
        return !Utils.isEmpty(q);
    }
}
