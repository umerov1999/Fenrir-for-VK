package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import dev.ragnarok.fenrir.R;

public class ColorFilterImageView extends AppCompatImageView {

    private int color;
    private boolean disabledColorFilter;

    public ColorFilterImageView(Context context) {
        this(context, null);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.ColorFilterImageView);

        try {
            color = attrArray.getColor(R.styleable.ColorFilterImageView_filter_color, Color.BLACK);
        } finally {
            attrArray.recycle();
        }

        resolveColorFilter();
    }

    private void resolveColorFilter() {
        if (disabledColorFilter) {
            setImageTintList(null);
        } else {
            setImageTintList(ColorStateList.valueOf(color));
        }
    }

    public void setColorFilterEnabled(boolean enabled) {
        disabledColorFilter = !enabled;
        resolveColorFilter();
    }
}