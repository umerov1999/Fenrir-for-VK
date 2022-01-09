package dev.ragnarok.fenrir.colorpicker.builder;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.textfield.TextInputEditText;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.colorpicker.ColorPickerView;
import dev.ragnarok.fenrir.colorpicker.OnColorChangedListener;
import dev.ragnarok.fenrir.colorpicker.OnColorSelectedListener;
import dev.ragnarok.fenrir.colorpicker.Utils;
import dev.ragnarok.fenrir.colorpicker.renderer.ColorWheelRenderer;
import dev.ragnarok.fenrir.colorpicker.slider.AlphaSlider;
import dev.ragnarok.fenrir.colorpicker.slider.LightnessSlider;

public class ColorPickerDialogBuilder {
    private final AlertDialog.Builder builder;
    private final LinearLayout pickerContainer;
    private final ColorPickerView colorPickerView;
    private final Integer[] initialColor = {null, null, null, null, null};
    private boolean isLightnessSliderEnabled = true;
    private boolean isAlphaSliderEnabled = true;
    private boolean isBorderEnabled = true;
    private boolean isColorEditEnabled;
    private boolean isPreviewEnabled;
    private int pickerCount = 1;

    private ColorPickerDialogBuilder(Context context) {
        this(context, 0);
    }

    private ColorPickerDialogBuilder(Context context, int theme) {
        int defaultMargin = getDimensionAsPx(context, R.dimen.default_slider_margin);
        int defaultMarginTop = getDimensionAsPx(context, R.dimen.default_margin_top);

        builder = new AlertDialog.Builder(context, theme);
        pickerContainer = new LinearLayout(context);
        pickerContainer.setOrientation(LinearLayout.VERTICAL);
        pickerContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        pickerContainer.setPadding(defaultMargin, defaultMarginTop, defaultMargin, 0);

        LinearLayout.LayoutParams layoutParamsForColorPickerView = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParamsForColorPickerView.weight = 1;
        colorPickerView = new ColorPickerView(context);

        pickerContainer.addView(colorPickerView, layoutParamsForColorPickerView);

        builder.setView(pickerContainer);
    }

    public static ColorPickerDialogBuilder with(Context context) {
        return new ColorPickerDialogBuilder(context);
    }

    public static ColorPickerDialogBuilder with(Context context, int theme) {
        return new ColorPickerDialogBuilder(context, theme);
    }

    private static int getDimensionAsPx(Context context, int rid) {
        return (int) (context.getResources().getDimension(rid) + .5f);
    }

    public ColorPickerDialogBuilder setTitle(String title) {
        builder.setTitle(title);
        return this;
    }

    public ColorPickerDialogBuilder setTitle(int titleId) {
        builder.setTitle(titleId);
        return this;
    }

    public ColorPickerDialogBuilder initialColor(int initialColor) {
        this.initialColor[0] = initialColor;
        return this;
    }

    public ColorPickerDialogBuilder initialColors(int[] initialColor) {
        for (int i = 0; i < initialColor.length && i < this.initialColor.length; i++) {
            this.initialColor[i] = initialColor[i];
        }
        return this;
    }

    public ColorPickerDialogBuilder wheelType(ColorPickerView.WHEEL_TYPE wheelType) {
        ColorWheelRenderer renderer = ColorWheelRendererBuilder.getRenderer(wheelType);
        colorPickerView.setRenderer(renderer);
        return this;
    }

    public ColorPickerDialogBuilder density(int density) {
        colorPickerView.setDensity(density);
        return this;
    }

    public ColorPickerDialogBuilder setOnColorChangedListener(OnColorChangedListener onColorChangedListener) {
        colorPickerView.addOnColorChangedListener(onColorChangedListener);
        return this;
    }

    public ColorPickerDialogBuilder setOnColorSelectedListener(OnColorSelectedListener onColorSelectedListener) {
        colorPickerView.addOnColorSelectedListener(onColorSelectedListener);
        return this;
    }

    public ColorPickerDialogBuilder setPositiveButton(CharSequence text, ColorPickerClickListener onClickListener) {
        builder.setPositiveButton(text, (dialog, which) -> positiveButtonOnClick(dialog, onClickListener));
        return this;
    }

    public ColorPickerDialogBuilder setPositiveButton(int textId, ColorPickerClickListener onClickListener) {
        builder.setPositiveButton(textId, (dialog, which) -> positiveButtonOnClick(dialog, onClickListener));
        return this;
    }

    public ColorPickerDialogBuilder setNegativeButton(CharSequence text, DialogInterface.OnClickListener onClickListener) {
        builder.setNegativeButton(text, onClickListener);
        return this;
    }

    public ColorPickerDialogBuilder setNegativeButton(int textId, DialogInterface.OnClickListener onClickListener) {
        builder.setNegativeButton(textId, onClickListener);
        return this;
    }

    public ColorPickerDialogBuilder noSliders() {
        isLightnessSliderEnabled = false;
        isAlphaSliderEnabled = false;
        return this;
    }

    public ColorPickerDialogBuilder alphaSliderOnly() {
        isLightnessSliderEnabled = false;
        isAlphaSliderEnabled = true;
        return this;
    }

