package dev.ragnarok.fenrir.filepicker.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import dev.ragnarok.fenrir.settings.CurrentTheme;


/**
 * @author akshay sunil masram
 */
public class MaterialCheckbox extends View {

    private Context context;
    private int minDim;
    private Paint paint;
    private RectF bounds;
    private boolean checked;
    private OnCheckedChangeListener onCheckedChangeListener;
    private Path tick;

    public MaterialCheckbox(Context context) {
        super(context);
        initView(context);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MaterialCheckbox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void initView(Context context) {
        this.context = context;
        checked = false;
        tick = new Path();
        paint = new Paint();
        bounds = new RectF();
        OnClickListener onClickListener = v -> {
            setChecked(!checked);
            onCheckedChangeListener.onCheckedChanged(this, isChecked());
        };

        setOnClickListener(onClickListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isChecked()) {
            paint.reset();
            paint.setAntiAlias(true);
            bounds.set((float) minDim / 10, (float) minDim / 10, minDim - ((float) minDim / 10), minDim - ((float) minDim / 10));
            paint.setColor(CurrentTheme.getColorControlNormal(context));
            canvas.drawRoundRect(bounds, (float) minDim / 8, (float) minDim / 8, paint);

            paint.setColor(Color.parseColor("#FFFFFF"));
            paint.setStrokeWidth((float) minDim / 10);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.BEVEL);
            canvas.drawPath(tick, paint);
        } else {
            paint.reset();
            paint.setAntiAlias(true);
            bounds.set((float) minDim / 10, (float) minDim / 10, minDim - ((float) minDim / 10), minDim - ((float) minDim / 10));
            paint.setColor(Color.parseColor("#C1C1C1"));
            canvas.drawRoundRect(bounds, (float) minDim / 8, (float) minDim / 8, paint);

            bounds.set((float) minDim / 5, (float) minDim / 5, minDim - ((float) minDim / 5), minDim - ((float) minDim / 5));
            paint.setColor(Color.parseColor("#FFFFFF"));
            canvas.drawRect(bounds, paint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        minDim = Math.min(width, height);
        bounds.set((float) minDim / 10, (float) minDim / 10, minDim - ((float) minDim / 10), minDim - ((float) minDim / 10));
        tick.moveTo((float) minDim / 4, (float) minDim / 2);
        tick.lineTo(minDim / 2.5f, minDim - ((float) minDim / 3));

        tick.moveTo(minDim / 2.75f, minDim - (minDim / 3.25f));
        tick.lineTo(minDim - ((float) minDim / 4), (float) minDim / 3);
        setMeasuredDimension(width, height);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
        invalidate();
    }

    public void setOnCheckedChangedListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }
}
