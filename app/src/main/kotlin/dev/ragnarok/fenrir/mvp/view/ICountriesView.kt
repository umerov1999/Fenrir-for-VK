package dev.ragnarok.fenrir.mvp.view

import dev.ragnarok.fenrir.model.database.Country
import dev.ragnarok.fenrir.mvp.core.IMvpView

interface ICountriesView : IMvpView, IErrorView {
    fun displayData(countries: List<Country>)
    fun notifyDataSetChanged()
    fun displayLoading(loading: Boolean)
    fun returnSelection(country: Country)
}