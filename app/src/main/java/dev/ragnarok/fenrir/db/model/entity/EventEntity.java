package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class EventEntity extends Entity {

    private int id;
    private String button_text;
    private String text;

    public int getId() {
        return id;
    }

    public EventEntity setId(int id) {
        this.id = id;
        return this;
    }

    public String getText() {
        return text;
    }

    public EventEntity setText(String text) {
        this.text = text;
        return this;
    }

    public String getButton_text() {
        return button_text;
    }

    public EventEntity setButton_text(String button_text) {
        this.button_text = button_text;
        return this;
    }
}
