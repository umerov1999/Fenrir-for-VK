package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCity;
import dev.ragnarok.fenrir.api.model.VKApiCountry;
import dev.ragnarok.fenrir.api.model.database.ChairDto;
import dev.ragnarok.fenrir.api.model.database.FacultyDto;
import dev.ragnarok.fenrir.api.model.database.SchoolClazzDto;
import dev.ragnarok.fenrir.api.model.database.SchoolDto;
import dev.ragnarok.fenrir.api.model.database.UniversityDto;
import io.reactivex.rxjava3.core.Single;


public interface IDatabaseApi {

    @CheckResult
    Single<List<VKApiCity>> getCitiesById(Collection<Integer> cityIds);

    @CheckResult
    Single<Items<VKApiCountry>> getCountries(Boolean needAll, String code, Integer offset, Integer count);

    @CheckResult
    Single<List<SchoolClazzDto>> getSchoolClasses(Integer countryId);

    @CheckResult
    Single<Items<ChairDto>> getChairs(int facultyId, Integer offset, Integer count);

    @CheckResult
    Single<Items<FacultyDto>> getFaculties(int universityId, Integer offset, Integer count);

    @CheckResult
    Single<Items<UniversityDto>> getUniversities(String query, Integer countryId, Integer cityId,
                                                 Integer offset, Integer count);

    @CheckResult
    Single<Items<SchoolDto>> getSchools(String query, int cityId, Integer offset, Integer count);

    @CheckResult
    Single<Items<VKApiCity>> getCities(int countryId, Integer regionId, String query, Boolean needAll, Integer offset, Integer count);
}
