package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import dev.ragnarok.fenrir.R;

public class MessageView extends LinearLayout {

    private static final int DEFAULT_COLOR = Color.RED;
    private final Paint FILL_PAINT = new Paint();
    private final Path PATH = new Path();
    private final float DEFAULT_RADIUS = 10;
    private float radius_top_left = DEFAULT_RADIUS;
    private float radius_top_right = DEFAULT_RADIUS;
    private float radius_bottom_left = DEFAULT_RADIUS;
    private float radius_bottom_right = DEFAULT_RADIUS;
    private int first_color = DEFAULT_COLOR;
    private int second_color = DEFAULT_COLOR;

    private int canvasWidth;
    private int canvasHeight;
    private LinearGradient gradient;

    public MessageView(Context context) {
        super(context, null);
    }

    public MessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
        FILL_PAINT.setStyle(Paint.Style.FILL);
        FILL_PAINT.setDither(true);
        FILL_PAINT.setAntiAlias(true);
        initializeAttributes(context, attrs);
    }

    private float dp2px(float dpValue) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, getResources().getDisplayMetrics());
    }

    private void initializeAttributes(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MessageView);
            setRadius(array.getDimension(R.styleable.MessageView_radius, dp2px(DEFAULT_RADIUS)));
            first_color = array.getColor(R.styleable.MessageView_first_color, DEFAULT_COLOR);
            second_color = array.getColor(R.styleable.MessageView_second_color, DEFAULT_COLOR);
            array.recycle();
        }
    }

    private void setRadius(float radius) {
        radius_bottom_right = radius;
        radius_bottom_left = radius;
        radius_top_right = radius;
        radius_top_left = radius;
    }

    public void setGradientColor(int first_color, int second_color) {
        this.first_color = first_color;
        this.second_color = second_color;
        invalidate();
    }

    public void setNonGradientColor(int color) {
        first_color = color;
        second_color = color;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (first_color == second_color) {
            FILL_PAINT.setColor(first_color);
            FILL_PAINT.setShader(null);
        } else {
            if (canvasWidth != getWidth() || canvasHeight != getHeight() || gradient == null) {
                canvasWidth = getWidth();
                canvasHeight = getHeight();
                gradient = new LinearGradient(0f, 0f, getWidth(), getHeight(),
                        first_color, second_color, Shader.TileMode.CLAMP);
            }
            FILL_PAINT.setShader(gradient);
        }
        int width = getWidth();
        int height = getHeight();

        PATH.reset();

        PATH.moveTo(0, radius_top_left);
        PATH.arcTo(0, 0, 2 * radius_top_left, 2 * radius_top_left, 180, 90, false);
        PATH.lineTo(width - radius_top_right, 0);
        PATH.arcTo(width - 2 * radius_top_right, 0, width, 2 * radius_top_right, 270, 90, false);
        PATH.lineTo(width, height - radius_bottom_right);
        PATH.arcTo(width - 2 * radius_bottom_right, height - 2 * radius_bottom_right, width, height, 0, 90, false);
        PATH.lineTo(radius_bottom_left, height);
        PATH.arcTo(0, height - 2 * radius_bottom_left, 2 * radius_bottom_left, height, 90, 90, false);
        PATH.lineTo(0, radius_top_left);
        PATH.close();

        canvas.drawPath(PATH, FILL_PAINT);
    }
}
