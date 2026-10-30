package dev.ragnarok.fenrir.fragment.comments

/** Offsets count raw VK items, never the size of the deduplicated UI/cache. */
internal class TopicCommentsPager {
    private val offsets = mutableMapOf("asc" to 0, "desc" to 0)
    private val previousPages = mutableMapOf<String, List<Int>>()
    private val finished = mutableSetOf<String>()

    data class Page<T>(val items: List<T>, val total: Int)

    suspend fun <T> load(
        sort: String,
        loadedIds: Set<Int>,
        id: (T) -> Int,
        accept: (T) -> Boolean = { true },
        request: suspend (Int) -> Page<T>
    ): Page<T> {
        check(hasMore(sort)) { "Topic pagination has reached the end" }
        do {
            val page = request(offset(sort))
            val fresh = fresh(page.items, loadedIds, id).filter(accept)
            received(sort, page.items.size, page.total, page.items.map(id))
            if (fresh.isNotEmpty() || !hasMore(sort)) return Page(fresh, page.total)
        } while (hasMore(sort))
        error("Topic pagination made no progress")
    }

    fun <T> fresh(items: List<T>, loadedIds: Set<Int>, id: (T) -> Int): List<T> {
        val seen = HashSet(loadedIds)
        return items.filter { seen.add(id(it)) }
    }

    /** Stage a request privately; publish this cursor only with its displayed rows. */
    fun copy(): TopicCommentsPager = TopicCommentsPager().also { candidate ->
        candidate.offsets.putAll(offsets)
        candidate.previousPages.putAll(previousPages.mapValues { it.value.toList() })
        candidate.finished.addAll(finished)
    }

    fun reset() {
        offsets.keys.toList().forEach { offsets[it] = 0 }
        finished.clear()
        previousPages.clear()
    }

    /** A page number is one-based; API offsets are zero-based in the requested order. */
    companion object {
        const val PAGE_SIZE = 20
        fun parseOffset(input: String, page: Boolean): Int {
            val value = input.trim().toLongOrNull()
            require(value != null && value >= if (page) 1L else 0L) { "Invalid position" }
            val max = if (page) Int.MAX_VALUE.toLong() / PAGE_SIZE + 1 else Int.MAX_VALUE.toLong()
            require(value <= max) { "Position is too large" }
            return (if (page) (value - 1) * PAGE_SIZE else value).toInt()
        }
    }

    fun seek(sort: String, start: Int, rawCount: Int, total: Int) {
        require(start >= 0 && rawCount >= 0 && total >= 0)
        require(start == 0 && total == 0 || start < total) { "Position is outside the topic" }
        require(rawCount > 0 || total == 0) { "Empty topic page" }
        val end = start.toLong() + rawCount
        require(end <= total) { "Inconsistent topic count" }
        reset()
        offsets[sort] = end.toInt()
        val opposite = if (sort == "asc") "desc" else "asc"
        offsets[opposite] = total - start
        if (end >= total) finished.add(sort)
        if (start == 0) finished.add(opposite)
    }

    fun offset(sort: String): Int = offsets.getValue(sort)
    fun hasMore(sort: String): Boolean = sort !in finished

    fun received(sort: String, rawCount: Int, total: Int, ids: List<Int>? = null) {
        require(rawCount >= 0 && total >= 0)
        check(ids.isNullOrEmpty() || previousPages[sort] != ids) {
            "VK repeated a topic page at a different offset"
        }
        val next = (offset(sort).toLong() + rawCount).also { require(it <= Int.MAX_VALUE) }.toInt()
        // An empty page before the advertised end is not proof of exhaustion.
        // Keep the request unchanged so a transient API inconsistency is retryable.
        check(rawCount != 0 || next >= total) { "VK returned an empty topic page before the end" }
        if (ids != null) previousPages[sort] = ids.toList()
        offsets[sort] = next
        if (next >= total) finished.add(sort)
    }

    fun initial(sort: String, rawCount: Int, total: Int, focused: Boolean) {
        reset()
        if (!focused) {
            received(sort, rawCount, total)
            finished.add(if (sort == "asc") "desc" else "asc")
        }
    }
}
