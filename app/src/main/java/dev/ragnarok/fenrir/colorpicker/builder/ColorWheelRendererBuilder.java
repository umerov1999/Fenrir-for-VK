package dev.ragnarok.fenrir.colorpicker.builder;

import dev.ragnarok.fenrir.colorpicker.ColorPickerView;
import dev.ragnarok.fenrir.colorpicker.renderer.ColorWheelRenderer;
import dev.ragnarok.fenrir.colorpicker.renderer.FlowerColorWheelRenderer;
import dev.ragnarok.fenrir.colorpicker.renderer.SimpleColorWheelRenderer;

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