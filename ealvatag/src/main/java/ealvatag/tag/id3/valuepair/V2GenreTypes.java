package ealvatag.tag.id3.valuepair;

import com.google.common.collect.ImmutableSortedSet;

import ealvatag.tag.reference.GenreTypes;

/**
 * ID3V2 Genre list
 * <p>
 * <p>Merging of Id3v2 genres and the extended ID3v2 genres
 */
public class V2GenreTypes {
    private static volatile V2GenreTypes instance;
    private final ImmutableSortedSet<String> genres;

    private V2GenreTypes() {
        ImmutableSortedSet.Builder<String> builder = ImmutableSortedSet.naturalOrder();
        builder.addAll(GenreTypes.getInstanceOf().getSortedValueSet());
        builder.add(ID3V2ExtendedGenreTypes.CR.getDescription());
        builder.add(ID3V2ExtendedGenreTypes.RX.getDescription());
        genres = builder.build();
    }

    public static V2GenreTypes getInstanceOf() {
        if (instance == null) {
            synchronized (V2GenreTypes.class) {
                if (instance == null) {
                    instance = new V2GenreTypes();
                }
            }
        }
        return instance;
    }

    public ImmutableSortedSet<String> getSortedValueSet() {
        return genres;
    }
}
