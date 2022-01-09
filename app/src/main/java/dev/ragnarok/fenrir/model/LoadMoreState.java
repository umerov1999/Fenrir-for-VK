package dev.ragnarok.fenrir.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({LoadMoreState.LOADING, LoadMoreState.INVISIBLE, LoadMoreState.CAN_LOAD_MORE, LoadMoreState.END_OF_LIST})
@Retention(RetentionPolicy.SOURCE)
public @interface LoadMoreState {
    int LOADING = 1;
    int INVISIBLE = 2;
    int CAN_LOAD_MORE = 3;
    int END_OF_LIST = 4;
}
