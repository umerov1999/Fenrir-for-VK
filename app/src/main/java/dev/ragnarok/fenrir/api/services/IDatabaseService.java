package dev.ragnarok.fenrir.api.services;

import java.util.List;

import dev.ragnarok.fenrir.api.model.Items;
import dev.ragnarok.fenrir.api.model.VKApiCity;
import dev.ragnarok.fenrir.api.model.VKApiCountry;
import dev.ragnarok.fenrir.api.model.database.ChairDto;
import dev.ragnarok.fenrir.api.model.database.FacultyDto;
import dev.ragnarok.fenrir.api.model.database.SchoolClazzDto;
import dev.ragnarok.fenrir.api.model.database.SchoolDto;
import dev.ragnarok.fenrir.api.model.database.UniversityDto;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface IDatabaseService {

    //https://vk.com/dev/database.getCitiesById
    @FormUrlEncoded
    @POST("database.getCities")
    Single<BaseResponse<List<VKApiCity>>> getCitiesById(@Field("city_ids") String cityIds);

    /**
     * Returns a list of countries.
     *
     * @param needAll 1 — to return a full list of all countries
     *                0 — to return a list of countries near the current user's country (default).
     * @param code    Country codes in ISO 3166-1 alpha-2 standard.
     * @param offset  Offset needed to return a specific subset of countries.
     * @param count   Number of countries to return. Default 100, maximum value 1000
     * @return Returns the total results number in count field and an array of objects describing countries in items field
     */
    @FormUrlEncoded
    @POST("database.getCountries")
    Single<BaseResponse<Items<VKApiCountry>>> getCountries(@Field("need_all") Integer needAll,
                                                           @Field("code") String code,
                                                           @Field("offset") Integer offset,
                                                           @Field("count") Integer count);

    /**
     * Returns a list of school classes specified for the country.
     *
     * @param countryId Country ID.
     * @return Returns an array of objects, each of them is a pair of class ID and definition.
     */
    @FormUrlEncoded
    @POST("database.getSchoolClasses")
    Single<BaseResponse<List<SchoolClazzDto>>> getSchoolClasses(@Field("country_id") Integer countryId);

    /**
     * Returns list of chairs on a specified faculty.
     *
     * @param facultyId id of the faculty to get chairs from
     * @param offset    offset required to get a certain subset of chairs
     * @param count     amount of chairs to get. Default 100, maximum value 10000
     * @return the total results number in count field and an array of objects describing chairs in items field
     */
    @FormUrlEncoded
    @POST("database.getChairs")
    Single<BaseResponse<Items<ChairDto>>> getChairs(@Field("faculty_id") int facultyId,
                                                    @Field("offset") Integer offset,
                                                    @Field("count") Integer count);

    /**
     * Returns a list of faculties (i.e., university departments).
     *
     * @param universityId University ID.
     * @param offset       Offset needed to return a specific subset of faculties.
     * @param count        Number of faculties to return. Default 100, maximum value 10000
     * @return the total results number in count field and an array
     * of objects describing faculties in items field
     */
    @FormUrlEncoded
    @POST("database.getFaculties")
    Single<BaseResponse<Items<FacultyDto>>> getFaculties(@Field("university_id") int universityId,
                                                         @Field("offset") Integer offset,
                                                         @Field("count") Integer count);

    /**
     * Returns a list of higher education institutions.
     *
     * @param query     Search query.
     * @param countryId Country ID.
     * @param cityId    City ID.
     * @param offset    Offset needed to return a specific subset of universities.
     * @param count     Number of universities to return. Default 100, maximum value 10000
     * @return an array of objects describing universities
     */
    @FormUrlEncoded
    @POST("database.getUniversities")
    Single<BaseResponse<Items<UniversityDto>>> getUniversities(@Field("q") String query,
                                                               @Field("country_id") Integer countryId,
                                                               @Field("city_id") Integer cityId,
                                                               @Field("offset") Integer offset,
                                                               @Field("count") Integer count);

    /**
     * Returns a list of schools.
     *
     * @param query  Search query.
     * @param cityId City ID.
     * @param offset Offset needed to return a specific subset of schools.
     * @param count  Number of schools to return. Default 100, maximum value 10000
     * @return an array of objects describing schools
     */
    @FormUrlEncoded
    @POST("database.getSchools")
    Single<BaseResponse<Items<SchoolDto>>> getSchools(@Field("q") String query,
                                                      @Field("city_id") int cityId,
                                                      @Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    /**
     * Returns a list of cities.
     *
     * @param countryId Country ID.
     * @param regionId  Region ID.
     * @param query     Search query.
     * @param needAll   1 — to return all cities in the country
     *                  0 — to return major cities in the country (default)
     * @param offset    Offset needed to return a specific subset of cities.
     * @param count     Number of cities to return. Default 100, maximum value 1000
     * @return the total results number in count field and an array of objects describing cities in items field
     */
    @FormUrlEncoded
    @POST("database.getCities")
    Single<BaseResponse<Items<VKApiCity>>> getCities(@Field("country_id") int countryId,
                                                     @Field("region_id") Integer regionId,
                                                     @Field("q") String query,
                                                     @Field("need_all") Integer needAll,
                                                     @Field("offset") Integer offset,
                                                     @Field("count") Integer count);
}
