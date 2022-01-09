package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.util.Utils;

public class YoutubeButton extends LinearLayout {

    public YoutubeButton(Context context) {
        this(context, null);
    }

    public YoutubeButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_youtube_button, this);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.YoutubeButton);

        try {
            @DrawableRes
            int iconres = a.getResourceId(R.styleable.YoutubeButton_youtube_button_icon, R.drawable.heart_filled);

            int iconcolor = a.getColor(R.styleable.YoutubeButton_youtube_button_icon_color, Color.GRAY);
            String text = a.getString(R.styleable.YoutubeButton_youtube_button_text);
            int textcolor = a.getColor(R.styleable.YoutubeButton_youtube_button_text_color, Color.GRAY);

            ImageView icon = findViewById(R.id.youtube_button_icon);
            Utils.setColorFilter(icon, iconcolor);
            icon.setImageResource(iconres);

            TextView textView = findViewById(R.id.youtube_button_text);
            textView.setTextColor(textcolor);
            textView.setText(text);
        } finally {
            a.recycle();
        }
    }

    public void setIconColor(@ColorInt int color) {
        ((ImageView) findViewById(R.id.youtube_button_icon)).setColorFilter(color);
    }
}