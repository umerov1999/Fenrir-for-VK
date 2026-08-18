package dev.ragnarok.fenrir.model

import androidx.annotation.IntDef

@IntDef(
    BlockReason.OTHER,
    BlockReason.SPAM,
    BlockReason.VERBAL_ABUSE,
    BlockReason.STRONG_LANGUAGE,
    BlockReason.IRRELEVANT_MESSAGES
)
@Retention(AnnotationRetention.SOURCE)
annotation class BlockReason {
    companion object {
        const val OTHER = 0
        const val SPAM = 1
        const val VERBAL_ABUSE = 2
        const val STRONG_LANGUAGE = 3
        const val IRRELEVANT_MESSAGES = 4
    }
}