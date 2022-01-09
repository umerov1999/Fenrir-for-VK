package dev.ragnarok.fenrir.dialog;

import static dev.ragnarok.fenrir.util.RxUtils.ignore;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.MenuAdapter;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Text;
import dev.ragnarok.fenrir.model.menu.Item;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PostShareDialog extends DialogFragment {

    public static final String REQUEST_POST_SHARE = "request_post_share";
    private static final String EXTRA_METHOD = "share-method";
    private static final String EXTRA_OWNER_ID = "share-owner-id";
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private int mAccountId;
    private Post mPost;
    private MenuAdapter mAdapter;

    public static PostShareDialog newInstance(int accountId, @NonNull Post post) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.POST, post);
        PostShareDialog fragment = new PostShareDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public static int extractMethod(@NonNull Bundle data) {
        AssertUtils.assertTrue(data.containsKey(EXTRA_METHOD));
        return data.getInt(EXTRA_METHOD);
    }

    public static Post extractPost(@NonNull Bundle data) {
        return data.getParcelable(Extra.POST);
    }

    public static int extractAccountId(@NonNull Bundle data) {
        AssertUtils.assertTrue(data.containsKey(Extra.ACCOUNT_ID));
        return data.getInt(Extra.ACCOUNT_ID);
    }

    public static int extractOwnerId(@NonNull Bundle data) {
        AssertUtils.assertTrue(data.containsKey(EXTRA_OWNER_ID));
        return data.getInt(EXTRA_OWNER_ID);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        mPost = requireArguments().getParcelable(Extra.POST);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    private void onItemClick(Item item) {
        Bundle data = new Bundle();

        int method = item.getKey();
        data.putInt(Extra.ACCOUNT_ID, mAccountId);
        data.putInt(EXTRA_METHOD, method);
        data.putParcelable(Extra.POST, mPost);

        if (method == Methods.REPOST_GROUP) {
            data.putInt(EXTRA_OWNER_ID, item.getExtra());
        }
        getParentFragmentManager().setFragmentResult(REQUEST_POST_SHARE, data);
        dismissAllowingStateLoss();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        IOwnersRepository interactor = Repository.INSTANCE.getOwners();

        List<Item> items = new ArrayList<>();

        items.add(new Item(Methods.SHARE_LINK, new Text(R.string.share_link)).setIcon(R.drawable.web));
        items.add(new Item(Methods.SEND_MESSAGE, new Text(R.string.repost_send_message)).setIcon(R.drawable.share));

        boolean canRepostYourself = mPost.getOwnerId() != mAccountId && !mPost.isFriendsOnly() && mPost.getAuthorId() != mAccountId;

        if (canRepostYourself) {
            items.add(new Item(Methods.REPOST_YOURSELF, new Text(R.string.repost_to_wall)).setIcon(R.drawable.ic_outline_share));
        }

        mAdapter = new MenuAdapter(requireActivity(), items, true);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.repost_title)
                .setAdapter(mAdapter, (dialog, which) -> onItemClick(items.get(which)))
                .setNegativeButton(R.string.button_cancel, null);


        boolean iAmOwnerAndAuthor = mPost.getOwnerId() == mAccountId && mPost.getAuthorId() == mAccountId;

        // Аккуратно, сложная логика!!!
        boolean canShareToGroups = mPost.isCanRepost() || (iAmOwnerAndAuthor && !mPost.isFriendsOnly());

        if (canShareToGroups) {
            compositeDisposable.add(interactor
                    .getCommunitiesWhereAdmin(mAccountId, true, true, false)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(owners -> {
                        for (Owner owner : owners) {
                            if (owner.getOwnerId() == mPost.getOwnerId()) {
                                continue;
                            }

                            items.add(new Item(Methods.REPOST_GROUP, new Text(owner.getFullName()))
                                    .setIcon(owner.get100photoOrSmaller())
                                    .setExtra(owner.getOwnerId()));
                        }

                        mAdapter.notifyDataSetChanged();
                    }, ignore()));
        }

        return builder.create();
    }

    public static final class Methods {
        public static final int SHARE_LINK = 1;
        public static final int SEND_MESSAGE = 2;
        public static final int REPOST_YOURSELF = 3;
        public static final int REPOST_GROUP = 4;
    }
}