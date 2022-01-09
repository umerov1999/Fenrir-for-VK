package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Utils;

public class MySpinnerView extends RelativeLayout {

    private String mHintText;
    @ColorInt
    private int mHintColor;
    @ColorInt
    private int mTextColor;
    private TextView mTextView;

    public MySpinnerView(Context context) {
        this(context, null);
    }

    public MySpinnerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_my_spinner, this);

        mTextView = findViewById(R.id.text);

        setBackgroundResource(R.drawable.backgroud_rectangle_border);

        ImageView icon = findViewById(R.id.icon);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MySpinnerView);

        try {
            mHintText = a.getString(R.styleable.MySpinnerView_spinner_hint);
            mHintColor = a.getColor(R.styleable.MySpinnerView_spinner_hint_color, CurrentTheme.getColorSecondary(context));
            mTextColor = a.getColor(R.styleable.MySpinnerView_spinner_text_color, CurrentTheme.getColorOnSurface(context));

            int iconColor = a.getColor(R.styleable.MySpinnerView_spinner_icon_color, CurrentTheme.getColorPrimary(context));
            Utils.setColorFilter(icon, iconColor);
        } finally {
            a.recycle();
        }

        mTextView.setText(mHintText);
        mTextView.setTextColor(mHintColor);
    }

    public void setIconOnClickListener(View.OnClickListener listener) {
        findViewById(R.id.icon).setOnClickListener(listener);
    }

    public void setValue(String value) {
        if (value != null) {
            mTextView.setText(value);
            mTextView.setTextColor(mTextColor);
        } else {
            mTextView.setText(mHintText);
            mTextView.setTextColor(mHintColor);
        }
    }
}