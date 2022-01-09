package dev.ragnarok.fenrir.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;

import dev.ragnarok.fenrir.R;

public class CircleRoadProgress extends View {

    private static final Paint PAINT = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
    private static final Property<CircleRoadProgress, Float> PROGRESS_PROPERTY = new Property<CircleRoadProgress, Float>(Float.class, "displayed-precentage") {
        @Override
        public Float get(CircleRoadProgress view) {
            return view.displayedPercentage;
        }

        @Override
        public void set(CircleRoadProgress view, Float value) {
            view.displayedPercentage = value;
            view.invalidate();
        }
    };
    private float circleCenterPointX;
    private float circleCenterPointY;
    private int roadColor;
    private float roadStrokeWidth;
    private float roadRadius;
    private int arcLoadingColor;
    private float arcLoadingStrokeWidth;
    private float arcLoadingStartAngle;
    private float displayedPercentage;

    public CircleRoadProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeAttributes(context, attrs);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        circleCenterPointX = (float) w / 2;
        circleCenterPointY = (float) h / 2;
        int paddingInContainer = 3;
        roadRadius = ((float) w / 2) - (roadStrokeWidth / 2) - paddingInContainer;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawRoad(canvas);
        drawArcLoading(canvas);
    }

    private void initializeAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CircleRoadProgress);
        circleCenterPointX = ta.getFloat(R.styleable.CircleRoadProgress_circleCenterPointX, 54f);
        circleCenterPointY = ta.getFloat(R.styleable.CircleRoadProgress_circleCenterPointY, 54f);
        roadColor = ta.getColor(R.styleable.CircleRoadProgress_roadColor, Color.parseColor("#575757"));
        roadStrokeWidth = ta.getDimensionPixelSize(R.styleable.CircleRoadProgress_roadStrokeWidth, 10);
        roadRadius = ta.getDimensionPixelSize(R.styleable.CircleRoadProgress_roadRadius, 42);
        arcLoadingColor = ta.getColor(R.styleable.CircleRoadProgress_arcLoadingColor, Color.parseColor("#f5d600"));
        arcLoadingStrokeWidth = ta.getDimensionPixelSize(R.styleable.CircleRoadProgress_arcLoadingStrokeWidth, 3);

        arcLoadingStartAngle = ta.getFloat(R.styleable.CircleRoadProgress_arcLoadingStartAngle, 270f);

        ta.recycle();
    }

    private void drawRoad(Canvas canvas) {
        PAINT.setDither(true);
        PAINT.setColor(roadColor);
        PAINT.setStyle(Paint.Style.STROKE);
        PAINT.setStrokeWidth(roadStrokeWidth);
        PAINT.setStrokeCap(Paint.Cap.ROUND);
        PAINT.setStrokeJoin(Paint.Join.ROUND);
        canvas.drawCircle(circleCenterPointX, circleCenterPointY, roadRadius, PAINT);
    }

    private void drawArcLoading(Canvas canvas) {
        PAINT.setColor(arcLoadingColor);
        PAINT.setStrokeWidth(arcLoadingStrokeWidth);

        float delta = circleCenterPointX - roadRadius;
        float arcSize = (circleCenterPointX - (delta / 2f)) * 2f;

        RectF box = new RectF(delta, delta, arcSize, arcSize);
        //float sweep = 360 * percent * 0.01f;
        float sweep = 360 * displayedPercentage * 0.01f;
        canvas.drawArc(box, arcLoadingStartAngle, sweep, false, PAINT);
    }

    public void changePercentage(int percent) {
        displayedPercentage = percent;

        invalidate();
    }

    public void changePercentageSmoothly(int percent) {

        ObjectAnimator animator = ObjectAnimator.ofFloat(this, PROGRESS_PROPERTY, percent);
        animator.setDuration(750);
        //animator.setInterpolator(new AccelerateInterpolator(1.75f));
        animator.start();
    }
}