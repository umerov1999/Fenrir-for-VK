package dev.ragnarok.fenrir.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.colorpicker.builder.ColorPickerDialogBuilder;
import dev.ragnarok.fenrir.settings.theme.ThemesController;

public class ColorPickerPreference extends Preference {

    protected boolean alphaSlider;
    protected boolean lightSlider;
    protected boolean border;

    protected int selectedColor;

    protected ColorPickerView.WHEEL_TYPE wheelType;
    protected int density;
    protected ImageView colorIndicator;
    private boolean pickerColorEdit;
    private String pickerTitle;
    private String pickerButtonCancel;
    private String pickerButtonOk;

    public ColorPickerPreference(@NonNull Context context) {
        super(context);
    }

    public ColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initWith(context, attrs);
    }

    public ColorPickerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWith(context, attrs);
    }

    public static int darken(int color, float factor) {
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(a,
                Math.max((int) (r * factor), 0),
                Math.max((int) (g * factor), 0),
                Math.max((int) (b * factor), 0));
    }

    private void initWith(@NonNull Context context, @Nullable AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView);

        try {
            alphaSlider = typedArray.getBoolean(R.styleable.ColorPickerView_alphaSlider, false);
            lightSlider = typedArray.getBoolean(R.styleable.ColorPickerView_lightnessSlider, false);
            border = typedArray.getBoolean(R.styleable.ColorPickerView_border, true);

            density = typedArray.getInt(R.styleable.ColorPickerView_density, 8);
            wheelType = ColorPickerView.WHEEL_TYPE.indexOf(typedArray.getInt(R.styleable.ColorPickerView_wheelType, 0));

            selectedColor = typedArray.getInt(R.styleable.ColorPickerView_initialColor, 0xffffffff);

            pickerColorEdit = typedArray.getBoolean(R.styleable.ColorPickerView_pickerColorEdit, true);
            pickerTitle = typedArray.getString(R.styleable.ColorPickerView_pickerTitle);
            if (pickerTitle == null)
                pickerTitle = "Choose color";

            pickerButtonCancel = typedArray.getString(R.styleable.ColorPickerView_pickerButtonCancel);
            if (pickerButtonCancel == null)
                pickerButtonCancel = "cancel";

            pickerButtonOk = typedArray.getString(R.styleable.ColorPickerView_pickerButtonOk);
            if (pickerButtonOk == null)
                pickerButtonOk = "ok";

        } finally {
            typedArray.recycle();
        }

        setWidgetLayoutResource(R.layout.color_widget);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        colorIndicator = (ImageView) holder.findViewById(R.id.color_indicator);

        selectedColor = getPersistedInt(selectedColor);

        int tmpColor = isEnabled()
                ? selectedColor
                : darken(selectedColor, .5f);

        ColorCircleDrawable colorChoiceDrawable = new ColorCircleDrawable(tmpColor);
        colorIndicator.setImageDrawable(colorChoiceDrawable);
    }

    public void setValue(int value) {
        if (callChangeListener(value)) {
            selectedColor = value;
            persistInt(value);
            notifyChanged();
        }
    }

    @Override
    protected void onClick() {
        ColorPickerDialogBuilder builder = ColorPickerDialogBuilder
                .with(getContext(), ThemesController.INSTANCE.currentStyle())
                .setTitle(pickerTitle)
                .initialColor(selectedColor)
                .showBorder(border)
                .wheelType(wheelType)
                .density(density)
                .showColorEdit(pickerColorEdit)
                .setPositiveButton(pickerButtonOk, (dialog, selectedColorFromPicker, allColors) -> setValue(selectedColorFromPicker))
                .setNegativeButton(pickerButtonCancel, null);

        if (!alphaSlider && !lightSlider) builder.noSliders();
        else if (!alphaSlider) builder.lightnessSliderOnly();
        else if (!lightSlider) builder.alphaSliderOnly();

        builder
                .build()
                .show();
    }
}
