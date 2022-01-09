package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class CityEntity {

    private int id;

    private String title;

    private boolean important;

    private String area;

    private String region;

    public int getId() {
        return id;
    }

    public CityEntity setId(int id) {
        this.id = id;
        return this;
    }

    public boolean isImportant() {
        return important;
    }

    public CityEntity setImportant(boolean important) {
        this.important = important;
        return this;
    }

    public String getArea() {
        return area;
    }

    public CityEntity setArea(String area) {
        this.area = area;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public CityEntity setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public CityEntity setTitle(String title) {
        this.title = title;
        return this;
    }
}