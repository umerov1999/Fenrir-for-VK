package dev.ragnarok.fenrir

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.View
import dev.ragnarok.fenrir.util.RxUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlin.contracts.contract

inline fun <reified T : Any> Single<T>.fromIOToMain(): Single<T> =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

inline fun <reified T : Any> Maybe<T>.fromIOToMain(): Maybe<T> =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

inline fun <reified T : Any> Single<T>.fromIOToMainComputation(): Single<T> =
    subscribeOn(Schedulers.computation()).observeOn(AndroidSchedulers.mainThread())

inline fun <reified T : Any> Single<T>.subscribeIOAndIgnoreResults(): Disposable =
    subscribeOn(Schedulers.io()).subscribe(RxUtils.ignore(), RxUtils.ignore())

inline fun <reified T : Any> Flowable<T>.toMainThread(): Flowable<T> =
    observeOn(AndroidSchedulers.mainThread())

inline fun <reified T : Any> Observable<T>.toMainThread(): Observable<T> =
    observeOn(AndroidSchedulers.mainThread())

fun Completable.fromIOToMain(): Completable =
    subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun SQLiteDatabase.query(
    tableName: String,
    columns: Array<String>,
    where: String?,
    args: Array<String>?
): Cursor = query(tableName, columns, where, args, null, null, null)

fun SQLiteDatabase.query(tableName: String, columns: Array<String>): Cursor =
    query(tableName, columns, null, null)

fun Cursor.getNullableInt(columnName: String): Int? =
    getColumnIndex(columnName).let { if (it < 0) null else getInt(it) }

fun Cursor.getInt(columnName: String): Int = getInt(getColumnIndexOrThrow(columnName))

fun Cursor.getLong(columnName: String): Long = getLong(getColumnIndexOrThrow(columnName))

fun Cursor.getBoolean(columnName: String): Boolean =
    getInt(getColumnIndexOrThrow(columnName)) == 1

fun Cursor.getNullableLong(columnName: String): Long? =
    getColumnIndex(columnName).let { if (it < 0) null else getLong(it) }

fun Cursor.getString(columnName: String): String? =
    getString(getColumnIndexOrThrow(columnName))

fun Disposable.notDisposed(): Boolean = !isDisposed

inline fun <reified T> Collection<T?>?.safeAllIsNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@safeAllIsNullOrEmpty != null)
    }
    if (this == null) {
        return true
    }
    for (obj in this) {
        if (obj is CharSequence) {
            return obj.isEmpty()
        } else if (obj is Collection<*>) {
            return obj.isEmpty()
        }
        if (obj != null) {
            return false
        }
    }
    return true
}

inline fun <reified T> Collection<T>?.nonNullNoEmpty(): Boolean {
    contract {
        returns(true) implies (this@nonNullNoEmpty != null)
    }
    return this != null && !this.isEmpty()
}

inline fun <reified T : CharSequence> T?.nonNullNoEmpty(): Boolean {
    contract {
        returns(true) implies (this@nonNullNoEmpty != null)
    }
    return this != null && this.isNotEmpty()
}

inline fun <reified K, reified V> Map<out K, V>?.nonNullNoEmpty(): Boolean {
    contract {
        returns(true) implies (this@nonNullNoEmpty != null)
    }
    return this != null && this.isNotEmpty()
}

fun IntArray?.nonNullNoEmpty(): Boolean {
    contract {
        returns(true) implies (this@nonNullNoEmpty != null)
    }
    return this != null && this.isNotEmpty()
}

fun IntArray?.nullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@nullOrEmpty != null)
    }
    return this == null || this.isEmpty()
}

inline fun <reified T : CharSequence> T?.trimmedIsNullOrEmpty(): Boolean {
    contract {
        returns(false) implies (this@trimmedIsNullOrEmpty != null)
    }
    return this == null || this.trim { it <= ' ' }.isEmpty()
}

inline fun <reified T : CharSequence> T?.trimmedNonNullNoEmpty(): Boolean {
    contract {
        returns(true) implies (this@trimmedNonNullNoEmpty != null)
    }
    return this != null && this.trim { it <= ' ' }.isNotEmpty()
}

inline fun <reified T : CharSequence> T?.nonNullNoEmpty(block: (T) -> Unit) {
    if (!isNullOrEmpty()) apply(block)
}

inline fun <reified T, reified E : Collection<T>> E?.nonNullNoEmpty(block: (E) -> Unit) {
    if (!isNullOrEmpty()) apply(block)
}

inline fun <reified T : CharSequence> T?.trimmedNonNullNoEmpty(block: (T) -> Unit) {
    this?.let {
        if (trim { it <= ' ' }.isNotEmpty()) {
            apply(block)
        }
    }
}

inline fun <reified T : Any> Flowable<T>.subscribeIgnoreErrors(consumer: Consumer<in T>): Disposable =
    subscribe(consumer, RxUtils.ignore())

inline fun <reified T : Any> Single<T>.subscribeIgnoreErrors(consumer: Consumer<in T>): Disposable =
    subscribe(consumer, RxUtils.ignore())

fun Completable.subscribeIOAndIgnoreResults(): Disposable =
    subscribeOn(Schedulers.io()).subscribe(RxUtils.dummy(), RxUtils.ignore())

inline fun View.fadeOut(duration: Long, crossinline onEnd: () -> Unit = {}) {
    ObjectAnimator.ofPropertyValuesHolder(
        this,
        PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
    ).apply {
        this.duration = duration
        addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator?) {
                onEnd()
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                onEnd()
            }
        })
        start()
    }
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

inline fun View.fadeIn(duration: Long, crossinline onEnd: () -> Unit = {}) {
    ObjectAnimator.ofPropertyValuesHolder(
        this,
        PropertyValuesHolder.ofFloat(View.ALPHA, 0f, 1f)
    ).apply {
        this.duration = duration
        addListener(object : StubAnimatorListener() {
            override fun onAnimationEnd(animation: Animator?) {
                onEnd()
            }

            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                onEnd()
            }
        })
        start()
    }
}

fun <T> MutableList<T>.insert(index: Int, element: T) {
    if (index <= size) {
        add(index, element)
    } else {
        add(element)
    }
}

fun <T> MutableList<T>.insertAfter(index: Int, element: T) {
    val cur = index + 1
    if (cur <= size) {
        add(cur, element)
    } else {
        add(element)
    }
}

open class StubAnimatorListener : Animator.AnimatorListener {
    override fun onAnimationRepeat(animation: Animator?) {

    }

    override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {

    }

    override fun onAnimationEnd(animation: Animator?) {

    }

    override fun onAnimationCancel(animation: Animator?) {

    }

    override fun onAnimationStart(animation: Animator?) {

    }
}