package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder;
import dev.ragnarok.fenrir.adapter.MessagesAdapter;
import dev.ragnarok.fenrir.fragment.base.PlaceSupportMvpFragment;
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.model.LastReadId;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.AbsMessageListPresenter;
import dev.ragnarok.fenrir.mvp.presenter.FwdsPresenter;
import dev.ragnarok.fenrir.mvp.view.IFwdsView;

public class FwdsFragment extends PlaceSupportMvpFragment<FwdsPresenter, IFwdsView>
        implements MessagesAdapter.OnMessageActionListener, IFwdsView, AttachmentsViewBinder.VoiceActionListener {

    private MessagesAdapter mAdapter;

    public static Bundle buildArgs(int accountId, ArrayList<Message> messages) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelableArrayList(Extra.MESSAGES, messages);
        return args;
    }

    public static FwdsFragment newInstance(Bundle args) {
        FwdsFragment fwdsFragment = new FwdsFragment();
        fwdsFragment.setArguments(args);
        return fwdsFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fwds, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAdapter = new MessagesAdapter(requireActivity(), Collections.emptyList(), this, true);
        mAdapter.setOnMessageActionListener(this);
        mAdapter.setVoiceActionListener(this);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setSubtitle(null);
            actionBar.setTitle(R.string.title_messages);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId, int position) {
        onOpenOwner(userId);
    }

    @Override
    public void onLongAvatarClick(@NonNull Message message, int userId, int position) {
        onOpenOwner(userId);
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        // not supported
    }

    @Override
    public void onBotKeyboardClick(@NonNull Keyboard.Button button) {
        // not supported
    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message, int position) {
        // not supported
        return false;
    }

    @Override
    public void onMessageClicked(@NonNull Message message, int position) {
        // not supported
    }

    @Override
    public void onMessageDelete(@NonNull Message message) {

    }

    @Override
    public void displayMessages(@NonNull List<Message> messages, @NonNull LastReadId lastReadId) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(messages, lastReadId);
        }
    }

    @Override
    public void notifyMessagesUpAdded(int position, int count) {
        // not supported
    }

    @Override
    public void notifyDataChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {
        // not supported
    }

    @Override
    public void configNowVoiceMessagePlaying(int voiceId, float progress, boolean paused, boolean amin, boolean speed) {
        if (nonNull(mAdapter)) {
            mAdapter.configNowVoiceMessagePlaying(voiceId, progress, paused, amin, speed);
        }
    }

    @Override
    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin, boolean speed) {
        if (nonNull(mAdapter)) {
            mAdapter.bindVoiceHolderById(holderId, play, paused, progress, amin, speed);
        }
    }

    @Override
    public void disableVoicePlaying() {
        if (nonNull(mAdapter)) {
            mAdapter.disableVoiceMessagePlaying();
        }
    }

    @Override
    public void showActionMode(String title, Boolean canEdit, Boolean canPin, Boolean canStar, Boolean doStar, Boolean canSpam) {
        // not supported
    }

    @Override
    public void finishActionMode() {
        // not supported
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemBindableChanged(index);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<FwdsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            ArrayList<Message> messages = requireArguments().getParcelableArrayList(Extra.MESSAGES);
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            return new FwdsPresenter(accountId, messages, saveInstanceState);
        };
    }

    @Override
    public void onVoiceHolderBinded(int voiceMessageId, int voiceHolderId) {
        callPresenter(p -> p.fireVoiceHolderCreated(voiceMessageId, voiceHolderId));
    }

    @Override
    public void onVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage) {
        callPresenter(p -> p.fireVoicePlayButtonClick(voiceHolderId, voiceMessageId, voiceMessage));
    }

    @Override
    public void onVoiceTogglePlaybackSpeed() {
        callPresenter(AbsMessageListPresenter::fireVoicePlaybackSpeed);
    }

    @Override
    public void onTranscript(String voiceMessageId, int messageId) {
        callPresenter(p -> p.fireTranscript(voiceMessageId, messageId));
    }
}
