package dev.ragnarok.fenrir.model;

public class School {

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

    public School setId(int id) {
        this.id = id;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public School setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getCityId() {
        return cityId;
    }

    public School setCityId(int cityId) {
        this.cityId = cityId;
        return this;
    }

    public String getName() {
        return name;
    }

    public School setName(String name) {
        this.name = name;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public School setFrom(int from) {
        this.from = from;
        return this;
    }

    public int getTo() {
        return to;
    }

    public School setTo(int to) {
        this.to = to;
        return this;
    }

    public int getYearGraduated() {
        return yearGraduated;
    }

    public School setYearGraduated(int yearGraduated) {
        this.yearGraduated = yearGraduated;
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public School setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }
}