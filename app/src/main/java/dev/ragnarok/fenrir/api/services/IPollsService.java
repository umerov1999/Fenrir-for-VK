package dev.ragnarok.fenrir.api.services;

import dev.ragnarok.fenrir.api.model.VKApiPoll;
import dev.ragnarok.fenrir.api.model.response.BaseResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;


public interface IPollsService {

    @FormUrlEncoded
    @POST("polls.create")
    Single<BaseResponse<VKApiPoll>> create(@Field("question") String question,
                                           @Field("is_anonymous") Integer isAnonymous,
                                           @Field("is_multiple") Integer isMultiple,
                                           @Field("owner_id") Integer ownerId,
                                           @Field("add_answers") String addAnswers);

    //https://vk.com/dev/polls.deleteVote
    @FormUrlEncoded
    @POST("polls.deleteVote")
    Single<BaseResponse<Integer>> deleteVote(@Field("owner_id") Integer ownerId,
                                             @Field("poll_id") int pollId,
                                             @Field("answer_id") int answerId,
                                             @Field("is_board") Integer isBoard);

    //https://vk.com/dev/polls.addVote
    @FormUrlEncoded
    @POST("polls.addVote")
    Single<BaseResponse<Integer>> addVote(@Field("owner_id") Integer ownerId,
                                          @Field("poll_id") int pollId,
                                          @Field("answer_ids") String answerIds,
                                          @Field("is_board") Integer isBoard);

    @FormUrlEncoded
    @POST("polls.getById")
    Single<BaseResponse<VKApiPoll>> getById(@Field("owner_id") Integer ownerId,
                                            @Field("is_board") Integer isBoard,
                                            @Field("poll_id") Integer pollId);

}
