package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.util.Utils;

public class OnlineView extends AppCompatImageView {

    private static final Paint PAINT = new Paint();

    static {
        PAINT.setStyle(Paint.Style.FILL);
        PAINT.setAntiAlias(true);
        PAINT.setDither(true);
    }

    //@DrawableRes
    //private int mIcon;

    //@Dimension
    //private float mIconPadding;

    @ColorInt
    private int mCircleColor;
    @ColorInt
    private int mStrokeColor;
    @Dimension
    private float mStrokeWidth;

    public OnlineView(Context context) {
        this(context, null);
    }

    public OnlineView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawStroke(canvas);
        drawSolid(canvas);
        super.onDraw(canvas);
    }

    private void drawSolid(Canvas canvas) {
        float minSize = Math.min(getHeightF(), getWidthF());
        float radius = minSize / 2 - mStrokeWidth;

        PAINT.setColor(mCircleColor);
        canvas.drawCircle(getWidthF() / 2, getHeightF() / 2, radius, PAINT);
    }

    private void drawStroke(Canvas canvas) {
        float minSize = Math.min(getHeightF(), getWidthF());
        float radius = minSize / 2;

        PAINT.setColor(mStrokeColor);
        canvas.drawCircle(getWidthF() / 2, getHeightF() / 2, radius, PAINT);
    }

    private float getWidthF() {
        return (float) getWidth();
    }

    private float getHeightF() {
        return (float) getHeight();
    }

    private float pxOf(float dp) {
        return Utils.dpToPx(dp, getContext());
    }

    private void init(Context context, AttributeSet attrs) {
        //LayoutInflater.from(getContext()).inflate(R.layout.view_online, this);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OnlineView);

        try {
            mCircleColor = a.getColor(R.styleable.OnlineView_circle_color, Color.BLUE);
            mStrokeColor = a.getColor(R.styleable.OnlineView_stroke_color, Color.WHITE);
            mStrokeWidth = a.getDimension(R.styleable.OnlineView_stroke_width, pxOf(1));
            //mIconPadding = a.getDimension(R.styleable.OnlineView_icon_padding, 8);
            //mIcon = a.getResourceId(R.styleable.OnlineView_icon, 0);
        } finally {
            a.recycle();
        }

        //View strokeView = findViewById(R.id.stroke);
        //ImageView circle = (ImageView) findViewById(R.id.circle);

        //strokeView.getBackground().setColorFilter(mStrokeColor, PorterDuff.Mode.MULTIPLY);
        //circle.getBackground().setColorFilter(mCircleColor, PorterDuff.Mode.MULTIPLY);
        //circle.setImageResource(mIcon);
        //int padding = (int) mIconPadding;
        //circle.setPadding(padding, padding, padding, padding);

        //int dp4 = (int) pxOf(4f);
        //setPadding(dp4, dp4, dp4, dp4);
    }

    public void setIcon(int resourceId) {
        setImageResource(resourceId);
        //mIcon = resourceId;
        //ImageView circle = (ImageView) findViewById(R.id.circle);
        //circle.setImageResource(mIcon);
    }

    public void setStrokeColor(int color) {
        mStrokeColor = color;
        invalidate();
        //View strokeView = findViewById(R.id.stroke);
        //strokeView.getBackground().setColorFilter(mStrokeColor, PorterDuff.Mode.MULTIPLY);
    }

    public void setCircleColor(int color) {
        mCircleColor = color;
        invalidate();
        //View strokeView = findViewById(R.id.stroke);
        //strokeView.getBackground().setColorFilter(mStrokeColor, PorterDuff.Mode.MULTIPLY);
    }
}
