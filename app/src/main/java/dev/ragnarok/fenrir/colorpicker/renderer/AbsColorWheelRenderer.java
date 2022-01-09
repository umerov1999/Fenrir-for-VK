package dev.ragnarok.fenrir.colorpicker.renderer;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.colorpicker.ColorCircle;

public abstract class AbsColorWheelRenderer implements ColorWheelRenderer {
    protected final List<ColorCircle> colorCircleList = new ArrayList<>();
    protected ColorWheelRenderOption colorWheelRenderOption;

    public void initWith(ColorWheelRenderOption colorWheelRenderOption) {
        this.colorWheelRenderOption = colorWheelRenderOption;
        colorCircleList.clear();
    }

    @Override
    public ColorWheelRenderOption getRenderOption() {
        if (colorWheelRenderOption == null) colorWheelRenderOption = new ColorWheelRenderOption();
        return colorWheelRenderOption;
    }

    public List<ColorCircle> getColorCircleList() {
        return colorCircleList;
    }

    protected int getAlphaValueAsInt() {
        return Math.round(colorWheelRenderOption.alpha * 255);
    }

    protected int calcTotalCount(float radius, float size) {
        return Math.max(1, (int) ((1f - GAP_PERCENTAGE) * Math.PI / (Math.asin(size / radius)) + 0.5f));
    }
}
