package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.db.Stores;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.Disposable;

public class MySearchView extends LinearLayout {

    private static final String TAG = MySearchView.class.getSimpleName();

    private String mQuery;

    private MaterialAutoCompleteTextView mInput;
    private ImageView mButtonBack;
    private ImageView mButtonClear;
    private ImageView mButtonAdditional;
    private OnQueryTextListener mOnQueryChangeListener;
    private Disposable mQueryDisposable = Disposable.disposed();
    private ArrayAdapter<String> listQueries;
    private int searchId;
    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {

        /**
         * Called when the input method default action key is pressed.
         */
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            Logger.d(TAG, "onEditorAction, actionId: " + actionId + ", event: " + event);
            onSubmitQuery();
            return true;
        }
    };
    private OnBackButtonClickListener mOnBackButtonClickListener;
    private OnAdditionalButtonClickListener mOnAdditionalButtonClickListener;
    private OnAdditionalButtonLongClickListener mOnAdditionalButtonLongClickListener;

    public MySearchView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public MySearchView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MySearchView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mQueryDisposable.dispose();
    }

    private void loadQueries() {
        mQueryDisposable.dispose();
        mQueryDisposable = Stores.getInstance().searchQueriesStore().getQueries(searchId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(s -> {
                    listQueries.clear();
                    listQueries.addAll(s);
                }, RxUtils.ignore());
    }

    protected void init(@NonNull Context context, AttributeSet attrs) {
        LayoutInflater.from(getContext()).inflate(R.layout.custom_searchview, this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MySearchView);
        try {
            searchId = a.getInt(R.styleable.MySearchView_search_source_id, getId());
        } finally {
            a.recycle();
        }

        mInput = findViewById(R.id.input);
        mInput.setOnEditorActionListener(mOnEditorActionListener);

        listQueries = new ArrayAdapter<>(getContext(), R.layout.search_dropdown_item);
        mInput.setAdapter(listQueries);

        loadQueries();

        mButtonBack = findViewById(R.id.button_back);
        mButtonClear = findViewById(R.id.clear);
        mButtonAdditional = findViewById(R.id.additional);

        mInput.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                mQuery = s.toString();
                if (mOnQueryChangeListener != null) {
                    mOnQueryChangeListener.onQueryTextChange(s.toString());
                }

                resolveCloseButton();
            }
        });
        mButtonClear.setOnClickListener(v -> clear());

        mButtonBack.setOnClickListener(v -> {
            if (mOnBackButtonClickListener != null) {
                mOnBackButtonClickListener.onBackButtonClick();
            }
        });

        mButtonAdditional.setOnClickListener(v -> {
            if (mOnAdditionalButtonClickListener != null) {
                mOnAdditionalButtonClickListener.onAdditionalButtonClick();
            }
        });

        mButtonAdditional.setOnLongClickListener(v -> {
            if (mOnAdditionalButtonLongClickListener != null) {
                mOnAdditionalButtonLongClickListener.onAdditionalButtonLongClick();
            }
            return true;
        });

        resolveCloseButton();
    }

    public Editable getText() {
        return mInput.getText();
    }

    public void clear() {
        mInput.getText().clear();
    }

    private void onSubmitQuery() {
        CharSequence query = mInput.getText();
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            mQueryDisposable.dispose();
            mQueryDisposable = Stores.getInstance().searchQueriesStore().insertQuery(searchId, query.toString())
                    .compose(RxUtils.applyCompletableIOToMainSchedulers())
                    .subscribe(this::loadQueries, RxUtils.ignore());
            if (mOnQueryChangeListener != null && mOnQueryChangeListener.onQueryTextSubmit(query.toString())) {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                }
            }
        }
    }

    public void setRightButtonVisibility(boolean visible) {
        mButtonAdditional.setVisibility(visible ? VISIBLE : GONE);
    }

    private void resolveCloseButton() {
        boolean empty = TextUtils.isEmpty(mQuery);
        Logger.d(TAG, "resolveCloseButton, empty: " + empty);
        mButtonClear.setVisibility(TextUtils.isEmpty(mQuery) ? GONE : VISIBLE);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        Bundle state = new Bundle();
        state.putParcelable("PARENT", superState);
        state.putString("query", mQuery);

        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle savedState = (Bundle) state;

        Parcelable superState = savedState.getParcelable("PARENT");
        super.onRestoreInstanceState(superState);

        mQuery = savedState.getString("query");
        mInput.setText(mQuery);
    }

    public void setOnQueryTextListener(OnQueryTextListener onQueryChangeListener) {
        mOnQueryChangeListener = onQueryChangeListener;
    }

    public void setOnBackButtonClickListener(OnBackButtonClickListener onBackButtonClickListener) {
        mOnBackButtonClickListener = onBackButtonClickListener;
    }

    public void setOnAdditionalButtonClickListener(OnAdditionalButtonClickListener onAdditionalButtonClickListener) {
        mOnAdditionalButtonClickListener = onAdditionalButtonClickListener;
    }

    public void setOnAdditionalButtonLongClickListener(OnAdditionalButtonLongClickListener onAdditionalButtonLongClickListener) {
        mOnAdditionalButtonLongClickListener = onAdditionalButtonLongClickListener;
    }

    public void setQuery(String query, boolean quetly) {
        OnQueryTextListener tmp = mOnQueryChangeListener;
        if (quetly) {
            mOnQueryChangeListener = null;
        }

        setQuery(query);

        if (quetly) {
            mOnQueryChangeListener = tmp;
        }
    }

    public void setQuery(String query) {
        mInput.setText(query);
    }

    public void setSelection(int start, int end) {
        mInput.setSelection(start, end);
    }

    public void setSelection(int position) {
        mInput.setSelection(position);
    }

    public void setLeftIcon(@DrawableRes int drawable) {
        mButtonBack.setImageResource(drawable);
    }

    public void setLeftIconTint(@ColorInt int color) {
        Utils.setTint(mButtonBack, color);
    }

    public void setRightIconTint(@ColorInt int color) {
        Utils.setTint(mButtonAdditional, color);
    }

    public void setLeftIcon(Drawable drawable) {
        mButtonBack.setImageDrawable(drawable);
    }

    public void setRightIcon(Drawable drawable) {
        mButtonAdditional.setImageDrawable(drawable);
    }

    public void setRightIcon(@DrawableRes int drawable) {
        mButtonAdditional.setImageResource(drawable);
    }

    /**
     * Callbacks for changes to the query text.
     */
    public interface OnQueryTextListener {

        /**
         * Called when the user submits the query. This could be due to a key press on the
         * keyboard or due to pressing a submit button.
         * The listener can override the standard behavior by returning true
         * to indicate that it has handled the submit request. Otherwise return false to
         * let the SearchView handle the submission by launching any associated intent.
         *
         * @param query the query text that is to be submitted
         * @return true if the query has been handled by the listener, false to let the
         * SearchView perform the default action.
         */
        boolean onQueryTextSubmit(String query);

        /**
         * Called when the query text is changed by the user.
         *
         * @param newText the new content of the query text field.
         * @return false if the SearchView should perform the default action of showing any
         * suggestions if available, true if the action was handled by the listener.
         */
        boolean onQueryTextChange(String newText);
    }

    public interface OnBackButtonClickListener {
        void onBackButtonClick();
    }

    public interface OnAdditionalButtonClickListener {
        void onAdditionalButtonClick();
    }

    public interface OnAdditionalButtonLongClickListener {
        void onAdditionalButtonLongClick();
    }
}
