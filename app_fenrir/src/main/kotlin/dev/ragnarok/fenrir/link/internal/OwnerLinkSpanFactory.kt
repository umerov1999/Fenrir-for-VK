package dev.ragnarok.fenrir.link.internal

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.orZero
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.util.Utils.safeCountOfMultiple
import kotlin.math.abs

object OwnerLinkSpanFactory {
    private val ownerPattern: Regex =
        Regex("\\[(id|club|event|public)(\\d+)\\|([^]]+)]|\\[(https:[^]]+|http:[^]]+|vk\\.(?:ru|com|me)[^]]+|)\\|([^]]+)]")
    private val topicCommentPattern: Regex =
        Regex("\\[(id|club|event|public)(\\d*):bp(-\\d*)_(\\d*)\\|([^]]+)]")

    fun findPatterns(
        input: String?,
        owners: Boolean,
        topics: Boolean
    ): List<AbsInternalLink>? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val ownerLinks = if (owners) findOwnersLinks(input) else null
        val topicLinks = if (topics) findTopicLinks(input) else null
        val count = safeCountOfMultiple(ownerLinks, topicLinks)
        if (count > 0) {
            val all: MutableList<AbsInternalLink> = ArrayList(count)
            if (ownerLinks.nonNullNoEmpty()) {
                all.addAll(ownerLinks)
            }
            if (topicLinks.nonNullNoEmpty()) {
                all.addAll(topicLinks)
            }
            all.sortBy { it.start }
            return all
        }
        return null
    }

    fun withSpans(
        input: CharSequence?,
        owners: Boolean,
        topics: Boolean,
        listener: ActionListener?
    ): CharSequence? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val ownerLinks = if (owners) findOwnersLinks(input) else null
        val topicLinks = if (topics) findTopicLinks(input) else null
        val count = safeCountOfMultiple(ownerLinks, topicLinks)
        if (count > 0) {
            val all: MutableList<AbsInternalLink> = ArrayList(count)
            if (ownerLinks.nonNullNoEmpty()) {
                all.addAll(ownerLinks)
            }
            if (topicLinks.nonNullNoEmpty()) {
                all.addAll(topicLinks)
            }
            all.sortBy { it.start }
            val result = SpannableStringBuilder(replace(input, all))
            for (link in all) {
                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        if (listener != null) {
                            when (link) {
                                is TopicLink -> {
                                    listener.onTopicLinkClicked(link)
                                }

                                is OwnerLink -> {
                                    listener.onOwnerClick(link.ownerId)
                                }

                                is UrlLink -> {
                                    listener.onUrlClick(link.url)
                                }
                            }
                        }
                    }
                }
                result.setSpan(
                    clickableSpan,
                    link.start,
                    link.end,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            return result
        }
        return input
    }

    private fun toLong(str: String?, powN: Int): Long {
        if (str.isNullOrEmpty()) {
            return Settings.get().accounts().current
        }
        try {
            return str.toLong() * powN
        } catch (_: NumberFormatException) {
        }
        return Settings.get().accounts().current
    }

    private fun findTopicLinks(input: CharSequence): List<TopicLink>? {
        return try {
            val res = topicCommentPattern.findAll(input)
            val links: MutableList<TopicLink> = ArrayList(res.count())
            for (i in res) {
                val link = TopicLink()
                val ownerType = i.groupValues.getOrNull(1)
                link.start = i.range.first
                link.end = i.range.last + 1
                link.replyToOwner = toLong(
                    i.groupValues.getOrNull(2),
                    if ("event" == ownerType || "club" == ownerType || "public" == ownerType) -1 else 1
                )
                link.topicOwnerId = toLong(i.groupValues.getOrNull(3), 1)
                link.replyToCommentId = i.groupValues.getOrNull(4)?.toInt().orZero()
                link.targetLine = i.groupValues.getOrNull(5)
                links.add(link)
            }
            links
        } catch (_: Exception) {
            null
        }
    }

    private fun findOwnersLinks(input: CharSequence): List<AbsInternalLink>? {
        return try {
            val res = ownerPattern.findAll(input)
            val links: MutableList<AbsInternalLink> = ArrayList(res.count())
            for (i in res) {
                if (i.groupValues.getOrNull(4).nonNullNoEmpty()) {
                    links.add(
                        UrlLink(
                            i.range.first,
                            i.range.last + 1,
                            i.groupValues.getOrNull(4).orEmpty(),
                            i.groupValues.getOrNull(5).orEmpty()
                        )
                    )
                } else {
                    val ownerType = i.groupValues.getOrNull(1)
                    val ownerId = toLong(
                        i.groupValues.getOrNull(2),
                        if ("event" == ownerType || "club" == ownerType || "public" == ownerType) -1 else 1
                    )
                    val name = i.groupValues.getOrNull(3)

                    links.add(OwnerLink(i.range.first, i.range.last + 1, ownerId, name.orEmpty()))
                }
            }
            links
        } catch (_: Exception) {
            null
        }
    }

    fun getTextWithCollapseOwnerLinks(input: CharSequence?): CharSequence? {
        if (input.isNullOrEmpty()) {
            return null
        }
        val links = findOwnersLinks(input)
        return replace(input, links)
    }

    private fun replace(input: CharSequence, links: List<AbsInternalLink>?): CharSequence {
        if (links.isNullOrEmpty()) {
            return input
        }
        val result = StringBuilder(input)
        for (y in links.indices) {
            val link = links[y]
            if (link.targetLine.isNullOrEmpty()) {
                continue
            }
            val origLength = link.end - link.start
            val newLength = link.targetLine?.length.orZero()
            shiftLinks(links, link, origLength - newLength)
            link.targetLine?.let { result.replace(link.start, link.end, it) }
            link.end -= (origLength - newLength)
        }
        return result.toString()
    }

    private fun shiftLinks(links: List<AbsInternalLink>?, after: AbsInternalLink?, count: Int) {
        links ?: return
        var shiftAllowed = false
        for (link in links) {
            if (shiftAllowed) {
                link.start -= count
                link.end -= count
            }
            if (link === after) {
                shiftAllowed = true
            }
        }
    }

    fun genOwnerLink(ownerId: Long, title: String?): String {
        return "[" + (if (ownerId > 0) "id" else "club") + abs(ownerId) + "|" + title + "]"
    }

    open class ActionListener {
        open fun onTopicLinkClicked(link: TopicLink) {}
        open fun onOwnerClick(ownerId: Long) {}
        open fun onUrlClick(url: String) {}
    }
}