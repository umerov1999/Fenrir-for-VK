package dev.ragnarok.fenrir.domain;

import java.util.List;

import dev.ragnarok.fenrir.model.City;
import dev.ragnarok.fenrir.model.database.Chair;
import dev.ragnarok.fenrir.model.database.Country;
import dev.ragnarok.fenrir.model.database.Faculty;
import dev.ragnarok.fenrir.model.database.School;
import dev.ragnarok.fenrir.model.database.SchoolClazz;
import dev.ragnarok.fenrir.model.database.University;
import io.reactivex.rxjava3.core.Single;

public interface IDatabaseInteractor {
    Single<List<Chair>> getChairs(int accountId, int facultyId, int count, int offset);

    Single<List<Country>> getCountries(int accountId, boolean ignoreCache);

    Single<List<City>> getCities(int accountId, int countryId, String q, boolean needAll, int count, int offset);

    Single<List<Faculty>> getFaculties(int accountId, int universityId, int count, int offset);

    Single<List<SchoolClazz>> getSchoolClasses(int accountId, int countryId);

    Single<List<School>> getSchools(int accountId, int cityId, String q, int count, int offset);

    Single<List<University>> getUniversities(int accountId, String filter, Integer cityId, Integer countyId, int count, int offset);
}