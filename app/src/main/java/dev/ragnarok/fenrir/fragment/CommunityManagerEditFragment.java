package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.model.Manager;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.CommunityManagerEditPresenter;
import dev.ragnarok.fenrir.mvp.view.ICommunityManagerEditView;
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;


public class CommunityManagerEditFragment extends BaseMvpFragment<CommunityManagerEditPresenter, ICommunityManagerEditView> implements ICommunityManagerEditView {

    private ImageView mAvatar;
    private OnlineView mOnlineView;
    private TextView mName;
    private TextView mDomain;
    private RadioButton mButtonModerator;
    private RadioButton mButtonEditor;
    private RadioButton mButtonAdmin;
    private CheckBox mShowAsContact;
    private View mContactInfoRoot;
    private TextInputEditText mPosition;
    private TextInputEditText mEmail;
    private TextInputEditText mPhone;
    private RadioGroup mRadioGroupRoles;
    private RadioGroup mRadioGroupCreator;
    private boolean mOptionDeleteVisible;

    public static CommunityManagerEditFragment newInstance(int accountId, int groupId, ArrayList<User> users) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        args.putParcelableArrayList(Extra.USERS, users);
        CommunityManagerEditFragment fragment = new CommunityManagerEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CommunityManagerEditFragment newInstance(int accountId, int groupId, Manager manager) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        args.putParcelable(Extra.MANAGER, manager);
        CommunityManagerEditFragment fragment = new CommunityManagerEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_manager_edit, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mAvatar = root.findViewById(R.id.avatar);
        mAvatar.setOnClickListener(v -> callPresenter(CommunityManagerEditPresenter::fireAvatarClick));

        mOnlineView = root.findViewById(R.id.online);
        mName = root.findViewById(R.id.name);
        mDomain = root.findViewById(R.id.domain);

        mButtonModerator = root.findViewById(R.id.button_moderator);
        mButtonEditor = root.findViewById(R.id.button_editor);
        mButtonAdmin = root.findViewById(R.id.button_admin);

        mRadioGroupRoles = root.findViewById(R.id.radio_group_roles);
        mRadioGroupRoles.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.button_moderator) {
                callPresenter(CommunityManagerEditPresenter::fireModeratorChecked);
            } else if (checkedId == R.id.button_editor) {
                callPresenter(CommunityManagerEditPresenter::fireEditorChecked);
            } else if (checkedId == R.id.button_admin) {
                callPresenter(CommunityManagerEditPresenter::fireAdminChecked);
            }
        });

        mRadioGroupCreator = root.findViewById(R.id.radio_group_creator);

        mShowAsContact = root.findViewById(R.id.community_manager_show_in_contacts);
        mShowAsContact.setOnCheckedChangeListener((buttonView, checked) -> callPresenter(p -> p.fireShowAsContactChecked(checked)));

        mContactInfoRoot = root.findViewById(R.id.contact_info_root);

        mPosition = root.findViewById(R.id.community_manager_positon);
        mPosition.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.firePositionEdit(s));
            }
        });

        mEmail = root.findViewById(R.id.community_manager_email);
        mEmail.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.fireEmailEdit(s));
            }
        });

        mPhone = root.findViewById(R.id.community_manager_phone);
        mPhone.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                callPresenter(p -> p.firePhoneEdit(s));
            }
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.community_manager_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            callPresenter(CommunityManagerEditPresenter::fireButtonSaveClick);
            return true;
        }

        if (item.getItemId() == R.id.action_delete) {
            callPresenter(CommunityManagerEditPresenter::fireDeleteClick);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(mOptionDeleteVisible);
    }

    @NonNull
    @Override
    public IPresenterFactory<CommunityManagerEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int groupId = requireArguments().getInt(Extra.GROUP_ID);
            ArrayList<User> users = requireArguments().getParcelableArrayList(Extra.USERS);
            Manager manager = requireArguments().getParcelable(Extra.MANAGER);

            return Objects.nonNull(manager)
                    ? new CommunityManagerEditPresenter(accountId, groupId, manager, saveInstanceState)
                    : new CommunityManagerEditPresenter(accountId, groupId, users, saveInstanceState);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.edit_manager_title);
        ActivityUtils.setToolbarSubtitle(this, R.string.editing);

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayUserInfo(User user) {
        if (Objects.nonNull(mAvatar)) {
            ViewUtils.displayAvatar(mAvatar, new RoundTransformation(), user.getMaxSquareAvatar(), null);
        }

        safelySetText(mName, user.getFullName());

        Integer iconRes = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if (Objects.nonNull(mOnlineView)) {
            mOnlineView.setVisibility(Objects.nonNull(iconRes) ? View.VISIBLE : View.INVISIBLE);

            if (Objects.nonNull(iconRes)) {
                mOnlineView.setIcon(iconRes);
            }
        }

        if (Utils.nonEmpty(user.getDomain())) {
            safelySetText(mDomain, "@" + user.getDomain());
        } else {
            safelySetText(mDomain, "@id" + user.getId());
        }
    }

    @Override
    public void showUserProfile(int accountId, User user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity());
    }

    @Override
    public void checkModerator() {
        safelySetCheched(mButtonModerator, true);
    }

    @Override
    public void checkEditor() {
        safelySetCheched(mButtonEditor, true);
    }

    @Override
    public void checkAdmin() {
        safelySetCheched(mButtonAdmin, true);
    }

    @Override
    public void setShowAsContactCheched(boolean cheched) {
        safelySetCheched(mShowAsContact, cheched);
    }

    @Override
    public void setContactInfoVisible(boolean visible) {
        safelySetVisibleOrGone(mContactInfoRoot, visible);
    }

    @Override
    public void displayPosition(String position) {
        safelySetText(mPosition, position);
    }

    @Override
    public void displayEmail(String email) {
        safelySetText(mEmail, email);
    }

    @Override
    public void displayPhone(String phone) {
        safelySetText(mPhone, phone);
    }

    @Override
    public void configRadioButtons(boolean isCreator) {
        safelySetVisibleOrGone(mRadioGroupRoles, !isCreator);
        safelySetVisibleOrGone(mRadioGroupCreator, isCreator);
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }

    @Override
    public void setDeleteOptionVisible(boolean visible) {
        mOptionDeleteVisible = visible;
        requireActivity().invalidateOptionsMenu();
    }
}
