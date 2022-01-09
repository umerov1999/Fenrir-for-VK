package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import dev.ragnarok.fenrir.R;

public class WrapWidthTextView extends AppCompatTextView {

    private boolean mFixWrapText;

    public WrapWidthTextView(Context context) {
        this(context, null);
    }

    public WrapWidthTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.WrapWidthTextView, 0, 0);
        try {
            mFixWrapText = a.getBoolean(R.styleable.WrapWidthTextView_fixWrapText, false);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mFixWrapText) {
            if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
                int width = getMaxWidth(getLayout());
                if (width > 0 && width < getMeasuredWidth()) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), heightMeasureSpec);
                }
            }
        }
    }

    private int getMaxWidth(Layout layout) {
        int linesCount = layout.getLineCount();
        if (linesCount < 2) {
            return 0;
        }

        float maxWidth = 0;
        for (int i = 0; i < linesCount; i++) {
            maxWidth = Math.max(maxWidth, layout.getLineWidth(i));
        }

        return (int) Math.ceil(maxWidth);
    }
}