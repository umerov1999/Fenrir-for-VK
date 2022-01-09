package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.getCauseIfRuntime;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.domain.IDatabaseInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.model.database.Country;
import dev.ragnarok.fenrir.mvp.presenter.base.RxSupportPresenter;
import dev.ragnarok.fenrir.mvp.view.ICountriesView;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.RxUtils;


public class CountriesPresenter extends RxSupportPresenter<ICountriesView> {

    private final int accountId;

    private final IDatabaseInteractor databaseInteractor;
    private final List<Country> filtered;
    private List<Country> countries;
    private String filter;
    private boolean loadingNow;

    public CountriesPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        this.accountId = accountId;
        countries = new ArrayList<>();
        filtered = new ArrayList<>();
        databaseInteractor = InteractorFactory.createDatabaseInteractor();

        requestData();
    }

    @Override
    public void onGuiCreated(@NonNull ICountriesView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(filtered);
        resolveLoadingView();
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveLoadingView();
    }

    private void resolveLoadingView() {
        callView(v -> v.displayLoading(loadingNow));
    }

    private void onDataReceived(List<Country> countries) {
        setLoadingNow(false);

        this.countries = countries;

        reFillFilteredData();
        callView(ICountriesView::notifyDataSetChanged);
    }

    public void fireFilterEdit(CharSequence text) {
        if (Objects.safeEquals(text.toString(), filter)) {
            return;
        }

        filter = text.toString();

        reFillFilteredData();
        callView(ICountriesView::notifyDataSetChanged);
    }

    private void reFillFilteredData() {
        filtered.clear();

        if (isEmpty(filter)) {
            filtered.addAll(countries);
            return;
        }

        String lowerFilter = filter.toLowerCase();

        for (Country country : countries) {
            if (country.getTitle().toLowerCase().contains(lowerFilter)) {
                filtered.add(country);
            }
        }
    }

    private void onDataGetError(Throwable t) {
        setLoadingNow(false);
        callView(v -> showError(v, getCauseIfRuntime(t)));
    }

    private void requestData() {
        setLoadingNow(true);
        appendDisposable(databaseInteractor.getCountries(accountId, false)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    public void fireCountryClick(Country country) {
        callView(v -> v.returnSelection(country));
    }
}