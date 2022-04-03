package dev.ragnarok.fenrir.util

class Optional<T> private constructor(private val value: T?) {
    fun get(): T? {
        return value
    }

    val isEmpty: Boolean
        get() = value == null

    fun nonEmpty(): Boolean {
        return value != null
    }

    fun requareNonEmpty(): T {
        return value!!
    }

    companion object {
        @JvmStatic
        fun <T> wrap(value: T?): Optional<T> {
            return Optional(value)
        }

        @JvmStatic
        fun <T> empty(): Optional<T> {
            return Optional(null)
        }
    }

}