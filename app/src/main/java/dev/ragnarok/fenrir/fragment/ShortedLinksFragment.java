package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.ShortedLinksAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.model.ShortLink;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.ShortedLinksPresenter;
import dev.ragnarok.fenrir.mvp.view.IShortedLinksView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class ShortedLinksFragment extends BaseMvpFragment<ShortedLinksPresenter, IShortedLinksView> implements IShortedLinksView, ShortedLinksAdapter.ClickListener {

    private TextView mEmpty;
    private TextInputEditText mLink;
    private MaterialButton do_Short;
    private MaterialButton do_Validate;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ShortedLinksAdapter mAdapter;

    public static ShortedLinksFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        ShortedLinksFragment fragment = new ShortedLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shorted_links, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mEmpty = root.findViewById(R.id.fragment_shorted_links_empty_text);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity());
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(ShortedLinksPresenter::fireScrollToEnd);
            }
        });

        mLink = root.findViewById(R.id.input_url);
        do_Short = root.findViewById(R.id.do_short);
        do_Validate = root.findViewById(R.id.do_validate);

        do_Short.setEnabled(false);
        do_Validate.setEnabled(false);
        mLink.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                do_Validate.setEnabled(!Utils.isEmpty(s));
                do_Short.setEnabled(!Utils.isEmpty(s));
                callPresenter(p -> p.fireInputEdit(s));
            }
        });

        do_Short.setOnClickListener(v -> callPresenter(ShortedLinksPresenter::fireShort));
        do_Validate.setOnClickListener(v -> callPresenter(ShortedLinksPresenter::fireValidate));

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> callPresenter(ShortedLinksPresenter::fireRefresh));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new ShortedLinksAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);

        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayData(List<ShortLink> links) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(links);
            resolveEmptyText();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.DIALOGS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.short_link);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_DIALOGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void updateLink(String url) {
        mLink.setText(url);
        mLink.setSelection(mLink.getText().length());
        do_Short.setEnabled(false);
        do_Validate.setEnabled(false);
    }

    @Override
    public void showLinkStatus(String status) {
        String stat = "";
        int color = Color.parseColor("#ff0000");

        switch (status) {
            case "not_banned":
                stat = getString(R.string.link_not_banned);
                color = Color.parseColor("#cc00aa00");
                break;
            case "banned":
                stat = getString(R.string.link_banned);
                color = Color.parseColor("#ccaa0000");
                break;
            case "processing":
                stat = getString(R.string.link_processing);
                color = Color.parseColor("#cc0000aa");
                break;
        }
        int text_color = Utils.isColorDark(color)
                ? Color.parseColor("#ffffff") : Color.parseColor("#000000");

        Snackbar.make(mLink, stat, BaseTransientBottomBar.LENGTH_LONG)
                .setBackgroundTint(color).setTextColor(text_color).setAnchorView(R.id.recycler_view).show();
    }

    @NonNull
    @Override
    public IPresenterFactory<ShortedLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ShortedLinksPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onCopy(int index, ShortLink link) {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("response", link.getShort_url());
        clipboard.setPrimaryClip(clip);
        CustomToast.CreateCustomToast(getContext()).showToast(R.string.copied);
    }

    @Override
    public void onDelete(int index, ShortLink link) {
        callPresenter(p -> p.fireDelete(index, link));
    }
}
