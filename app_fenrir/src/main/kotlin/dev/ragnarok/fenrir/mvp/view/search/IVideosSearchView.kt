package dev.ragnarok.fenrir.mvp.view.search

import dev.ragnarok.fenrir.model.Video

interface IVideosSearchView : IBaseSearchView<Video> {
    fun returnSelectionToParent(video: Video)
}