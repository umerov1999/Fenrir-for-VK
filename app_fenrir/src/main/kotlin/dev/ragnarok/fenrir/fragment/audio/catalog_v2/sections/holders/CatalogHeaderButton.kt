package dev.ragnarok.fenrir.fragment.audio.catalog_v2.sections.holders

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.model.catalog_v2_audio.CatalogV2Button
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.settings.Settings

class CatalogHeaderButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
    private val layoutInflater: LayoutInflater
    private var simpleText: TextView? = null
    private var dropdownText: TextView? = null
    private var clearButton: View? = null

    init {
        layoutInflater = LayoutInflater.from(context)
    }

    fun setUpWithCatalogAction(catalogAction: CatalogV2Button) {
        when (catalogAction.action?.type) {
            "open_section" -> {
                if (clearButton != null && clearButton?.visibility != GONE) {
                    clearButton?.visibility = GONE
                }
                if (dropdownText != null && dropdownText?.visibility != GONE) {
                    dropdownText?.visibility = GONE
                }
                if (simpleText == null) {
                    simpleText = layoutInflater.inflate(
                        R.layout.item_catalog_v2_header_button,
                        this, false
                    ) as TextView?
                    addView(simpleText)
                }
                if (simpleText?.visibility != VISIBLE) {
                    simpleText?.visibility = VISIBLE
                }
                simpleText?.text = catalogAction.title
                simpleText?.setOnClickListener {
                    catalogAction.section_id?.let { it1 ->
                        PlaceFactory.getCatalogV2AudioSectionPlace(
                            Settings.get().accounts().current,
                            it1
                        ).tryOpenWith(context)
                    }
                }
            }
        }
    }
}