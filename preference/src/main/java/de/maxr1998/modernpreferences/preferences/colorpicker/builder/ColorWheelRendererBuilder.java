package de.maxr1998.modernpreferences.preferences.colorpicker.builder;

import de.maxr1998.modernpreferences.preferences.colorpicker.ColorPickerView;
import de.maxr1998.modernpreferences.preferences.colorpicker.renderer.ColorWheelRenderer;
import de.maxr1998.modernpreferences.preferences.colorpicker.renderer.FlowerColorWheelRenderer;
import de.maxr1998.modernpreferences.preferences.colorpicker.renderer.SimpleColorWheelRenderer;

public class ColorWheelRendererBuilder {
    public static ColorWheelRenderer getRenderer(ColorPickerView.WHEEL_TYPE wheelType) {
        switch (wheelType) {
            case CIRCLE:
                return new SimpleColorWheelRenderer();
            case FLOWER:
                return new FlowerColorWheelRenderer();
        }
        throw new IllegalArgumentException("wrong WHEEL_TYPE");
    }
}