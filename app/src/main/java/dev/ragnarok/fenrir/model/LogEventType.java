package dev.ragnarok.fenrir.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.adapter.horizontal.Entry;


public class LogEventType implements Entry {

    private final int type;

    @StringRes
    private final int title;

    private boolean active;

    public LogEventType(int type, int title) {
        this.type = type;
        this.title = title;
    }

    public int getType() {
        return type;
    }

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(title);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public LogEventType setActive(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public boolean isCustom() {
        return false;
    }
}
