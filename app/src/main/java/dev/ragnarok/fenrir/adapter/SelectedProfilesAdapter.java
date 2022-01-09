package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import java.util.EventListener;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Community;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;

public class SelectedProfilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CHECK = 0;
    private static final int VIEW_TYPE_USER = 1;

    private final Context mContext;
    private final List<Owner> mData;
    private final Transformation mTransformation;
    private ActionListener mActionListener;

    public SelectedProfilesAdapter(Context context, List<Owner> data) {
        mContext = context;
        mData = data;
        mTransformation = CurrentTheme.createTransformationForAvatar();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CHECK:
                return new CheckViewHolder(LayoutInflater.from(mContext)
                        .inflate(R.layout.item_selection_check, parent, false));
            case VIEW_TYPE_USER:
                return new ProfileViewHolder(LayoutInflater.from(mContext)
                        .inflate(R.layout.item_selected_user, parent, false));

        }

        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            bindCheckViewHolder((CheckViewHolder) holder);
        } else {
            bindProfileViewHolder((ProfileViewHolder) holder, position);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_CHECK : VIEW_TYPE_USER;
    }

    private void bindCheckViewHolder(CheckViewHolder holder) {
        if (mData.isEmpty()) {
            holder.counter.setText(R.string.press_plus_for_add);
        } else {
            holder.counter.setText(String.valueOf(mData.size()));
        }

        holder.root.setOnClickListener(v -> {
            if (mActionListener != null) {
                mActionListener.onCheckClick();
            }
        });
    }

    private void bindProfileViewHolder(ProfileViewHolder holder, int adapterPosition) {
        Owner owner = mData.get(toDataPosition(adapterPosition));
        String title = null;
        String ava = null;
        if (owner instanceof User) {
            title = ((User) owner).getFirstName();
            ava = ((User) owner).getPhoto50();
        } else if (owner instanceof Community) {
            title = ((Community) owner).getName();
            ava = ((Community) owner).getPhoto50();
        }
        holder.name.setText(title);

        PicassoInstance.with()
                .load(ava)
                .transform(mTransformation)
                .into(holder.avatar);

        holder.buttonRemove.setOnClickListener(v -> {
            if (mActionListener != null) {
                mActionListener.onClick(holder.getBindingAdapterPosition(), owner);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    public void setActionListener(ActionListener actionListener) {
        mActionListener = actionListener;
    }

    public int toAdapterPosition(int dataPosition) {
        return dataPosition + 1;
    }

    public int toDataPosition(int adapterPosition) {
        return adapterPosition - 1;
    }

    public void notifyHeaderChange() {
        notifyItemChanged(0);
    }

    public interface ActionListener extends EventListener {
        void onClick(int adapterPosition, Owner owner);

        void onCheckClick();
    }

    private static class CheckViewHolder extends RecyclerView.ViewHolder {

        final TextView counter;
        final View root;

        CheckViewHolder(View itemView) {
            super(itemView);
            counter = itemView.findViewById(R.id.counter);
            root = itemView.findViewById(R.id.root);
        }
    }

    private class ProfileViewHolder extends RecyclerView.ViewHolder {

        final ImageView avatar;
        final TextView name;
        final ImageView buttonRemove;

        ProfileViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            buttonRemove = itemView.findViewById(R.id.button_remove);
            buttonRemove.getDrawable().setTint(CurrentTheme.getColorOnSurface(mContext));

            View root = itemView.findViewById(R.id.root);
            root.getBackground().setTint(CurrentTheme.getMessageBackgroundSquare(mContext));
            //root.getBackground().setAlpha(180);
        }
    }
}
