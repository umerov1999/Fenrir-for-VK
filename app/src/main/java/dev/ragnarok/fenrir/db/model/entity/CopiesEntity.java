package dev.ragnarok.fenrir.db.model.entity;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import dev.ragnarok.fenrir.db.model.IdPairEntity;


public class CopiesEntity {

    @SerializedName("count")
    private int count;
    @SerializedName("pairDbos")
    private List<IdPairEntity> pairDbos;

    public int getCount() {
        return count;
    }

    public CopiesEntity setCount(int count) {
        this.count = count;
        return this;
    }

    public List<IdPairEntity> getPairDbos() {
        return pairDbos;
    }

    public CopiesEntity setPairDbos(List<IdPairEntity> pairDbos) {
        this.pairDbos = pairDbos;
        return this;
    }
}