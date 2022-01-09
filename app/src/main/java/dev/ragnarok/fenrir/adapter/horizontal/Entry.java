package dev.ragnarok.fenrir.adapter.horizontal;

import android.content.Context;

import androidx.annotation.NonNull;

public interface Entry {

    String getTitle(@NonNull Context context);

    boolean isActive();

    boolean isCustom();
}
