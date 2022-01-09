package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

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

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.AnswerVKOfficialAdapter;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.AnswerVKOfficial;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AnswerVKOfficialPresenter;
import dev.ragnarok.fenrir.mvp.view.IAnswerVKOfficialView;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.ViewUtils;

public class AnswerVKOfficialFragment extends BaseMvpFragment<AnswerVKOfficialPresenter, IAnswerVKOfficialView> implements SwipeRefreshLayout.OnRefreshListener, IAnswerVKOfficialView, AnswerVKOfficialAdapter.ClickListener {

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AnswerVKOfficialAdapter mAdapter;

    public static AnswerVKOfficialFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AnswerVKOfficialFragment fragment = new AnswerVKOfficialFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_feedback, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mEmpty = root.findViewById(R.id.fragment_feedback_empty_text);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity());
        RecyclerView recyclerView = root.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                callPresenter(AnswerVKOfficialPresenter::fireScrollToEnd);
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new AnswerVKOfficialAdapter(null, requireActivity());
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
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.NOTIFICATIONS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.drawer_feedback);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AbsNavigationFragment.SECTION_ITEM_FEEDBACK);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayData(AnswerVKOfficialList users) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(users);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyFirstListReceived() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).readAllNotifications();
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
    public void openOwnerWall(int owner_id) {
        PlaceFactory.getOwnerWallPlace(Settings.get().accounts().getCurrent(), owner_id, null).tryOpenWith(requireActivity());
    }

    @Override
    public void openAction(@NonNull AnswerVKOfficial.Action action) {
        if (action.getType() == AnswerVKOfficial.Action_Types.URL) {
            LinkHelper.openLinkInBrowser(requireActivity(), ((AnswerVKOfficial.ActionURL) action).url);
        } else if (action.getType() == AnswerVKOfficial.Action_Types.MESSAGE) {
            AnswerVKOfficial.ActionMessage msg = (AnswerVKOfficial.ActionMessage) action;
            PlaceFactory.getMessagesLookupPlace(Settings.get().accounts().getCurrent(), msg.peerId, msg.messageId, null).tryOpenWith(requireActivity());
        }
    }

    @Override
    public void onRefresh() {
        callPresenter(AnswerVKOfficialPresenter::fireRefresh);
    }

    @NonNull
    @Override
    public IPresenterFactory<AnswerVKOfficialPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AnswerVKOfficialPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }
}
