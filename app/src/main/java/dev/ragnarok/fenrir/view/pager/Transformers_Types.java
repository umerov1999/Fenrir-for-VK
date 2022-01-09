package dev.ragnarok.fenrir.view.pager;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({Transformers_Types.OFF,
        Transformers_Types.DEPTH_TRANSFORMER,
        Transformers_Types.ZOOM_OUT_TRANSFORMER,
        Transformers_Types.CLOCK_SPIN_TRANSFORMER,
        Transformers_Types.BACKGROUND_TO_FOREGROUND_TRANSFORMER,
        Transformers_Types.CUBE_IN_DEPTH_TRANSFORMER,
        Transformers_Types.FAN_TRANSFORMER,
        Transformers_Types.GATE_TRANSFORMER,
        Transformers_Types.SLIDER_TRANSFORMER})
@Retention(RetentionPolicy.SOURCE)
public @interface Transformers_Types {
    int OFF = 0;
    int DEPTH_TRANSFORMER = 1;
    int ZOOM_OUT_TRANSFORMER = 2;
    int CLOCK_SPIN_TRANSFORMER = 3;
    int BACKGROUND_TO_FOREGROUND_TRANSFORMER = 4;
    int CUBE_IN_DEPTH_TRANSFORMER = 5;
    int FAN_TRANSFORMER = 6;
    int GATE_TRANSFORMER = 7;
    int SLIDER_TRANSFORMER = 8;
}

