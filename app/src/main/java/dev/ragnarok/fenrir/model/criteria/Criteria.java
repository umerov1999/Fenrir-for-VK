package dev.ragnarok.fenrir.model.criteria;

import androidx.annotation.NonNull;

public class Criteria implements Cloneable {

    @NonNull
    @Override
    public Criteria clone() throws CloneNotSupportedException {
        return (Criteria) super.clone();
    }
}
