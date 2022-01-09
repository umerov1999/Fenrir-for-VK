package dev.ragnarok.fenrir.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.SelectProfilesActivity;
import dev.ragnarok.fenrir.adapter.PrivacyAdapter;
import dev.ragnarok.fenrir.dialog.base.AccountDependencyDialogFragment;
import dev.ragnarok.fenrir.model.FriendList;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Privacy;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.util.AssertUtils;

public class PrivacyViewFragment extends AccountDependencyDialogFragment implements PrivacyAdapter.ActionListener {
    public static final String REQUEST_PRIVACY_VIEW = "request_privacy_view";
    private static final String SAVE_PRIVACY = "save_privacy";
    private Privacy mPrivacy;
    private PrivacyAdapter mAdapter;
    private final ActivityResultLauncher<Intent> requestSelectUsersAllowed = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<Owner> users = result.getData().getParcelableArrayListExtra(Extra.OWNERS);
                    AssertUtils.requireNonNull(users);

                    for (Owner user : users) {
                        if (user instanceof User) {
                            mPrivacy.allowFor((User) user);
                        }
                    }
                    safeNotifyDatasetChanged();
                }
            });

    private final ActivityResultLauncher<Intent> requestSelectUsersDisAllowed = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    ArrayList<Owner> users = result.getData().getParcelableArrayListExtra(Extra.OWNERS);
                    AssertUtils.requireNonNull(users);

                    for (Owner user : users) {
                        if (user instanceof User) {
                            mPrivacy.disallowFor((User) user);
                        }
                    }
                    safeNotifyDatasetChanged();
                }
            });

    public static Bundle buildArgs(int aid, Privacy privacy) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.PRIVACY, privacy);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static PrivacyViewFragment newInstance(Bundle args) {
        PrivacyViewFragment fragment = new PrivacyViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mPrivacy = savedInstanceState.getParcelable(SAVE_PRIVACY);
        }

        if (mPrivacy == null) {
            mPrivacy = clonePrivacyFromArgs();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = View.inflate(requireActivity(), R.layout.fragment_privacy_view, null);

        int columns = getResources().getInteger(R.integer.privacy_entry_column_count);

        mAdapter = new PrivacyAdapter(requireActivity(), mPrivacy);
        mAdapter.setActionListener(this);

        RecyclerView recyclerView = root.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(mAdapter);

        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.privacy_settings)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> returnResult())
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void returnResult() {
        Bundle intent = new Bundle();
        intent.putParcelable(Extra.PRIVACY, mPrivacy);
        getParentFragmentManager().setFragmentResult(REQUEST_PRIVACY_VIEW, intent);
    }

    @Override
    public void onTypeClick() {
        String[] items = {
                getString(R.string.privacy_to_all_users),
                getString(R.string.privacy_to_friends_only),
                getString(R.string.privacy_to_friends_and_friends_of_friends),
                getString(R.string.privacy_to_only_me)
        };

        new MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            mPrivacy.setType(Privacy.Type.ALL);
                            break;
                        case 1:
                            mPrivacy.setType(Privacy.Type.FRIENDS);
                            break;
                        case 2:
                            mPrivacy.setType(Privacy.Type.FRIENDS_OF_FRIENDS);
                            break;
                        case 3:
                            mPrivacy.setType(Privacy.Type.ONLY_ME);
                            break;
                    }

                    safeNotifyDatasetChanged();
                }).setNegativeButton(R.string.button_cancel, null).show();
    }

    private void safeNotifyDatasetChanged() {
        if (isAdded() && mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAllowedUserRemove(User user) {
        mPrivacy.removeFromAllowed(user);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onAllowedFriendsListRemove(FriendList friendList) {
        mPrivacy.removeFromAllowed(friendList);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onDisallowedUserRemove(User user) {
        mPrivacy.removeFromDisallowed(user);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onDisallowedFriendsListRemove(FriendList friendList) {
        mPrivacy.removeFromDisallowed(friendList);
        safeNotifyDatasetChanged();
    }

    @Override
    public void onAddToAllowedClick() {
        requestSelectUsersAllowed.launch(SelectProfilesActivity.startFriendsSelection(requireActivity()));
    }

    @Override
    public void onAddToDisallowedClick() {
        requestSelectUsersDisAllowed.launch(SelectProfilesActivity.startFriendsSelection(requireActivity()));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVE_PRIVACY, mPrivacy);
    }

    private Privacy clonePrivacyFromArgs() {
        Privacy privacy = requireArguments().getParcelable(Extra.PRIVACY);
        if (privacy == null) {
            throw new IllegalArgumentException("Args do not contain Privacy extra");
        }

        try {
            return privacy.clone();
        } catch (CloneNotSupportedException e) {
            return privacy;
        }
    }
}
