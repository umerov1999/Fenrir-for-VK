package dev.ragnarok.fenrir.model;

public class Career {

    private Community group;

    private String company;

    private int countryId;

    private int cityId;

    private int from;

    private int until;

    private String position;

    public int getCityId() {
        return cityId;
    }

    public Career setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public Career setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public Career setFrom(int from) {
        this.from = from;
        return this;
    }

    public Community getGroup() {
        return group;
    }

    public Career setGroup(Community group) {
        this.group = group;
        return this;
    }

    public int getUntil() {
        return until;
    }

    public Career setUntil(int until) {
        this.until = until;
        return this;
    }

    public String getCompany() {
        return company;
    }

    public Career setCompany(String company) {
        this.company = company;
        return this;
    }

    public String getPosition() {
        return position;
    }

    public Career setPosition(String position) {
        this.position = position;
        return this;
    }
}