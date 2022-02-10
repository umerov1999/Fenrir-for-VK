package de.maxr1998.modernpreferences.preferences.colorpicker.renderer;

import java.util.List;

import de.maxr1998.modernpreferences.preferences.colorpicker.ColorCircle;

public interface ColorWheelRenderer {
    float GAP_PERCENTAGE = 0.025f;

    void draw();

    ColorWheelRenderOption getRenderOption();

    void initWith(ColorWheelRenderOption colorWheelRenderOption);

    List<ColorCircle> getColorCircleList();
}
