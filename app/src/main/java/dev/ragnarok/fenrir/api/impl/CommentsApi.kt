package dev.ragnarok.fenrir.api.impl

import dev.ragnarok.fenrir.Constants
import dev.ragnarok.fenrir.api.IServiceProvider
import dev.ragnarok.fenrir.api.interfaces.ICommentsApi
import dev.ragnarok.fenrir.api.model.response.CustomCommentsResponse
import dev.ragnarok.fenrir.api.services.ICommentsService
import io.reactivex.rxjava3.core.Single

internal class CommentsApi(accountId: Int, provider: IServiceProvider) :
    AbsApi(accountId, provider), ICommentsApi {
    override fun get(
        sourceType: String?,
        ownerId: Int,
        sourceId: Int,
        offset: Int?,
        count: Int?,
        sort: String?,
        startCommentId: Int?,
        threadComment: Int?,
        accessKey: String?,
        fields: String?
    ): Single<CustomCommentsResponse> {
        val thread_id = threadComment ?: 0
        return provideService(ICommentsService::class.java)
            .flatMap { service: ICommentsService ->
                service["""var comment_id = Args.comment_id;
var owner_id = Args.owner_id;
var source_id = Args.source_id;
var start_comment_id = Args.start_comment_id;
var offset = Args.offset;
var count = Args.count;
var sort = Args.sort;
var access_key = Args.access_key;
var source_type = Args.source_type;
var fields = Args.fields;

var positive_group_id;
if(owner_id < 0){
    positive_group_id = -owner_id;
} else {
    positive_group_id = owner_id;
}

var admin_level = 0;
if(owner_id < 0){
    admin_level = API.groups.getById({"v":"${Constants.API_VERSION}","group_id":positive_group_id,
        "fields":"admin_level"})[0].admin_level;
}

var result;
if(source_type == "post"){
    if(comment_id != 0)
    {
    	result = API.wall.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "post_id":source_id, 
        "need_likes":"1", 
        "start_comment_id":start_comment_id, 
        "offset":offset, 
        "count":count, 
        "sort":sort, 
        "preview_length":"0", 
        "extended":"1",
        "comment_id":comment_id,
        "thread_items_count":"10",
        "fields":fields
    });
    }
    else
    {
    result = API.wall.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "post_id":source_id, 
        "need_likes":"1", 
        "start_comment_id":start_comment_id, 
        "offset":offset, 
        "count":count, 
        "sort":sort, 
        "preview_length":"0", 
        "extended":"1",
        "thread_items_count":"10",
        "fields":fields
    });
    }
}

if(source_type == "photo"){
    result = API.photos.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "photo_id":source_id, 
        "need_likes":"1", 
        "start_comment_id":start_comment_id, 
        "offset":offset, 
        "count":count, 
        "sort":sort, 
        "extended":"1",
        "fields":fields,
        "access_key":access_key});
}

if(source_type == "video"){
    result = API.video.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "video_id":source_id, 
        "need_likes":"1", 
        "start_comment_id":start_comment_id, 
        "offset":offset, 
        "count":count, 
        "sort":sort, 
        "extended":"1",
        "fields":fields
    });
}

if(source_type == "topic"){
    result = API.board.getComments({"v":"${Constants.API_VERSION}","group_id":positive_group_id, 
        "topic_id":source_id, 
        "need_likes":"1", 
        "start_comment_id":start_comment_id, 
        "offset":offset, 
        "count":count, 
        "sort":sort, 
        "extended":"1"});
}

var first_id;
if(source_type == "post"){
   if(comment_id != 0)
   {
   first_id = API.wall.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "post_id":source_id, "count":"1", "sort":"asc", "comment_id":comment_id, "thread_items_count":"10"}).items[0].id;
   }
   else
   {
   first_id = API.wall.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "post_id":source_id, "count":"1", "sort":"asc", "thread_items_count":"10"}).items[0].id;
   }
}

if(source_type == "photo"){
   first_id = API.photos.getComments({"v":"${Constants.API_VERSION}",
        "owner_id":owner_id, 
        "photo_id":source_id, 
        "count":"1", 
        "sort":"asc", 
        "access_key":access_key}).items[0].id;
}

if(source_type == "video"){
   first_id = API.video.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "video_id":source_id, "count":"1", "sort":"asc"}).items[0].id;
}

if(source_type == "topic"){
    first_id = API.board.getComments({"v":"${Constants.API_VERSION}","group_id":positive_group_id, 
        "topic_id":source_id, "count":"1", "sort":"asc"}).items[0].id;
}

var last_id;
if(source_type == "post"){
    if(comment_id != 0)
    {
    last_id = API.wall.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "post_id":source_id, "count":"1", "sort":"desc", "comment_id":comment_id, "thread_items_count":"10"}).items[0].id;
    }
    else
    {
    last_id = API.wall.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "post_id":source_id, "count":"1", "sort":"desc", "thread_items_count":"10"}).items[0].id;
    }
}

if(source_type == "photo"){
    last_id = API.photos.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "photo_id":source_id, "count":"1", "sort":"desc"}).items[0].id;
}

if(source_type == "video"){
    last_id = API.video.getComments({"v":"${Constants.API_VERSION}","owner_id":owner_id, 
        "video_id":source_id, "count":"1", "sort":"desc"}).items[0].id;
}

if(source_type == "topic"){
    last_id = API.board.getComments({"v":"${Constants.API_VERSION}","group_id":positive_group_id, 
        "topic_id":source_id, "count":"1", "sort":"desc"}).items[0].id;
}

return {"main": result, 
    "first_id": first_id, 
    "last_id": last_id, 
    "admin_level": admin_level};""", sourceType, ownerId, sourceId, offset, count, sort, startCommentId, thread_id, accessKey, fields]
                    .map(handleExecuteErrors("execute.getComments"))
                    .map(extractResponseWithErrorHandling())
            }
    }
}