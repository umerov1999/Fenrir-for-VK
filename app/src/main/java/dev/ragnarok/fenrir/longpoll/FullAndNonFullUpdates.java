package dev.ragnarok.fenrir.longpoll;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.api.model.longpoll.AddMessageUpdate;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class FullAndNonFullUpdates {

    private List<AddMessageUpdate> full;
    private List<AddMessageUpdate> nonFull;

    @NonNull
    public List<AddMessageUpdate> prepareFull() {
        if (Objects.isNull(full)) {
            full = new ArrayList<>(1);
        }

        return full;
    }

    @NonNull
    public List<AddMessageUpdate> prepareNonFull() {
        if (Objects.isNull(nonFull)) {
            nonFull = new ArrayList<>(1);
        }

        return nonFull;
    }

    public List<AddMessageUpdate> getFullMessages() {
        return full;
    }

    public List<AddMessageUpdate> getNonFull() {
        return nonFull;
    }

    public boolean hasFullMessages() {
        return !Utils.safeIsEmpty(full);
    }

    public boolean hasNonFullMessages() {
        return !Utils.safeIsEmpty(nonFull);
    }

    @NonNull
    @Override
    public String toString() {
        return "FullAndNonFullUpdates[" +
                "full=" + (full == null ? 0 : full.size()) +
                ", nonFull=" + (nonFull == null ? 0 : nonFull.size()) + ']';
    }
}
