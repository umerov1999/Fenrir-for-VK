package dev.ragnarok.fenrir.adapter;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.UserInfoResolveUtil;
import dev.ragnarok.fenrir.model.Account;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class AccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int DATA_TYPE_NORMAL = 0;
    private static final int DATA_TYPE_HIDDEN = 1;
    private static final int STATUS_COLOR_OFFLINE = Color.parseColor("#999999");
    private final Context context;
    private final List<Account> data;
    private final Transformation transformation;
    private final Callback callback;
    private final boolean showHidden;

    public AccountAdapter(Context context, List<Account> items, Callback callback) {
        this.context = context;
        data = items;
        transformation = CurrentTheme.createTransformationForAvatar();
        this.callback = callback;
        showHidden = Settings.get().security().IsShow_hidden_accounts();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case DATA_TYPE_HIDDEN:
                return new HiddenViewHolder(LayoutInflater.from(context).inflate(R.layout.line_hidden, parent, false));
            case DATA_TYPE_NORMAL:
                return new Holder(LayoutInflater.from(context).inflate(R.layout.item_account, parent, false));
        }

        throw new UnsupportedOperationException();
    }

    protected int getDataTypeByAdapterPosition(int adapterPosition) {
        if (Utils.isHiddenAccount(getByPosition(adapterPosition).getId()) && !showHidden) {
            return DATA_TYPE_HIDDEN;
        } else return DATA_TYPE_NORMAL;
    }

    @Override
    public int getItemViewType(int adapterPosition) {
        return getDataTypeByAdapterPosition(adapterPosition);
    }

    @NonNull
    public Account getByPosition(int position) {
        return data.get(position);
    }

    public boolean checkPosition(int position) {
        return position >= 0 && data.size() > position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder dualHolder, int position) {
        if (getDataTypeByAdapterPosition(position) == DATA_TYPE_HIDDEN) {
            return;
        }
        Holder holder = (Holder) dualHolder;
        Account account = getByPosition(position);

        Owner owner = account.getOwner();

        if (Objects.isNull(owner)) {
            holder.firstName.setText(String.valueOf(account.getId()));
            ViewUtils.displayAvatar(holder.avatar, transformation, null, Constants.PICASSO_TAG);
        } else {
            holder.firstName.setText(owner.getFullName());
            ViewUtils.displayAvatar(holder.avatar, transformation, owner.getMaxSquareAvatar(), Constants.PICASSO_TAG);
        }

        if (owner instanceof User) {
            User user = (User) owner;
            boolean online = user.isOnline();
            if (nonEmpty(user.getDomain())) {
                holder.domain.setText("@" + user.getDomain());
            } else {
                holder.domain.setText("@id" + account.getId());
            }
            holder.tvLastTime.setVisibility(View.VISIBLE);
            holder.tvLastTime.setText(UserInfoResolveUtil.getUserActivityLine(context, user, false));
            holder.tvLastTime.setTextColor(user.isOnline() ? CurrentTheme.getColorPrimary(context) : STATUS_COLOR_OFFLINE);
            Integer iconRes = ViewUtils.getOnlineIcon(online, user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
            holder.vOnline.setIcon(iconRes != null ? iconRes : 0);
            holder.vOnline.setVisibility(online ? View.VISIBLE : View.GONE);
        } else {
            holder.domain.setText("club" + Math.abs(account.getId()));
            holder.tvLastTime.setVisibility(View.GONE);
            holder.vOnline.setVisibility(View.GONE);
        }

        boolean isCurrent = account.getId() == Settings.get()
                .accounts()
                .getCurrent();

        holder.active.setVisibility(isCurrent ? View.VISIBLE : View.INVISIBLE);
        if (Utils.hasMarshmallow() && FenrirNative.isNativeLoaded()) {
            if (isCurrent) {
                holder.active.fromRes(R.raw.select_check_box, Utils.dp(40), Utils.dp(40), new int[]{0x333333, CurrentTheme.getColorPrimary(context), 0x777777, CurrentTheme.getColorSecondary(context)});
                holder.active.playAnimation();
            } else {
                holder.active.clearAnimationDrawable();
            }
        } else {
            if (isCurrent) {
                holder.active.setImageResource(R.drawable.check);
            }
        }
        holder.account.setOnClickListener(v -> callback.onClick(account));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface Callback {
        void onClick(Account account);
    }

    private static class HiddenViewHolder extends RecyclerView.ViewHolder {
        HiddenViewHolder(View view) {
            super(view);
        }
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final TextView firstName;
        final TextView domain;
        final OnlineView vOnline;
        final TextView tvLastTime;
        final ImageView avatar;
        final RLottieImageView active;
        final View account;

        public Holder(View itemView) {
            super(itemView);
            firstName = itemView.findViewById(R.id.first_name);
            domain = itemView.findViewById(R.id.domain);
            avatar = itemView.findViewById(R.id.avatar);
            active = itemView.findViewById(R.id.active);
            account = itemView.findViewById(R.id.account_select);
            vOnline = itemView.findViewById(R.id.item_user_online);
            tvLastTime = itemView.findViewById(R.id.last_time);
        }
    }
}
