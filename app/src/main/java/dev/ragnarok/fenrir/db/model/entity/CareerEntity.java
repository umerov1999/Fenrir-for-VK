package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class CareerEntity {

    private int groupId;

    private String company;

    private int countryId;

    private int cityId;

    private int from;

    private int until;

    private String position;

    public int getCityId() {
        return cityId;
    }

    public CareerEntity setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public CareerEntity setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public CareerEntity setFrom(int from) {
        this.from = from;
        return this;
    }

    public int getGroupId() {
        return groupId;
    }

    public CareerEntity setGroupId(int groupId) {
        this.groupId = groupId;
        return this;
    }

    public int getUntil() {
        return until;
    }

    public CareerEntity setUntil(int until) {
        this.until = until;
        return this;
    }

    public String getCompany() {
        return company;
    }

    public CareerEntity setCompany(String company) {
        this.company = company;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public CareerEntity setPosition(String position) {
        this.position = position;
        return this;
    }
}