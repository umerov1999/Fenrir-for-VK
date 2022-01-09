package dev.ragnarok.fenrir.api.impl;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.IServiceProvider;
import dev.ragnarok.fenrir.api.interfaces.IDatabaseApi;
import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCity;
import dev.ragnarok.fenrir.api.model.VKApiCountry;
import dev.ragnarok.fenrir.api.model.database.ChairDto;
import dev.ragnarok.fenrir.api.model.database.FacultyDto;
import dev.ragnarok.fenrir.api.model.database.SchoolClazzDto;
import dev.ragnarok.fenrir.api.model.database.SchoolDto;
import dev.ragnarok.fenrir.api.model.database.UniversityDto;
import dev.ragnarok.fenrir.api.services.IDatabaseService;
import io.reactivex.rxjava3.core.Single;


class DatabaseApi extends AbsApi implements IDatabaseApi {

    DatabaseApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<List<VKApiCity>> getCitiesById(Collection<Integer> cityIds) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getCitiesById(join(cityIds, ",", Object::toString))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiCountry>> getCountries(Boolean needAll, String code, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service.getCountries(integerFromBoolean(needAll), code, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<SchoolClazzDto>> getSchoolClasses(Integer countryId) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getSchoolClasses(countryId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<ChairDto>> getChairs(int facultyId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getChairs(facultyId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<FacultyDto>> getFaculties(int universityId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getFaculties(universityId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<UniversityDto>> getUniversities(String query, Integer countryId, Integer cityId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getUniversities(query, countryId, cityId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<SchoolDto>> getSchools(String query, int cityId, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getSchools(query, cityId, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiCity>> getCities(int countryId, Integer regionId, String query, Boolean needAll, Integer offset, Integer count) {
        return provideService(IDatabaseService.class)
                .flatMap(service -> service
                        .getCities(countryId, regionId, query, integerFromBoolean(needAll), offset, count)
                        .map(extractResponseWithErrorHandling()));
    }
}
