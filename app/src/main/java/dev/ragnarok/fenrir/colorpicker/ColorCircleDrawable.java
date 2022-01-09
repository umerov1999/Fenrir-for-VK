package dev.ragnarok.fenrir.colorpicker;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;

import dev.ragnarok.fenrir.colorpicker.builder.PaintBuilder;

public class ColorCircleDrawable extends ColorDrawable {
    private final Paint fillPaint = PaintBuilder.newPaint().style(Paint.Style.FILL).color(0).build();
    private final Paint fillBackPaint = PaintBuilder.newPaint().shader(PaintBuilder.createAlphaPatternShader(26)).build();
    private float strokeWidth;
    private final Paint strokePaint = PaintBuilder.newPaint().style(Paint.Style.STROKE).stroke(strokeWidth).color(0xff9e9e9e).build();

    public ColorCircleDrawable(int color) {
        super(color);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawColor(0);

        int width = getBounds().width();
        float radius = width / 2f;
        strokeWidth = radius / 8f;

        strokePaint.setStrokeWidth(strokeWidth);
        fillPaint.setColor(getColor());
        canvas.drawCircle(radius, radius, radius - strokeWidth, fillBackPaint);
        canvas.drawCircle(radius, radius, radius - strokeWidth, fillPaint);
        canvas.drawCircle(radius, radius, radius - strokeWidth, strokePaint);
    }

    @Override
    public void setColor(int color) {
        super.setColor(color);
        invalidateSelf();
    }
}
