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

    fun reset() {
        offsets.keys.toList().forEach { offsets[it] = 0 }
        finished.clear()
        previousPages.clear()
    }

    fun offset(sort: String): Int = offsets.getValue(sort)
    fun hasMore(sort: String): Boolean = sort !in finished

    fun received(sort: String, rawCount: Int, total: Int, ids: List<Int>? = null) {
        require(rawCount >= 0 && total >= 0)
        check(ids.isNullOrEmpty() || previousPages[sort] != ids) {
            "VK repeated a topic page at a different offset"
        }
        val next = offset(sort) + rawCount
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