package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class SchoolEntity {

    private int id;
    private int countryId;
    private int cityId;
    private String name;
    private int from;
    private int to;
    private int yearGraduated;
    private String clazz;

    public int getId() {
        return id;
    }

    public SchoolEntity setId(int id) {
        this.id = id;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public SchoolEntity setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getCityId() {
        return cityId;
    }

    public SchoolEntity setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public String getName() {
        return name;
    }

    public SchoolEntity setName(String name) {
        this.name = name;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public SchoolEntity setFrom(int from) {
        this.from = from;
        return this;
    }

    public int getTo() {
        return to;
    }

    public SchoolEntity setTo(int to) {
        this.to = to;
        return this;
    }

    public int getYearGraduated() {
        return yearGraduated;
    }

    public SchoolEntity setYearGraduated(int yearGraduated) {
        this.yearGraduated = yearGraduated;
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public SchoolEntity setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }
}