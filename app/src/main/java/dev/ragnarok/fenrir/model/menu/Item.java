package dev.ragnarok.fenrir.model.menu;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;

import dev.ragnarok.fenrir.model.Icon;
import dev.ragnarok.fenrir.model.Text;

public class Item {

    private final int key;
    private final Text title;
    private Icon icon;
    private Section section;
    private Integer color;

    private int extra;

    public Item(int key, Text title) {
        this.key = key;
        this.title = title;
    }

    public int getExtra() {
        return extra;
    }

    public Item setExtra(int extra) {
        this.extra = extra;
        return this;
    }

    public Integer getColor() {
        return color;
    }

    public Item setColor(@ColorInt int color) {
        this.color = color;
        return this;
    }

    public int getKey() {
        return key;
    }

    public Section getSection() {
        return section;
    }

    public Item setSection(Section section) {
        this.section = section;
        return this;
    }

    public Icon getIcon() {
        return icon;
    }

    public Item setIcon(@DrawableRes int res) {
        icon = Icon.fromResources(res);
        return this;
    }

    public Item setIcon(String remoteUrl) {
        icon = Icon.fromUrl(remoteUrl);
        return this;
    }

    public Item setIcon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public Text getTitle() {
        return title;
    }
}