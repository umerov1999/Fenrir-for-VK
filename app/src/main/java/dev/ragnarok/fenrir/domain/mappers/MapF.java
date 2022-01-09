package dev.ragnarok.fenrir.domain.mappers;

public interface MapF<O, R> {
    R map(O orig);
}