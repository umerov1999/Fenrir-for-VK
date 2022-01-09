package dev.ragnarok.fenrir.model;

import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

import dev.ragnarok.fenrir.settings.Settings;


public class SparseArrayOwnersBundle implements IOwnersBundle {

    private final SparseArray<Owner> data;

    public SparseArrayOwnersBundle(int capacity) {
        data = new SparseArray<>(capacity);
    }

    @Nullable
    @Override
    public Owner findById(int id) {
        return data.get(id);
    }

    @NonNull
    @Override
    public Owner getById(int id) {
        Owner owner = findById(id);

        if (owner == null) {
            if (id > 0) {
                owner = new User(id);
            } else if (id < 0) {
                owner = new Community(-id);
            } else {
                owner = new User(Settings.get().accounts().getCurrent());
                //throw new IllegalArgumentException("Zero owner id!!!");
            }
        }

        return owner;
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public void putAll(@NonNull Collection<? extends Owner> owners) {
        for (Owner owner : owners) {
            put(owner);
        }
    }

    @Override
    public void put(@NonNull Owner owner) {
        switch (owner.getOwnerType()) {
            case OwnerType.USER:
                data.put(((User) owner).getId(), owner);
                break;
            case OwnerType.COMMUNITY:
                data.put(-((Community) owner).getId(), owner);
                break;
        }
    }

    @Override
    public Collection<Integer> getMissing(Collection<Integer> ids) {
        Collection<Integer> missing = new ArrayList<>();

        for (Integer id : ids) {
            if (data.get(id) == null) {
                missing.add(id);
            }
        }

        return missing;
    }
}