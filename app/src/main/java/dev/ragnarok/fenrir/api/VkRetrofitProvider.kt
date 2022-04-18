package dev.ragnarok.fenrir.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dev.ragnarok.fenrir.api.RetrofitWrapper.Companion.wrap
import dev.ragnarok.fenrir.api.adapters.*
import dev.ragnarok.fenrir.api.model.*
import dev.ragnarok.fenrir.api.model.database.SchoolClazzDto
import dev.ragnarok.fenrir.api.model.feedback.UserArray
import dev.ragnarok.fenrir.api.model.feedback.VKApiBaseFeedback
import dev.ragnarok.fenrir.api.model.local_json.ChatJsonResponse
import dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent
import dev.ragnarok.fenrir.api.model.response.ChatsInfoResponse
import dev.ragnarok.fenrir.api.model.response.CustomCommentsResponse
import dev.ragnarok.fenrir.api.model.response.LikesListResponse
import dev.ragnarok.fenrir.api.model.response.NewsfeedCommentsResponse.Dto
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse
import dev.ragnarok.fenrir.model.AnswerVKOfficialList
import dev.ragnarok.fenrir.settings.IProxySettings
import dev.ragnarok.fenrir.settings.Settings
import io.reactivex.rxjava3.core.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class VkRetrofitProvider(
    private val proxyManager: IProxySettings,
    private val clientFactory: IVkMethodHttpClientFactory
) : IVkRetrofitProvider {
    private val retrofitCacheLock = Any()
    private val serviceRetrofitLock = Any()

    private val retrofitCache = Collections.synchronizedMap(HashMap<Int, RetrofitWrapper>(1))

    @Volatile
    private var serviceRetrofit: RetrofitWrapper? = null
    private fun onProxySettingsChanged() {
        synchronized(retrofitCacheLock) {
            for ((_, value) in retrofitCache) {
                value?.cleanup()
            }
            retrofitCache.clear()
        }
    }

    override fun provideNormalRetrofit(accountId: Int): Single<RetrofitWrapper> {
        return Single.fromCallable {
            var retrofit: RetrofitWrapper?
            synchronized(retrofitCacheLock) {
                retrofit = retrofitCache[accountId]
                if (retrofit != null) {
                    return@fromCallable retrofit
                }
                val client = clientFactory.createDefaultVkHttpClient(
                    accountId,
                    vkgson,
                    proxyManager.activeProxy
                )
                retrofit = createDefaultVkApiRetrofit(client)
                retrofitCache.put(accountId, retrofit)
            }
            retrofit
        }
    }

    override fun provideCustomRetrofit(accountId: Int, token: String): Single<RetrofitWrapper> {
        return Single.fromCallable {
            val client = clientFactory.createCustomVkHttpClient(
                accountId,
                token,
                vkgson,
                proxyManager.activeProxy
            )
            createDefaultVkApiRetrofit(client)
        }
    }

    override fun provideServiceRetrofit(): Single<RetrofitWrapper> {
        return Single.fromCallable {
            if (serviceRetrofit == null) {
                synchronized(serviceRetrofitLock) {
                    if (serviceRetrofit == null) {
                        val client = clientFactory.createServiceVkHttpClient(
                            vkgson,
                            proxyManager.activeProxy
                        )
                        serviceRetrofit = createDefaultVkApiRetrofit(client)
                    }
                }
            }
            serviceRetrofit
        }
    }

    override fun provideNormalHttpClient(accountId: Int): Single<OkHttpClient> {
        return Single.fromCallable {
            clientFactory.createDefaultVkHttpClient(
                accountId,
                vkgson,
                proxyManager.activeProxy
            )
        }
    }

    private fun createDefaultVkApiRetrofit(okHttpClient: OkHttpClient): RetrofitWrapper {
        return wrap(
            Retrofit.Builder()
                .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/")
                .addConverterFactory(GSON_CONVERTER_FACTORY)
                .addCallAdapterFactory(RX_ADAPTER_FACTORY)
                .client(okHttpClient)
                .build()
        )
    }

    companion object {
        val vkgson: Gson = GsonBuilder()
            .registerTypeAdapter(AnswerVKOfficialList::class.java, AnswerVKOfficialDtoAdapter())
            .registerTypeAdapter(VKApiAttachments.Entry::class.java, AttachmentsEntryDtoAdapter())
            .registerTypeAdapter(VKApiDoc.Entry::class.java, DocsEntryDtoAdapter())
            .registerTypeAdapter(VKApiPhoto::class.java, PhotoDtoAdapter())
            .registerTypeAdapter(Boolean::class.javaPrimitiveType, BooleanAdapter())
            .registerTypeAdapter(Boolean::class.javaObjectType, BooleanAdapter())
            .registerTypeAdapter(VKApiPrivacy::class.java, PrivacyDtoAdapter())
            .registerTypeAdapter(VKApiPhotoAlbum::class.java, PhotoAlbumDtoAdapter())
            .registerTypeAdapter(VKApiVideoAlbum::class.java, VideoAlbumDtoAdapter())
            .registerTypeAdapter(VKApiAttachments::class.java, AttachmentsDtoAdapter())
            .registerTypeAdapter(VKApiAudio::class.java, AudioDtoAdapter())
            .registerTypeAdapter(VKApiPost::class.java, PostDtoAdapter())
            .registerTypeAdapter(VKApiPostSource::class.java, PostSourceDtoAdapter())
            .registerTypeAdapter(VKApiUser::class.java, UserDtoAdapter())
            .registerTypeAdapter(VKApiCommunity::class.java, CommunityDtoAdapter())
            .registerTypeAdapter(VKApiBaseFeedback::class.java, FeedbackDtoAdapter())
            .registerTypeAdapter(VKApiComment::class.java, CommentDtoAdapter())
            .registerTypeAdapter(VKApiVideo::class.java, VideoDtoAdapter())
            .registerTypeAdapter(UserArray::class.java, FeedbackUserArrayDtoAdapter())
            .registerTypeAdapter(VKApiMessage::class.java, MessageDtoAdapter())
            .registerTypeAdapter(VKApiNews::class.java, NewsAdapter())
            .registerTypeAdapter(AbsLongpollEvent::class.java, LongpollUpdateAdapter())
            .registerTypeAdapter(ChatsInfoResponse::class.java, ChatsInfoAdapter())
            .registerTypeAdapter(VKApiChat::class.java, ChatDtoAdapter())
            .registerTypeAdapter(ChatUserDto::class.java, ChatUserDtoAdapter())
            .registerTypeAdapter(SchoolClazzDto::class.java, SchoolClazzDtoAdapter())
            .registerTypeAdapter(LikesListResponse::class.java, LikesListAdapter())
            .registerTypeAdapter(Dto::class.java, NewsfeedCommentDtoAdapter())
            .registerTypeAdapter(VKApiTopic::class.java, TopicDtoAdapter())
            .registerTypeAdapter(GroupSettingsDto::class.java, GroupSettingsAdapter())
            .registerTypeAdapter(
                CustomCommentsResponse::class.java,
                CustomCommentsResponseAdapter()
            )
            .registerTypeAdapter(VKApiAudioPlaylist::class.java, AudioPlaylistDtoAdapter())
            .registerTypeAdapter(VKApiStory::class.java, StoryDtoAdapter())
            .registerTypeAdapter(FaveLinkDto::class.java, FaveLinkDtoAdapter())
            .registerTypeAdapter(VKApiArticle::class.java, ArticleDtoAdapter())
            .registerTypeAdapter(VKApiCatalogLink::class.java, VKApiCatalogLinkDtoAdapter())
            .registerTypeAdapter(ChatJsonResponse::class.java, ChatJsonResponseDtoAdapter())
            .registerTypeAdapter(VKApiJsonString::class.java, JsonStringDtoAdapter())
            .registerTypeAdapter(
                VKApiProfileInfoResponce::class.java,
                ProfileInfoResponceDtoAdapter()
            )
            .registerTypeAdapter(VKApiMarket::class.java, MarketDtoAdapter())
            .registerTypeAdapter(
                ServicePlaylistResponse::class.java,
                ServicePlaylistResponseDtoAdapter()
            )
            .create()
        private val GSON_CONVERTER_FACTORY = GsonConverterFactory.create(vkgson)
        private val RX_ADAPTER_FACTORY = RxJava3CallAdapterFactory.create()
    }

    init {
        proxyManager.observeActive()
            .subscribe { onProxySettingsChanged() }
    }
}