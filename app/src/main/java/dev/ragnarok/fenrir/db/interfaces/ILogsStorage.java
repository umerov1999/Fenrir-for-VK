package dev.ragnarok.fenrir.db.interfaces;

import java.util.List;

import dev.ragnarok.fenrir.model.LogEvent;
import io.reactivex.rxjava3.core.Single;


public interface ILogsStorage {

    Single<LogEvent> add(int type, String tag, String body);

    Single<List<LogEvent>> getAll(int type);

    void Clear();
}
