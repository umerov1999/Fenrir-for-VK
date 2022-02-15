package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.module.FenrirNative;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemeValue;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class ThemeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_THEME = 0;
    private static final int TYPE_SPECIAL = 1;
    private final boolean isDark;
    private List<ThemeValue> data;
    private ClickListener clickListener;
    private String currentId;

    public ThemeAdapter(List<ThemeValue> data, Context context) {
        this.data = data;
        currentId = Settings.get().ui().getMainThemeKey();
        isDark = Settings.get().ui().isDarkModeEnabled(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_THEME:
                return new ThemeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false));
            case TYPE_SPECIAL:
                return new SpecialThemeHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_special_theme, parent, false));
        }
        throw new RuntimeException("ThemeAdapter.onCreateViewHolder");
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getSpecial() ? TYPE_SPECIAL : TYPE_THEME;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case TYPE_THEME:
                bindThemeHolder((ThemeHolder) holder, position);
                break;
            case TYPE_SPECIAL:
                bindSpecialHolder((SpecialThemeHolder) holder, position);
                break;
        }
    }

    private void bindSpecialHolder(@NonNull SpecialThemeHolder holder, int position) {
        ThemeValue category = data.get(position);
        holder.itemView.setAlpha(category.getDisabled() ? 0.55f : 1.0f);
        boolean isSelected = currentId.equals(category.getId());

        holder.title.setText(category.getDisabled() ? holder.itemView.getContext().getString(R.string.not_available) : category.getName());
        if (!Utils.isEmpty(category.getName())) {
            holder.special_title.setVisibility(View.VISIBLE);
            String name = category.getName();
            if (name.length() > 4)
                name = name.substring(0, 4);
            name = name.trim();
            holder.special_title.setText(name);
            holder.special_title.setTextColor(position % 2 == 0 ? CurrentTheme.getColorPrimary(holder.itemView.getContext()) : CurrentTheme.getColorSecondary(holder.itemView.getContext()));
        } else {
            holder.special_title.setVisibility(View.INVISIBLE);
        }
        holder.selected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        if (Utils.hasMarshmallow() && FenrirNative.isNativeLoaded()) {
            if (isSelected) {
                holder.selected.fromRes(R.raw.theme_selected, Utils.dp(120), Utils.dp(120), new int[]{0x333333, CurrentTheme.getColorWhite(holder.selected.getContext()), 0x777777, CurrentTheme.getColorPrimary(holder.selected.getContext()), 0x999999, CurrentTheme.getColorSecondary(holder.selected.getContext())});
                holder.selected.playAnimation();
            } else {
                holder.selected.clearAnimationDrawable();
            }
        } else {
            if (isSelected) {
                holder.selected.setImageResource(R.drawable.theme_select);
            }
        }

        holder.clicked.setOnClickListener(v -> {
            currentId = category.getId();
            clickListener.onClick(position, category);
        });
    }

    private void bindThemeHolder(@NonNull ThemeHolder holder, int position) {
        ThemeValue category = data.get(position);
        holder.itemView.setAlpha(category.getDisabled() ? 0.55f : 1.0f);
        boolean isSelected = currentId.equals(category.getId());

        holder.title.setText(category.getDisabled() ? holder.itemView.getContext().getString(R.string.not_available) : category.getName());
        holder.primary.setBackgroundColor(isDark ? category.getColorNightPrimary() : category.getColorDayPrimary());
        holder.secondary.setBackgroundColor(isDark ? category.getColorNightSecondary() : category.getColorDaySecondary());
        holder.selected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        if (Utils.hasMarshmallow() && FenrirNative.isNativeLoaded()) {
            if (isSelected) {
                holder.selected.fromRes(R.raw.theme_selected, Utils.dp(120), Utils.dp(120), new int[]{0x333333, CurrentTheme.getColorWhite(holder.selected.getContext()), 0x777777, CurrentTheme.getColorPrimary(holder.selected.getContext()), 0x999999, CurrentTheme.getColorSecondary(holder.selected.getContext())});
                holder.selected.playAnimation();
            } else {
                holder.selected.clearAnimationDrawable();
            }
        } else {
            if (isSelected) {
                holder.selected.setImageResource(R.drawable.theme_select);
            }
        }

        holder.clicked.setOnClickListener(v -> {
            currentId = category.getId();
            clickListener.onClick(position, category);
        });
        holder.gradient.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{isDark ? category.getColorNightPrimary() : category.getColorDayPrimary(), isDark ? category.getColorNightSecondary() : category.getColorDaySecondary()}));
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(ThemeValue[] data) {
        this.data = Arrays.asList(data);
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void onClick(int index, ThemeValue value);
    }

    static class ThemeHolder extends RecyclerView.ViewHolder {
        final ImageView primary;
        final ImageView secondary;
        final RLottieImageView selected;
        final ImageView gradient;
        final ViewGroup clicked;
        final TextView title;

        public ThemeHolder(View itemView) {
            super(itemView);
            primary = itemView.findViewById(R.id.theme_icon_primary);
            secondary = itemView.findViewById(R.id.theme_icon_secondary);
            selected = itemView.findViewById(R.id.selected);
            clicked = itemView.findViewById(R.id.theme_type);
            title = itemView.findViewById(R.id.item_title);
            gradient = itemView.findViewById(R.id.theme_icon_gradient);
        }
    }

    static class SpecialThemeHolder extends RecyclerView.ViewHolder {
        final RLottieImageView selected;
        final ViewGroup clicked;
        final TextView title;
        final TextView special_title;

        public SpecialThemeHolder(View itemView) {
            super(itemView);
            selected = itemView.findViewById(R.id.selected);
            clicked = itemView.findViewById(R.id.theme_type);
            title = itemView.findViewById(R.id.item_title);
            special_title = itemView.findViewById(R.id.special_text);
        }
    }
}
