package dev.ragnarok.fenrir.model;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import dev.ragnarok.fenrir.adapter.horizontal.Entry;


public class DocFilter implements Entry {

    private final int type;

    @StringRes
    private final int title;

    private boolean active;

    public DocFilter(int type, @StringRes int title) {
        this.type = type;
        this.title = title;
    }

    @Override
    public String getTitle(@NonNull Context context) {
        return context.getString(title);
    }

    public int getType() {
        return type;
    }

    public int getTitle() {
        return title;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public DocFilter setActive(boolean active) {
        this.active = active;
        return this;
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    public static class Type {
        public static final int ALL = 0;
        public static final int TEXT = 1;
        public static final int ARCHIVE = 2;
        public static final int GIF = 3;
        public static final int IMAGE = 4;
        public static final int AUDIO = 5;
        public static final int VIDEO = 6;
        public static final int BOOKS = 7;
        public static final int OTHER = 8;
    }
}
