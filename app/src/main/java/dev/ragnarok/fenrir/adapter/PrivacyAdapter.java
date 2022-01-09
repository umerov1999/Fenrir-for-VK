package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.Collection;
import java.util.EventListener;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.FriendList;
import dev.ragnarok.fenrir.model.Privacy;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;

public class PrivacyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_ENTRY = 0;
    private static final int TYPE_TITLE = 1;

    private final Context mContext;
    private final Privacy mPrivacy;
    private RecyclerView.LayoutManager mLayoutManager;
    private ActionListener mActionListener;

    public PrivacyAdapter(Context context, Privacy privacy) {
        mContext = context;
        mPrivacy = privacy;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case TYPE_ENTRY:
                return new EntryViewHolder(inflater.inflate(R.layout.item_privacy_entry, parent, false));
            case TYPE_TITLE:
                return new TitleViewHolder(inflater.inflate(R.layout.item_privacy_title, parent, false));
        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            bindTitle((TitleViewHolder) holder);
            return;
        }

        if (position <= mPrivacy.getAllowedUsers().size()) {
            int allowedUserIndex = position - 1;
            bindUserEntry((EntryViewHolder) holder, mPrivacy.getAllowedUsers().get(allowedUserIndex), true);
            return;
        }

        if (position <= count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists())) {
            int allowedListIndex = position - count(mPrivacy.getAllowedUsers()) - 1;
            bindListEntry((EntryViewHolder) holder, mPrivacy.getAllowedLists().get(allowedListIndex), true);
            return;
        }

        if (position <= count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists(), mPrivacy.getDisallowedUsers()) + 1) {
            int excludedUserIndex = position - count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists()) - 2;
            bindUserEntry((EntryViewHolder) holder, mPrivacy.getDisallowedUsers().get(excludedUserIndex), false);
            return;
        }

        int excludedListIndex = position - count(
                mPrivacy.getAllowedUsers(),
                mPrivacy.getAllowedLists(),
                mPrivacy.getDisallowedUsers()) - 2;

        bindListEntry((EntryViewHolder) holder, mPrivacy.getDisallowedLists().get(excludedListIndex), false);
    }

    private int count(Collection<?>... collection) {
        return Utils.safeCountOfMultiple(collection);
    }

    private void bindTitle(TitleViewHolder holder) {
        if (mLayoutManager instanceof StaggeredGridLayoutManager) {
            StaggeredGridLayoutManager.LayoutParams layoutParams = new StaggeredGridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setFullSpan(true);
            holder.itemView.setLayoutParams(layoutParams);
        }

        int position = holder.getBindingAdapterPosition();
        if (position == 0) {
            String title = mContext.getString(getTypeTitle());
            String fullText = mContext.getString(R.string.who_can_have_access) + " " + title;
            Spannable spannable = SpannableStringBuilder.valueOf(fullText);
            ClickableSpan span = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (mActionListener != null) {
                        mActionListener.onTypeClick();
                    }
                }
            };

            spannable.setSpan(span, fullText.length() - title.length(), fullText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.title.setText(spannable, TextView.BufferType.SPANNABLE);
        } else {
            holder.title.setText(R.string.who_cannot_have_access);
        }

        holder.buttonAdd.setOnClickListener(v -> {
            if (mActionListener != null) {
                if (position == 0) {
                    mActionListener.onAddToAllowedClick();
                } else {
                    mActionListener.onAddToDisallowedClick();
                }
            }
        });
    }

    private int getTypeTitle() {
        switch (mPrivacy.getType()) {
            default:
                return R.string.privacy_to_all_users;
            case Privacy.Type.FRIENDS:
                return R.string.privacy_to_friends_only;
            case Privacy.Type.FRIENDS_OF_FRIENDS:
            case Privacy.Type.FRIENDS_OF_FRIENDS_ONLY:
                return R.string.privacy_to_friends_and_friends_of_friends;
            case Privacy.Type.ONLY_ME:
            case Privacy.Type.NOBODY:
                return R.string.privacy_to_only_me;
        }
    }

    private void bindListEntry(EntryViewHolder holder, FriendList friendList, boolean allow) {
        holder.avatar.setColorFilter(CurrentTheme.getColorSecondary(mContext));

        PicassoInstance.with()
                .load(R.drawable.ic_privacy_friends_list)
                .into(holder.avatar);

        holder.title.setText(friendList.getName());
        holder.buttonRemove.setOnClickListener(v -> {
            if (mActionListener != null) {
                if (allow) {
                    mActionListener.onAllowedFriendsListRemove(friendList);
                } else {
                    mActionListener.onDisallowedFriendsListRemove(friendList);
                }
            }
        });
    }

    private void bindUserEntry(EntryViewHolder holder, User user, boolean allow) {
        holder.avatar.setColorFilter(null);

        PicassoInstance.with()
                .load(user.getMaxSquareAvatar())
                .into(holder.avatar);
        holder.title.setText(user.getFullName());
        holder.buttonRemove.setOnClickListener(v -> {
            if (mActionListener != null) {
                if (allow) {
                    mActionListener.onAllowedUserRemove(user);
                } else {
                    mActionListener.onDisallowedUserRemove(user);
                }
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mLayoutManager = recyclerView.getLayoutManager();
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mLayoutManager = null;
    }

    @Override
    public int getItemCount() {
        // 2 titles
        return 2 + count(mPrivacy.getAllowedUsers(),
                mPrivacy.getAllowedLists(),
                mPrivacy.getDisallowedUsers(),
                mPrivacy.getDisallowedLists());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_TITLE;
        }

        if (position == count(mPrivacy.getAllowedUsers(), mPrivacy.getAllowedLists()) + 1) {
            return TYPE_TITLE;
        }

        return TYPE_ENTRY;
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public interface ActionListener extends EventListener {
        void onTypeClick();

        void onAllowedUserRemove(User user);

        void onAllowedFriendsListRemove(FriendList friendList);

        void onDisallowedUserRemove(User user);

        void onDisallowedFriendsListRemove(FriendList friendList);

        void onAddToAllowedClick();

        void onAddToDisallowedClick();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final View buttonAdd;

        TitleViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            title.setMovementMethod(LinkMovementMethod.getInstance());
            buttonAdd = itemView.findViewById(R.id.button_add);
        }
    }

    static class EntryViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final View buttonRemove;
        final TextView title;

        EntryViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            buttonRemove = itemView.findViewById(R.id.button_remove);
            title = itemView.findViewById(R.id.name);
        }
    }
}
