package dev.ragnarok.fenrir.link.internal;

import androidx.annotation.NonNull;

public class AbsInternalLink {

    public int start;
    public int end;

    public String targetLine;

    @NonNull
    @Override
    public String toString() {
        return "AbsInternalLink{" +
                "start=" + start +
                ", end=" + end +
                ", targetLine='" + targetLine + '\'' +
                '}';
    }
}
