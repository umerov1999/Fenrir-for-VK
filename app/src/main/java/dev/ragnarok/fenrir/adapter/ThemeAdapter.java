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

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView primary;
        final ImageView secondary;
        final RLottieImageView selected;
        final ImageView gradient;
        final ViewGroup clicked;
        final TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            primary = itemView.findViewById(R.id.theme_icon_primary);
            secondary = itemView.findViewById(R.id.theme_icon_secondary);
            selected = itemView.findViewById(R.id.selected);
            clicked = itemView.findViewById(R.id.theme_type);
            title = itemView.findViewById(R.id.item_title);
            gradient = itemView.findViewById(R.id.theme_icon_gradient);
        }
    }
}
