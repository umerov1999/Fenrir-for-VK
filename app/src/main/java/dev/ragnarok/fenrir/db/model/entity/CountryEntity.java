package dev.ragnarok.fenrir.db.model.entity;


import androidx.annotation.Keep;

@Keep
public class CountryEntity extends Entity {

    private int id;

    private String title;

    public CountryEntity set(int id, String title) {
        this.id = id;
        this.title = title;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }
}