package dev.ragnarok.fenrir.db.model.entity;

import androidx.annotation.Keep;

@Keep
public class MilitaryEntity {

    private String unit;

    private int unitId;

    private int countryId;

    private int from;

    private int until;

    public int getUntil() {
        return until;
    }

    public MilitaryEntity setUntil(int until) {
        this.until = until;
        return this;
    }

    public int getFrom() {
        return from;
    }

    public MilitaryEntity setFrom(int from) {
        this.from = from;
        return this;
    }

    public int getCountryId() {
        return countryId;
    }

    public MilitaryEntity setCountryId(int countryId) {
        this.countryId = countryId;
        return this;
    }

    public int getUnitId() {
        return unitId;
    }

    public MilitaryEntity setUnitId(int unitId) {
        this.unitId = unitId;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public MilitaryEntity setUnit(String unit) {
        this.unit = unit;
        return this;
    }
}