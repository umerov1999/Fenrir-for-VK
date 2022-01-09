package dev.ragnarok.fenrir.activity;

import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.SelectProfileCriteria;

public interface ProfileSelectable {

    void select(Owner owner);

    SelectProfileCriteria getAcceptableCriteria();
}