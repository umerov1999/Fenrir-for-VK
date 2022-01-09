package dev.ragnarok.fenrir.view.mozaik;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.PostImage;

public class MozaikLayout extends RelativeLayout {

    private List<PostImage> photos = new ArrayList<>();
    private final MatrixCalculator.Libra libra = index -> photos.get(index).getAspectRatio();
    private int maxSingleImageHeight;
    private int prefImageSize;
    private int spacing;
    private MozaikLayoutParamsCalculator layoutParamsCalculator;

    public MozaikLayout(Context context) {
        super(context);
        //this.maxSingleImageHeight = (int) context.getResources().getDimension(R.dimen.max_single_image_height);
        maxSingleImageHeight = getDisplayHeight();
        prefImageSize = (int) context.getResources().getDimension(R.dimen.pref_image_size);
        spacing = (int) dpToPx(1F);
    }

    public MozaikLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initDimensions(context, attrs);
    }

    public MozaikLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDimensions(context, attrs);
    }

    private int getDisplayHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    private void initDimensions(Context context, AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.MozaikLayout, 0, 0);

        try {
            //maxSingleImageHeight = a.getDimensionPixelSize(R.styleable.MozaikLayout_maxSingleImageHeight, (int) context.getResources().getDimension(R.dimen.max_single_image_height));
            maxSingleImageHeight = a.getDimensionPixelSize(R.styleable.MozaikLayout_maxSingleImageHeight, getDisplayHeight());
            prefImageSize = (int) a.getDimension(R.styleable.MozaikLayout_prefImageSize, context.getResources().getDimension(R.dimen.pref_image_size));
            spacing = a.getDimensionPixelSize(R.styleable.MozaikLayout_spacing, (int) dpToPx(1));
        } finally {
            a.recycle();
        }
    }

    private void initCalculator(int parentWidth) {
        int[][] matrix = createMatrix(parentWidth);

        layoutParamsCalculator = new MozaikLayoutParamsCalculator(matrix, photos, parentWidth, spacing);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        //int parentHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (photos.size() == 1) {
            View parent = getChildAt(0);
            LayoutParams parentparams = getLayoutParamsForSingleImage(photos.get(0), (LayoutParams) parent.getLayoutParams(), parentWidth);
            parent.measure(parentparams.width, parentparams.height);
        } else {
            if (layoutParamsCalculator == null) {
                initCalculator(parentWidth);
            }

            for (int p = 0; p < photos.size(); p++) {
                PostImage image = photos.get(p);
                View parent = getChildAt(p);
                if (parent.getVisibility() == View.GONE) {
                    continue;
                }

                if (image.getPosition() == null) {
                    image.setPosition(layoutParamsCalculator.getPostImagePosition(p));
                }

                PostImagePosition position = image.getPosition();

                LayoutParams params = (LayoutParams) parent.getLayoutParams();
                params.width = position.sizeX;
                params.height = position.sizeY;
                params.topMargin = position.marginY;
                params.leftMargin = position.marginX;

                parent.measure(image.getPosition().sizeX, image.getPosition().sizeY);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private int[][] createMatrix(int maxWidth) {
        int prefRowCount = getPreferedRowCount(maxWidth);

        //long start = System.currentTimeMillis();

        MatrixCalculator matrixCalculator = new MatrixCalculator(photos.size(), libra);
        return matrixCalculator.calculate(prefRowCount);

        //Exestime.log("MozaikLayout.createMatrix", start, "photocount: " + photos.size() + ", prefRowCount: " + prefRowCount);
        //return matrix;
    }

    private int getPreferedRowCount(int maxWidthPx) {
        int dpPerProportion = (int) (prefImageSize / getDensity());

        int proportionDpSum = 0;
        for (PostImage image : photos) {
            float proportion = image.getAspectRatio();
            proportionDpSum = (int) (proportionDpSum + proportion * dpPerProportion);
        }

        int maxContainerWidthDp = convertPixtoDip(maxWidthPx);
        int prefRowCount = (int) Math.round((double) proportionDpSum / (double) maxContainerWidthDp);

        if (prefRowCount == 0) {
            prefRowCount = 1;
        }

        return prefRowCount;
    }

    public float getDensity() {
        return getResources().getDisplayMetrics().density;
    }

    public int convertPixtoDip(int pixel) {
        float scale = getDensity();
        return (int) ((pixel - 0.5f) / scale);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (photos.size() == 1) {
            View parent = getChildAt(0);
            LayoutParams params = getLayoutParamsForSingleImage(photos.get(0), (LayoutParams) parent.getLayoutParams(), getWidth());
            parent.layout(params.leftMargin, params.topMargin, params.rightMargin, params.bottomMargin);
        } else {
            if (layoutParamsCalculator == null) {
                initCalculator(getWidth());
            }

            for (int p = 0; p < photos.size(); p++) {
                PostImage postImage = photos.get(p);
                View parent = getChildAt(p);
                if (parent.getVisibility() == View.GONE) {
                    continue;
                }

                if (postImage.getPosition() == null) {
                    postImage.setPosition(layoutParamsCalculator.getPostImagePosition(p));
                }

                LayoutParams params = (LayoutParams) parent.getLayoutParams();

                PostImagePosition position = postImage.getPosition();

                params.width = position.sizeX;
                params.height = position.sizeY;
                params.topMargin = position.marginY;
                params.leftMargin = position.marginX;

                //parent.setLayoutParams(params);
                parent.layout(position.marginX, position.marginY, params.rightMargin, params.bottomMargin);
            }
        }

        super.onLayout(changed, l, t, r, b);
    }

    public void setPhotos(List<PostImage> photos) {
        this.photos = photos;
        layoutParamsCalculator = null;
    }

    private float dpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

    private RelativeLayout.LayoutParams getLayoutParamsForSingleImage(PostImage photo, RelativeLayout.LayoutParams params, int maxWidth) {
        double coef = (double) photo.getWidth() / (double) photo.getHeight();
        int measuredwidth = maxWidth;
        int measuredheight = (int) (maxWidth / coef);

        if (maxSingleImageHeight < measuredheight) {
            measuredheight = maxSingleImageHeight;
            measuredwidth = (int) (measuredheight * coef);
        }

        params.height = measuredheight;
        params.width = measuredwidth;
        return params;
    }
}
