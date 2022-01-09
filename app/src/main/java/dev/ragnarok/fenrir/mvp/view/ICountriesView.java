package dev.ragnarok.fenrir.mvp.view;

import java.util.List;

import dev.ragnarok.fenrir.model.database.Country;
import dev.ragnarok.fenrir.mvp.core.IMvpView;


public interface ICountriesView extends IMvpView, IErrorView {
    void displayData(List<Country> countries);

    void notifyDataSetChanged();

    void displayLoading(boolean loading);

    void returnSelection(Country country);
}