    public ColorPickerDialogBuilder lightnessSliderOnly() {
        isLightnessSliderEnabled = true;
        isAlphaSliderEnabled = false;
        return this;
    }

    public ColorPickerDialogBuilder showAlphaSlider(boolean showAlpha) {
        isAlphaSliderEnabled = showAlpha;
        return this;
    }

    public ColorPickerDialogBuilder showLightnessSlider(boolean showLightness) {
        isLightnessSliderEnabled = showLightness;
        return this;
    }

    public ColorPickerDialogBuilder showBorder(boolean showBorder) {
        isBorderEnabled = showBorder;
        return this;
    }

    public ColorPickerDialogBuilder showColorEdit(boolean showEdit) {
        isColorEditEnabled = showEdit;
        return this;
    }

    public ColorPickerDialogBuilder showColorPreview(boolean showPreview) {
        isPreviewEnabled = showPreview;
        if (!showPreview)
            pickerCount = 1;
        return this;
    }

    public ColorPickerDialogBuilder setPickerCount(int pickerCount) throws IndexOutOfBoundsException {
        if (pickerCount < 1 || pickerCount > 5)
            throw new IndexOutOfBoundsException("Picker Can Only Support 1-5 Colors");
        this.pickerCount = pickerCount;
        if (this.pickerCount > 1)
            isPreviewEnabled = true;
        return this;
    }

    public AlertDialog build() {
        Context context = builder.getContext();
        colorPickerView.setInitialColors(initialColor, getStartOffset(initialColor));
        colorPickerView.setShowBorder(isBorderEnabled);

        if (isLightnessSliderEnabled) {
            LinearLayout.LayoutParams layoutParamsForLightnessBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getDimensionAsPx(context, R.dimen.default_slider_height));
            LightnessSlider lightnessSlider = new LightnessSlider(context);
            lightnessSlider.setLayoutParams(layoutParamsForLightnessBar);
            pickerContainer.addView(lightnessSlider);
            colorPickerView.setLightnessSlider(lightnessSlider);
            lightnessSlider.setColor(getStartColor(initialColor));
            lightnessSlider.setShowBorder(isBorderEnabled);
        }
        if (isAlphaSliderEnabled) {
            LinearLayout.LayoutParams layoutParamsForAlphaBar = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getDimensionAsPx(context, R.dimen.default_slider_height));
            AlphaSlider alphaSlider = new AlphaSlider(context);
            alphaSlider.setLayoutParams(layoutParamsForAlphaBar);
            pickerContainer.addView(alphaSlider);
            colorPickerView.setAlphaSlider(alphaSlider);
            alphaSlider.setColor(getStartColor(initialColor));
            alphaSlider.setShowBorder(isBorderEnabled);
        }
        if (isColorEditEnabled) {
            LinearLayout.LayoutParams layoutParamsForColorEdit = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextInputEditText colorEdit = (TextInputEditText) View.inflate(context, R.layout.color_edit, null);
            colorEdit.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            colorEdit.setSingleLine();
            colorEdit.setVisibility(View.GONE);

            // limit number of characters to hexColors
            int maxLength = isAlphaSliderEnabled ? 9 : 7;
            colorEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

            pickerContainer.addView(colorEdit, layoutParamsForColorEdit);

            colorEdit.setText(Utils.getHexString(getStartColor(initialColor), isAlphaSliderEnabled));
            colorPickerView.setColorEdit(colorEdit);
        }
        if (isPreviewEnabled) {
            LinearLayout colorPreview = (LinearLayout) View.inflate(context, R.layout.color_preview, null);
            colorPreview.setVisibility(View.GONE);
            pickerContainer.addView(colorPreview);

            if (initialColor.length == 0) {
                ImageView colorImage = (ImageView) View.inflate(context, R.layout.color_selector, null);
                colorImage.setImageDrawable(new ColorDrawable(Color.WHITE));
            } else {
                for (int i = 0; i < initialColor.length && i < pickerCount; i++) {
                    if (initialColor[i] == null)
                        break;
                    LinearLayout colorLayout = (LinearLayout) View.inflate(context, R.layout.color_selector, null);
                    ImageView colorImage = colorLayout.findViewById(R.id.image_preview);
                    colorImage.setImageDrawable(new ColorDrawable(initialColor[i]));
                    colorPreview.addView(colorLayout);
                }
            }
            colorPreview.setVisibility(View.VISIBLE);
            colorPickerView.setColorPreview(colorPreview, getStartOffset(initialColor));
        }

        return builder.create();
    }

    private Integer getStartOffset(Integer[] colors) {
        int start = 0;
        for (int i = 0; i < colors.length; i++) {
            if (colors[i] == null) {
                return start;
            }
            start = (i + 1) / 2;
        }
        return start;
    }

    private int getStartColor(Integer[] colors) {
        Integer startColor = getStartOffset(colors);
        return startColor == null ? Color.WHITE : colors[startColor];
    }

    private void positiveButtonOnClick(DialogInterface dialog, ColorPickerClickListener onClickListener) {
        int selectedColor = colorPickerView.getSelectedColor();
        Integer[] allColors = colorPickerView.getAllColors();
        onClickListener.onClick(dialog, selectedColor, allColors);
    }
}