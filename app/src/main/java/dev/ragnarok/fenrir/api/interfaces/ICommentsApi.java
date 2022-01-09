package dev.ragnarok.fenrir.api.interfaces;

import androidx.annotation.CheckResult;

import dev.ragnarok.fenrir.api.model.response.CustomCommentsResponse;
import io.reactivex.rxjava3.core.Single;


public interface ICommentsApi {

    // {"response":{"main":false,"first_id":null,"last_id":null,"admin_level":0},"execute_errors":[{"method":"video.getComments","error_code":18,"error_msg":"User was deleted or banned"},{"method":"video.getComments","error_code":18,"error_msg":"User was deleted or banned"},{"method":"video.getComments","error_code":18,"error_msg":"User was deleted or banned"},{"method":"execute.getComments","error_code":18,"error_msg":"User was deleted or banned"}]}
    @CheckResult
    Single<CustomCommentsResponse> get(String sourceType, int ownerId, int sourceId, Integer offset, Integer count,
                                       String sort, Integer startCommentId, Integer threadComment, String accessKey, String fields);
}
