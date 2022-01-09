package dev.ragnarok.fenrir.model;

public class Military {

    private String unit;

    private int unitId;

    private int countryId;

    private int from;

    private int until;

    public int getUntil() {
        return until;
    }

    public Military setUntil(int until) {
        this.until = until;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public Military setFrom(int from) {
        this.from = from;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public Military setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getUnitId() {
        return unitId;
    }

    public Military setUnitId(int unitId) {
        this.unitId = unitId;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public Military setUnit(String unit) {
        this.unit = unit;
        return this;
    }
}