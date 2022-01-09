package dev.ragnarok.fenrir

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.View
import dev.ragnarok.fenrir.util.RxUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.schedulers.Schedulers

class Extensions {
    companion object {
        fun <T : Any> Single<T>.fromIOToMain(): Single<T> =
            subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        fun <T : Any> Single<T>.subscribeIOAndIgnoreResults(): Disposable =
            subscribeOn(Schedulers.io()).subscribe(RxUtils.ignore(), RxUtils.ignore())

        fun <T : Any> Flowable<T>.toMainThread(): Flowable<T> =
            observeOn(AndroidSchedulers.mainThread())

        fun <T : Any> Observable<T>.toMainThread(): Observable<T> =
            observeOn(AndroidSchedulers.mainThread())

        fun Completable.fromIOToMain(): Completable =
            subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

        fun SQLiteDatabase.query(
            tableName: String,
            columns: Array<String>,
            where: String?,
            args: Array<String>?
        ): Cursor = query(tableName, columns, where, args, null, null, null)

        fun SQLiteDatabase.query(tablename: String, columns: Array<String>): Cursor =
            query(tablename, columns, null, null)

        fun Cursor.getNullableInt(columnName: String): Int? =
            getColumnIndex(columnName).let { if (isNull(it)) null else getInt(it) }

        fun Cursor.getInt(columnName: String): Int = getInt(getColumnIndex(columnName))

        fun Cursor.getBoolean(columnName: String): Boolean = getInt(getColumnIndex(columnName)) == 1

        fun Cursor.getLong(columnName: String): Long? =
            getColumnIndex(columnName).let { if (isNull(it)) null else getLong(it) }

        fun Cursor.getString(columnName: String): String? = getString(getColumnIndex(columnName))

        fun Disposable.notDisposed(): Boolean = !isDisposed

        fun <T : Any> Collection<T>?.nullOrEmpty(): Boolean = if (this == null) true else size == 0

        fun <T : Any> Collection<T>?.nonEmpty(): Boolean = if (this == null) false else size > 0

        fun CharSequence?.nonEmpty(): Boolean = if (this == null) false else length > 0

        fun <T : Any> Flowable<T>.subscribeIgnoreErrors(consumer: Consumer<in T>): Disposable =
            subscribe(consumer, RxUtils.ignore())

        fun <T : Any> Single<T>.subscribeIgnoreErrors(consumer: Consumer<in T>): Disposable =
            subscribe(consumer, RxUtils.ignore())

        fun Completable.subscribeIOAndIgnoreResults(): Disposable =
            subscribeOn(Schedulers.io()).subscribe(RxUtils.dummy(), RxUtils.ignore())

        fun View.fadeOut(duration: Long, onEnd: () -> Unit = {}) {
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

        fun View.fadeIn(duration: Long, onEnd: () -> Unit = {}) {
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
    }
}