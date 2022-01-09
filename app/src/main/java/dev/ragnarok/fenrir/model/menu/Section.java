package dev.ragnarok.fenrir.model.menu;

import androidx.annotation.DrawableRes;

import dev.ragnarok.fenrir.model.Text;

public class Section {

    private final Text title;
    @DrawableRes
    private Integer icon;

    public Section(Text title) {
        this.title = title;
    }

    public Text getTitle() {
        return title;
    }

    public Integer getIcon() {
        return icon;
    }

    public Section setIcon(Integer icon) {
        this.icon = icon;
        return this;
    }
}
