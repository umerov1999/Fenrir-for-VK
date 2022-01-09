package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import io.reactivex.rxjava3.core.Single;


public interface IStatusApi {

    @CheckResult
    Single<Boolean> set(String text, Integer groupId);

}
