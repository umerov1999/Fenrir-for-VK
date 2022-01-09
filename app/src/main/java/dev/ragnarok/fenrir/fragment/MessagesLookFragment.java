package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.adapter.MessagesAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.listener.EndlessRecyclerOnScrollListener;
import dev.ragnarok.fenrir.model.FwdMessages;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.LoadMoreState;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AbsMessageListPresenter;
import dev.ragnarok.fenrir.mvp.presenter.MessagesLookPresenter;
import dev.ragnarok.fenrir.mvp.view.IMessagesLookView;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.LoadMoreFooterHelper;

public class MessagesLookFragment extends PlaceSupportMvpFragment<MessagesLookPresenter, IMessagesLookView>
        implements IMessagesLookView, MessagesAdapter.OnMessageActionListener, BackPressCallback {

    private RecyclerView mRecyclerView;
    private FrameLayout toolbarRootView;
    private FrameLayout downMenuGroup;
    private MessagesAdapter mMessagesAdapter;
    private View mHeaderView;
    private View mFooterView;
    private LoadMoreFooterHelper mHeaderHelper;
    private LoadMoreFooterHelper mFooterHelper;
    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;
    private ActionModeHolder mActionView;

    public static Bundle buildArgs(int accountId, int peerId, int focusMessageId, @Nullable Message message) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.PEER_ID, peerId);
        args.putInt(Extra.FOCUS_TO, focusMessageId);
        if (nonNull(message)) {
            args.putParcelable(Extra.MESSAGE, message);
        }
        return args;
    }

    public static MessagesLookFragment newInstance(Bundle args) {
        MessagesLookFragment fragment = new MessagesLookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_messages_lookup, container, false);
        root.setBackground(CurrentTheme.getChatBackground(requireActivity()));
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, true);

        mRecyclerView = root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(layoutManager);

        mHeaderView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mFooterView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mHeaderHelper = LoadMoreFooterHelper.createFrom(mHeaderView, this::onHeaderLoadMoreClick);
        mFooterHelper = LoadMoreFooterHelper.createFrom(mFooterView, this::onFooterLoadMoreClick);
        downMenuGroup = root.findViewById(R.id.down_menu);
        toolbarRootView = root.findViewById(R.id.toolbar_root);

        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                onFooterLoadMoreClick();
            }

            @Override
            public void onScrollToFirstElement() {
                onHeaderLoadMoreClick();
            }
        };

        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
        return root;
    }

    @Override
    public void showDeleteForAllDialog(ArrayList<Integer> ids) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.messages_delete_for_all_question_message)
                .setNeutralButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_for_all, (dialog, which) -> callPresenter(p -> p.fireDeleteForAllClick(ids)))
                .setNegativeButton(R.string.button_for_me, (dialog, which) -> callPresenter(p -> p.fireDeleteForMeClick(ids)))
                .show();
    }

    private void onFooterLoadMoreClick() {
        callPresenter(MessagesLookPresenter::fireFooterLoadMoreClick);
    }

    private void onHeaderLoadMoreClick() {
        callPresenter(MessagesLookPresenter::fireHeaderLoadMoreClick);
    }

    @Override
    public void displayMessages(@NonNull List<Message> messages, @NonNull LastReadId lastReadId) {
        mMessagesAdapter = new MessagesAdapter(requireActivity(), messages, lastReadId, this, true);
        mMessagesAdapter.setOnMessageActionListener(this);
        mMessagesAdapter.addFooter(mFooterView);
        mMessagesAdapter.addHeader(mHeaderView);
        mRecyclerView.setAdapter(mMessagesAdapter);
    }

    @Override
    public void focusTo(int index) {
        mRecyclerView.removeOnScrollListener(mEndlessRecyclerOnScrollListener);
        mRecyclerView.scrollToPosition(index + 1); // +header
        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    @Override
    public void notifyMessagesUpAdded(int startPosition, int count) {
        if (nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyItemRangeInserted(startPosition + 1, count); //+header
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {
        if (nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyItemRemoved(0);
            mMessagesAdapter.notifyItemRangeInserted(0, count + 1); //+header
        }
    }

    @Override
    public void configNowVoiceMessagePlaying(int id, float progress, boolean paused, boolean amin, boolean speed) {
        mMessagesAdapter.configNowVoiceMessagePlaying(id, progress, paused, amin, speed);
    }

    @Override
    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin, boolean speed) {
        mMessagesAdapter.bindVoiceHolderById(holderId, play, paused, progress, amin, speed);
    }

    @Override
    public void disableVoicePlaying() {
        mMessagesAdapter.disableVoiceMessagePlaying();
    }

    @Override
    public void showActionMode(String title, Boolean canEdit, Boolean canPin, Boolean canStar, Boolean doStar, Boolean canSpam) {
        boolean isDown = Settings.get().main().isMessages_menu_down();
        if (isDown ? downMenuGroup == null : toolbarRootView == null) {
            return;
        }
        if (!isDown) {
            if (toolbarRootView.getChildCount() == Constants.FRAGMENT_CHAT_APP_BAR_VIEW_COUNT) {
                mActionView = new ActionModeHolder(LayoutInflater.from(requireActivity()).inflate(R.layout.view_action_mode, toolbarRootView, false), this);
                toolbarRootView.addView(mActionView.rootView);
            }
        } else {
            downMenuGroup.setVisibility(View.VISIBLE);
            if (downMenuGroup.getChildCount() == Constants.FRAGMENT_CHAT_DOWN_MENU_VIEW_COUNT) {
                mActionView = new ActionModeHolder(LayoutInflater.from(requireActivity()).inflate(R.layout.view_action_mode, downMenuGroup, false), this);
                downMenuGroup.addView(mActionView.rootView);
            }
        }

        mActionView.show();
        mActionView.titleView.setText(title);
        mActionView.buttonSpam.setVisibility(canSpam ? View.VISIBLE : View.GONE);
        mActionView.buttonEdit.setVisibility(canEdit ? View.VISIBLE : View.GONE);
        mActionView.buttonPin.setVisibility(canPin ? View.VISIBLE : View.GONE);
        mActionView.buttonStar.setVisibility(canStar ? View.VISIBLE : View.GONE);
        mActionView.buttonStar.setImageResource(doStar ? R.drawable.star_add : R.drawable.star_none);
    }

    @Override
    public void finishActionMode() {
        if (nonNull(mActionView)) {
            mActionView.hide();
        }
        if (Settings.get().main().isMessages_menu_down() && nonNull(downMenuGroup)) {
            downMenuGroup.setVisibility(View.GONE);
        }
    }

    @Override
    public void notifyDataChanged() {
        if (nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mMessagesAdapter)) {
            mMessagesAdapter.notifyItemBindableChanged(index);
        }
    }

    @Override
    public void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState) {
        if (nonNull(mFooterHelper)) {
            mFooterHelper.switchToState(upHeaderState);
        }

        if (nonNull(mHeaderHelper)) {
            mHeaderHelper.switchToState(downHeaderState);
        }
    }

    @Override
    public void forwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, new FwdMessages(messages));
    }

    @NonNull
    @Override
    public IPresenterFactory<MessagesLookPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = requireArguments().getInt(Extra.PEER_ID);
            int focusTo = requireArguments().getInt(Extra.FOCUS_TO);
            Message message = requireArguments().containsKey(Extra.MESSAGE) ? requireArguments().getParcelable(Extra.MESSAGE) : null;
            return new MessagesLookPresenter(aid, peerId, focusTo, message, saveInstanceState);
        };
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId, int position) {
        if (nonNull(mActionView) && mActionView.isVisible()) {
            callPresenter(p -> p.fireMessageClick(message, position));
        } else {
            callPresenter(p -> p.fireOwnerClick(userId));
        }
    }

    @Override
    public void onLongAvatarClick(@NonNull Message message, int userId, int position) {
        if (nonNull(mActionView) && mActionView.isVisible()) {
            callPresenter(p -> p.fireMessageClick(message, position));
        } else {
            callPresenter(p -> p.fireOwnerClick(userId));
        }
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        callPresenter(p -> p.fireMessageRestoreClick(message, position));
    }

    @Override
    public void onBotKeyboardClick(@NonNull Keyboard.Button button) {

    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message, int position) {
        callPresenter(p -> p.fireMessageLongClick(message, position));
        return true;
    }

    @Override
    public void onMessageClicked(@NonNull Message message, int position) {
        callPresenter(p -> p.fireMessageClick(message, position));
    }

    @Override
    public void onMessageDelete(@NonNull Message message) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(message.getId());
        callPresenter(p -> p.fireDeleteForMeClick(ids));
    }

    @Override
    public boolean onBackPressed() {
        if (nonNull(mActionView) && mActionView.isVisible()) {
            mActionView.hide();
            return false;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(R.string.viewing_messages);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    class ActionModeHolder implements View.OnClickListener {
        public View buttonClose;
        public View rootView;
        public View buttonEdit;
        public View buttonForward;
        public View buttonCopy;
        public View buttonDelete;
        public View buttonPin;
        public View buttonSpam;
        public ImageView buttonStar;
        public TextView titleView;
        WeakReference<MessagesLookFragment> reference;

        public ActionModeHolder(View rootView, MessagesLookFragment fragment) {
            this.rootView = rootView;
            reference = new WeakReference<>(fragment);
            buttonClose = rootView.findViewById(R.id.buttonClose);
            buttonEdit = rootView.findViewById(R.id.buttonEdit);
            buttonForward = rootView.findViewById(R.id.buttonForward);
            buttonCopy = rootView.findViewById(R.id.buttonCopy);
            buttonDelete = rootView.findViewById(R.id.buttonDelete);
            buttonPin = rootView.findViewById(R.id.buttonPin);
            buttonStar = rootView.findViewById(R.id.buttonStar);
            titleView = rootView.findViewById(R.id.actionModeTitle);
            buttonSpam = rootView.findViewById(R.id.buttonSpam);

            buttonClose.setOnClickListener(this);
            buttonEdit.setOnClickListener(this);
            buttonForward.setOnClickListener(this);
            buttonCopy.setOnClickListener(this);
            buttonDelete.setOnClickListener(this);
            buttonPin.setOnClickListener(this);
            buttonStar.setOnClickListener(this);
            buttonSpam.setOnClickListener(this);
        }

        public void show() {
            rootView.setVisibility(View.VISIBLE);
        }

        public boolean isVisible() {
            return rootView.getVisibility() == View.VISIBLE;
        }

        public void hide() {
            rootView.setVisibility(View.GONE);
            if (Settings.get().main().isMessages_menu_down()) {
                Utils.safeObjectCall(reference.get(), () -> {
                    if (reference.get().downMenuGroup != null) {
                        reference.get().downMenuGroup.setVisibility(View.GONE);
                    }
                });
            }
            Utils.safeObjectCall(reference.get(), () -> reference.get().callPresenter(AbsMessageListPresenter::fireActionModeDestroy));
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.buttonClose) {
                hide();
            } else if (v.getId() == R.id.buttonForward) {
                Utils.safeObjectCall(reference.get(), () -> reference.get().callPresenter(AbsMessageListPresenter::fireForwardClick));
                hide();
            } else if (v.getId() == R.id.buttonCopy) {
                Utils.safeObjectCall(reference.get(), () -> reference.get().callPresenter(AbsMessageListPresenter::fireActionModeCopyClick));
                hide();
            } else if (v.getId() == R.id.buttonDelete) {
                Utils.safeObjectCall(reference.get(), () -> reference.get().callPresenter(AbsMessageListPresenter::fireActionModeDeleteClick));
                hide();
            } else if (v.getId() == R.id.buttonSpam) {
                new MaterialAlertDialogBuilder(requireActivity())
                        .setIcon(R.drawable.report_red)
                        .setMessage(R.string.do_report)
                        .setTitle(R.string.select)
                        .setPositiveButton(R.string.button_yes, (dialog, which) -> {
                            Utils.safeObjectCall(reference.get(), () -> reference.get().callPresenter(AbsMessageListPresenter::fireActionModeSpamClick));
                            hide();
                        })
                        .setNeutralButton(R.string.delete, (dialog, which) -> {
                            Utils.safeObjectCall(reference.get(), () -> reference.get().callPresenter(AbsMessageListPresenter::fireActionModeDeleteClick));
                            hide();
                        })
                        .setCancelable(true)
                        .show();
            }
        }
    }
}
