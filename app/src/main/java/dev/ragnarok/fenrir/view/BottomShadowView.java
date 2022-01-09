package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;

import androidx.core.view.ViewCompat;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;

import dev.ragnarok.fenrir.R;

public class BottomShadowView extends FrameLayout {

    public BottomShadowView(Context context) {
        super(context);
        init(context, null);
    }

    public BottomShadowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BottomShadowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    protected void init(Context context, AttributeSet attrs) {
        setOutlineProvider(ViewOutlineProvider.BOUNDS);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BottomShadowView);
        ViewCompat.setBackground(this, a.getDrawable(R.styleable.BottomShadowView_android_background));
        if (getBackground() instanceof ColorDrawable) {
            ColorDrawable background = (ColorDrawable) getBackground();
            MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
            materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
            materialShapeDrawable.initializeElevationOverlay(context);
            ViewCompat.setBackground(this, materialShapeDrawable);
        }
        setElevation(a.getDimension(R.styleable.BottomShadowView_elevation, 0));
        a.recycle();
    }

    @Override
    public void setElevation(float elevation) {
        super.setElevation(elevation);
        MaterialShapeUtils.setElevation(this, elevation);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        MaterialShapeUtils.setParentAbsoluteElevation(this);
    }
}
