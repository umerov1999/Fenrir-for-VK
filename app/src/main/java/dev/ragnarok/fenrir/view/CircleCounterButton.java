package dev.ragnarok.fenrir.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;

public class CircleCounterButton extends LinearLayout {

    private static final int DEF_ACTIVE_ICON_COLOR = Color.parseColor("#f0f0f0");
    private static final int DEF_NOACTIVE_ICON_COLOR = Color.parseColor("#b0b0b0");
    private static final int DEF_ACTIVE_BACKGROUND_COLOR = Color.RED;
    private static final int DEF_NOACTIVE_BACKGROUND_COLOR = Color.parseColor("#45dcdcdc");

    private Drawable mIcon;
    private int textColor;
    private int mActiveIconColor;
    private int mNoactiveIconColor;
    private int mActiveBackgroundColor;
    private int mNoactiveBackgroundColor;
    private boolean mAlwaysCounter;
    private boolean mActive;
    private int mCount;

    private ImageView icon;
    private TextView counter;
    private ObjectAnimator animator;

    public CircleCounterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircleCounterButton(Context context) {
        this(context, null);
    }

    protected void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.button_circle_with_counter, this);

        icon = findViewById(R.id.icon);
        counter = findViewById(R.id.counter);

        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.CircleCounterButton);

        try {
            initAttributes(attrArray, counter.getCurrentTextColor());
        } finally {
            attrArray.recycle();
        }

        counter.setTextColor(textColor);

        initViews();

        setGravity(Gravity.CENTER);
    }

    private void initViews() {
        resolveActiveViews();
        resolveCounter();
        icon.setImageDrawable(mIcon);
    }

    protected void initAttributes(TypedArray attrArray, int deftextcolor) {
        mIcon = attrArray.getDrawable(R.styleable.CircleCounterButton_button_icon);
        textColor = attrArray.getColor(R.styleable.CircleCounterButton_text_color, deftextcolor);
        mCount = attrArray.getInt(R.styleable.CircleCounterButton_count, 0);
        mActive = attrArray.getBoolean(R.styleable.CircleCounterButton_active, Boolean.FALSE);
        mAlwaysCounter = attrArray.getBoolean(R.styleable.CircleCounterButton_always_counter, Boolean.FALSE);
        mActiveIconColor = attrArray.getColor(R.styleable.CircleCounterButton_active_icon_color, DEF_ACTIVE_ICON_COLOR);

        mNoactiveIconColor = attrArray.getColor(R.styleable.CircleCounterButton_noactive_icon_color, DEF_NOACTIVE_ICON_COLOR);
        mActiveBackgroundColor = attrArray.getColor(R.styleable.CircleCounterButton_active_background_color, DEF_ACTIVE_BACKGROUND_COLOR);

        mNoactiveBackgroundColor = attrArray.getColor(R.styleable.CircleCounterButton_noactive_background_color, DEF_NOACTIVE_BACKGROUND_COLOR);
    }

    private void resolveCounter() {
        counter.setVisibility(mAlwaysCounter || mCount > 0 ? VISIBLE : GONE);
        counter.setText(String.valueOf(mCount));
    }

    private void resolveActiveViews() {
        if (mActive) {
            Utils.setTint(icon, mActiveIconColor);
            Utils.setBackgroundTint(icon, mActiveBackgroundColor);
        } else {
            Utils.setTint(icon, mNoactiveIconColor);
            Utils.setBackgroundTint(icon, mNoactiveBackgroundColor);
        }
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        mActive = active;
        resolveActiveViews();
    }

    public void setCount(int count, boolean animate) {
        mCount = count;

        counter.setVisibility(mAlwaysCounter || mCount > 0 ? VISIBLE : GONE);

        if (Objects.nonNull(animator)) {
            animator.cancel();
        }

        animator = ViewUtils.setCountText(counter, count, animate);
    }

    public void setIcon(Drawable drawable) {
        mIcon = drawable;
        icon.setImageDrawable(mIcon);
    }

    public void setIcon(int res) {
        mIcon = ContextCompat.getDrawable(getContext(), res);
        icon.setImageDrawable(mIcon);
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int count) {
        setCount(count, false);
    }
}