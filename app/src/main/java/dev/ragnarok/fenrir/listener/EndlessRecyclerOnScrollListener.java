package dev.ragnarok.fenrir.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import dev.ragnarok.fenrir.util.Objects;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

    private final int MIN_DELAY;
    private final int VISIBILITY_THRESHOLD; //elements to the end
    private Long mLastInterceptTime;
    private int visibleItemCount;
    private int totalItemCount;
    private int pastVisibleItems;

    public EndlessRecyclerOnScrollListener() {
        MIN_DELAY = 100;
        VISIBILITY_THRESHOLD = 0;
    }

    public EndlessRecyclerOnScrollListener(int visibilityThreshold, int minDelay) {
        MIN_DELAY = minDelay;
        VISIBILITY_THRESHOLD = visibilityThreshold;
    }

    private boolean isAllowScrollIntercept(long minDelay) {
        return mLastInterceptTime == null || System.currentTimeMillis() - mLastInterceptTime > minDelay;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();

        if (Objects.isNull(manager)) {
            // wtf?
            return;
        }

        if (!isAllowScrollIntercept(MIN_DELAY)) {
            return;
        }

        boolean isLastElementVisible = false;
        if (manager instanceof StaggeredGridLayoutManager) {
            isLastElementVisible = isAtLastElementOfStaggedGridLayoutManager((StaggeredGridLayoutManager) manager);
        }

        if (manager instanceof LinearLayoutManager) {
            isLastElementVisible = isAtLastElementOfLinearLayoutManager((LinearLayoutManager) manager);
        }

        if (manager instanceof GridLayoutManager) {
            isLastElementVisible = isAtLastElementOfGridLayoutManager((GridLayoutManager) manager);
        }

        if (isLastElementVisible) {
            mLastInterceptTime = System.currentTimeMillis();
            onScrollToLastElement();
            return;
        }

        boolean isFirstElementVisible = false;

        if (manager instanceof LinearLayoutManager) {
            isFirstElementVisible = ((LinearLayoutManager) manager).findFirstVisibleItemPosition() == 0;
        }

        if (isFirstElementVisible) {
            mLastInterceptTime = System.currentTimeMillis();
            onScrollToFirstElement();
        }
    }

    private boolean isAtLastElementOfLinearLayoutManager(LinearLayoutManager linearLayoutManager) {
        visibleItemCount = linearLayoutManager.getChildCount();
        totalItemCount = linearLayoutManager.getItemCount();
        pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();
        return (visibleItemCount + pastVisibleItems) >= totalItemCount - VISIBILITY_THRESHOLD;
    }

    private boolean isAtLastElementOfGridLayoutManager(GridLayoutManager gridLayoutManager) {
        visibleItemCount = gridLayoutManager.getChildCount();
        totalItemCount = gridLayoutManager.getItemCount();
        pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();
        return (visibleItemCount + pastVisibleItems) >= totalItemCount - VISIBILITY_THRESHOLD;
    }

    private boolean isAtLastElementOfStaggedGridLayoutManager(StaggeredGridLayoutManager staggeredGridLayoutManager) {
        visibleItemCount = staggeredGridLayoutManager.getChildCount();
        totalItemCount = staggeredGridLayoutManager.getItemCount();
        int[] firstVisibleItems = new int[staggeredGridLayoutManager.getSpanCount()];
        firstVisibleItems = staggeredGridLayoutManager.findFirstVisibleItemPositions(firstVisibleItems);

        if (firstVisibleItems != null && firstVisibleItems.length > 0) {
            pastVisibleItems = firstVisibleItems[0];
        }

        return (visibleItemCount + pastVisibleItems) >= totalItemCount - VISIBILITY_THRESHOLD;
    }

    public abstract void onScrollToLastElement();

    public void onScrollToFirstElement() {

    }
}
