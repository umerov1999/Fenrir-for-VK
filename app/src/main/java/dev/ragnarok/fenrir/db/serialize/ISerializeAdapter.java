package dev.ragnarok.fenrir.db.serialize;


public interface ISerializeAdapter<T> {
    T deserialize(String raw);

    String serialize(T data);
}