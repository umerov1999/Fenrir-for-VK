package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import dev.ragnarok.fenrir.R;

public class AspectRatioFrameLayout extends FrameLayout {

    private static final int DEFAULT_PROPORTION_WIDTH = 16;
    private static final int DEFAULT_PROPORTION_HEIGHT = 9;

    private int mProportionWidth;
    private int mProportionHeight;

    public AspectRatioFrameLayout(Context context) {
        super(context);
        mProportionWidth = DEFAULT_PROPORTION_WIDTH;
        mProportionHeight = DEFAULT_PROPORTION_HEIGHT;
    }

    public AspectRatioFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AspectRatioFrameLayout,
                0, 0);

        try {
            mProportionWidth = a.getInt(R.styleable.AspectRatioFrameLayout_aspectRatioWidth, DEFAULT_PROPORTION_WIDTH);
            mProportionHeight = a.getInt(R.styleable.AspectRatioFrameLayout_aspectRatioHeight, DEFAULT_PROPORTION_HEIGHT);
        } finally {
            a.recycle();
        }
    }

    public void setAspectRatio(int w, int h) {
        mProportionHeight = h;
        mProportionWidth = w;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = originalWidth * mProportionHeight / mProportionWidth;

        int finalWidth, finalHeight;

        if (calculatedHeight > originalHeight) {
            finalWidth = originalHeight * mProportionWidth / mProportionHeight;
            finalHeight = originalHeight;
        } else {
            finalWidth = originalWidth;
            finalHeight = calculatedHeight;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }
}
