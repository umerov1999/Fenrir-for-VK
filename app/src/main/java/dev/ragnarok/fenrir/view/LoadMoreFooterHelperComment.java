package dev.ragnarok.fenrir.view;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.LoadMoreState;

public class LoadMoreFooterHelperComment {

    public Callback callback;
    public Holder holder;
    public int state = LoadMoreState.INVISIBLE;

    public static LoadMoreFooterHelperComment createFrom(View view, Callback callback) {
        LoadMoreFooterHelperComment helper = new LoadMoreFooterHelperComment();
        helper.holder = new Holder(view);
        helper.callback = callback;
        helper.holder.bLoadMore.setOnClickListener(v -> {
            if (callback != null) {
                callback.onLoadMoreClick();
            }
        });
        return helper;
    }

    public void setEndOfListTextRes(@StringRes int res) {
        holder.tvEndOfList.setText(res);
    }

    public void setEndOfListText(String text) {
        holder.tvEndOfList.setText(text);
    }

    public void switchToState(@LoadMoreState int state) {
        this.state = state;
        holder.container.setVisibility(state == LoadMoreState.INVISIBLE ? View.GONE : View.VISIBLE);

        switch (state) {
            case LoadMoreState.LOADING:
                holder.tvEndOfList.setVisibility(View.INVISIBLE);
                holder.bLoadMore.setVisibility(View.INVISIBLE);
                holder.progress.setVisibility(View.VISIBLE);
                break;
            case LoadMoreState.END_OF_LIST:
                holder.tvEndOfList.setVisibility(View.VISIBLE);
                holder.bLoadMore.setVisibility(View.INVISIBLE);
                holder.progress.setVisibility(View.INVISIBLE);
                break;
            case LoadMoreState.CAN_LOAD_MORE:
                holder.tvEndOfList.setVisibility(View.INVISIBLE);
                holder.bLoadMore.setVisibility(View.VISIBLE);
                holder.progress.setVisibility(View.INVISIBLE);
                break;
            case LoadMoreState.INVISIBLE:
                break;
        }
    }

    public interface Callback {
        void onLoadMoreClick();
    }

    public static class Holder {
        public final View container;
        public final ProgressBar progress;
        public final View bLoadMore;
        public final TextView tvEndOfList;

        public Holder(View root) {
            container = root.findViewById(R.id.footer_load_more_root);
            progress = root.findViewById(R.id.footer_load_more_progress);
            bLoadMore = root.findViewById(R.id.footer_load_more_run);
            tvEndOfList = root.findViewById(R.id.footer_load_more_end_of_list);
        }
    }
}
