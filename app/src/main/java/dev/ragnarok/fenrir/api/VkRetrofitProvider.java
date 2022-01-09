package dev.ragnarok.fenrir.api;

import static dev.ragnarok.fenrir.util.Objects.isNull;
import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.annotation.SuppressLint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import dev.ragnarok.fenrir.api.adapters.AnswerVKOfficialDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.ArticleDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.AttachmentsDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.AttachmentsEntryDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.AudioDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.AudioPlaylistDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.BooleanAdapter;
import dev.ragnarok.fenrir.api.adapters.ChatDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.ChatUserDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.ChatsInfoAdapter;
import dev.ragnarok.fenrir.api.adapters.CommentDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.CommunityDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.CustomCommentsResponseAdapter;
import dev.ragnarok.fenrir.api.adapters.DocsEntryDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.FaveLinkDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.FeedbackDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.FeedbackUserArrayDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.GroupSettingsAdapter;
import dev.ragnarok.fenrir.api.adapters.JsonStringDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.LikesListAdapter;
import dev.ragnarok.fenrir.api.adapters.LongpollUpdateAdapter;
import dev.ragnarok.fenrir.api.adapters.MarketDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.MessageDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.NewsAdapter;
import dev.ragnarok.fenrir.api.adapters.NewsfeedCommentDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.PhotoAlbumDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.PhotoDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.PostDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.PostSourceDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.PrivacyDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.ProfileInfoResponceDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.SchoolClazzDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.ServicePlaylistResponseDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.StoryDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.TopicDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.UserDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.VKApiCatalogLinkDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.VideoAlbumDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.VideoDtoAdapter;
import dev.ragnarok.fenrir.api.adapters.local_json.ChatJsonResponseDtoAdapter;
import dev.ragnarok.fenrir.api.model.ChatUserDto;
import dev.ragnarok.fenrir.api.model.FaveLinkDto;
import dev.ragnarok.fenrir.api.model.GroupSettingsDto;
import dev.ragnarok.fenrir.api.model.VKApiArticle;
import dev.ragnarok.fenrir.api.model.VKApiAudio;
import dev.ragnarok.fenrir.api.model.VKApiAudioPlaylist;
import dev.ragnarok.fenrir.api.model.VKApiCatalogLink;
import dev.ragnarok.fenrir.api.model.VKApiChat;
import dev.ragnarok.fenrir.api.model.VKApiComment;
import dev.ragnarok.fenrir.api.model.VKApiCommunity;
import dev.ragnarok.fenrir.api.model.VKApiMessage;
import dev.ragnarok.fenrir.api.model.VKApiNews;
import dev.ragnarok.fenrir.api.model.VKApiPhoto;
import dev.ragnarok.fenrir.api.model.VKApiPhotoAlbum;
import dev.ragnarok.fenrir.api.model.VKApiPost;
import dev.ragnarok.fenrir.api.model.VKApiStory;
import dev.ragnarok.fenrir.api.model.VKApiTopic;
import dev.ragnarok.fenrir.api.model.VKApiUser;
import dev.ragnarok.fenrir.api.model.VKApiVideo;
import dev.ragnarok.fenrir.api.model.VKApiVideoAlbum;
import dev.ragnarok.fenrir.api.model.VkApiAttachments;
import dev.ragnarok.fenrir.api.model.VkApiDoc;
import dev.ragnarok.fenrir.api.model.VkApiJsonString;
import dev.ragnarok.fenrir.api.model.VkApiMarket;
import dev.ragnarok.fenrir.api.model.VkApiPostSource;
import dev.ragnarok.fenrir.api.model.VkApiPrivacy;
import dev.ragnarok.fenrir.api.model.VkApiProfileInfoResponce;
import dev.ragnarok.fenrir.api.model.database.SchoolClazzDto;
import dev.ragnarok.fenrir.api.model.feedback.UserArray;
import dev.ragnarok.fenrir.api.model.feedback.VkApiBaseFeedback;
import dev.ragnarok.fenrir.api.model.local_json.ChatJsonResponse;
import dev.ragnarok.fenrir.api.model.longpoll.AbsLongpollEvent;
import dev.ragnarok.fenrir.api.model.response.ChatsInfoResponse;
import dev.ragnarok.fenrir.api.model.response.CustomCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.LikesListResponse;
import dev.ragnarok.fenrir.api.model.response.NewsfeedCommentsResponse;
import dev.ragnarok.fenrir.api.model.response.ServicePlaylistResponse;
import dev.ragnarok.fenrir.model.AnswerVKOfficialList;
import dev.ragnarok.fenrir.settings.IProxySettings;
import dev.ragnarok.fenrir.settings.Settings;
import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class VkRetrofitProvider implements IVkRetrofitProvider {

    private static final Gson VKGSON = new GsonBuilder()
            .registerTypeAdapter(AnswerVKOfficialList.class, new AnswerVKOfficialDtoAdapter())
            .registerTypeAdapter(VkApiAttachments.Entry.class, new AttachmentsEntryDtoAdapter())
            .registerTypeAdapter(VkApiDoc.Entry.class, new DocsEntryDtoAdapter())
            .registerTypeAdapter(VKApiPhoto.class, new PhotoDtoAdapter())
            .registerTypeAdapter(boolean.class, new BooleanAdapter())
            .registerTypeAdapter(VkApiPrivacy.class, new PrivacyDtoAdapter())
            .registerTypeAdapter(VKApiPhotoAlbum.class, new PhotoAlbumDtoAdapter())
            .registerTypeAdapter(VKApiVideoAlbum.class, new VideoAlbumDtoAdapter())
            .registerTypeAdapter(VkApiAttachments.class, new AttachmentsDtoAdapter())
            .registerTypeAdapter(VKApiAudio.class, new AudioDtoAdapter())
            .registerTypeAdapter(VKApiPost.class, new PostDtoAdapter())
            .registerTypeAdapter(VkApiPostSource.class, new PostSourceDtoAdapter())
            .registerTypeAdapter(VKApiUser.class, new UserDtoAdapter())
            .registerTypeAdapter(VKApiCommunity.class, new CommunityDtoAdapter())
            .registerTypeAdapter(VkApiBaseFeedback.class, new FeedbackDtoAdapter())
            .registerTypeAdapter(VKApiComment.class, new CommentDtoAdapter())
            .registerTypeAdapter(VKApiVideo.class, new VideoDtoAdapter())
            .registerTypeAdapter(UserArray.class, new FeedbackUserArrayDtoAdapter())
            .registerTypeAdapter(VKApiMessage.class, new MessageDtoAdapter())
            .registerTypeAdapter(VKApiNews.class, new NewsAdapter())
            .registerTypeAdapter(AbsLongpollEvent.class, new LongpollUpdateAdapter())
            .registerTypeAdapter(ChatsInfoResponse.class, new ChatsInfoAdapter())
            .registerTypeAdapter(VKApiChat.class, new ChatDtoAdapter())
            .registerTypeAdapter(ChatUserDto.class, new ChatUserDtoAdapter())
            .registerTypeAdapter(SchoolClazzDto.class, new SchoolClazzDtoAdapter())
            .registerTypeAdapter(LikesListResponse.class, new LikesListAdapter())
            .registerTypeAdapter(NewsfeedCommentsResponse.Dto.class, new NewsfeedCommentDtoAdapter())
            .registerTypeAdapter(VKApiTopic.class, new TopicDtoAdapter())
            .registerTypeAdapter(GroupSettingsDto.class, new GroupSettingsAdapter())
            .registerTypeAdapter(CustomCommentsResponse.class, new CustomCommentsResponseAdapter())
            .registerTypeAdapter(VKApiAudioPlaylist.class, new AudioPlaylistDtoAdapter())
            .registerTypeAdapter(VKApiStory.class, new StoryDtoAdapter())
            .registerTypeAdapter(FaveLinkDto.class, new FaveLinkDtoAdapter())
            .registerTypeAdapter(VKApiArticle.class, new ArticleDtoAdapter())
            .registerTypeAdapter(VKApiCatalogLink.class, new VKApiCatalogLinkDtoAdapter())
            .registerTypeAdapter(ChatJsonResponse.class, new ChatJsonResponseDtoAdapter())
            .registerTypeAdapter(VkApiJsonString.class, new JsonStringDtoAdapter())
            .registerTypeAdapter(VkApiProfileInfoResponce.class, new ProfileInfoResponceDtoAdapter())
            .registerTypeAdapter(VkApiMarket.class, new MarketDtoAdapter())
            .registerTypeAdapter(ServicePlaylistResponse.class, new ServicePlaylistResponseDtoAdapter())
            .create();

    private static final GsonConverterFactory GSON_CONVERTER_FACTORY = GsonConverterFactory.create(VKGSON);
    private static final RxJava3CallAdapterFactory RX_ADAPTER_FACTORY = RxJava3CallAdapterFactory.create();

    private final IProxySettings proxyManager;
    private final IVkMethodHttpClientFactory clientFactory;
    private final Object retrofitCacheLock = new Object();
    private final Object serviceRetrofitLock = new Object();
    @SuppressLint("UseSparseArrays")
    private final Map<Integer, RetrofitWrapper> retrofitCache = Collections.synchronizedMap(new HashMap<>(1));
    private volatile RetrofitWrapper serviceRetrofit;

    public VkRetrofitProvider(IProxySettings proxySettings, IVkMethodHttpClientFactory clientFactory) {
        proxyManager = proxySettings;
        this.clientFactory = clientFactory;
        proxyManager.observeActive()
                .subscribe(optional -> onProxySettingsChanged());
    }

    public static Gson getVkgson() {
        return VKGSON;
    }

    private void onProxySettingsChanged() {
        synchronized (retrofitCacheLock) {
            for (Map.Entry<Integer, RetrofitWrapper> entry : retrofitCache.entrySet()) {
                entry.getValue().cleanup();
            }

            retrofitCache.clear();
        }
    }

    @Override
    public Single<RetrofitWrapper> provideNormalRetrofit(int accountId) {
        return Single.fromCallable(() -> {
            RetrofitWrapper retrofit;

            synchronized (retrofitCacheLock) {
                retrofit = retrofitCache.get(accountId);

                if (nonNull(retrofit)) {
                    return retrofit;
                }

                OkHttpClient client = clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy());
                retrofit = createDefaultVkApiRetrofit(client);
                retrofitCache.put(accountId, retrofit);
            }

            return retrofit;
        });
    }

    @Override
    public Single<RetrofitWrapper> provideCustomRetrofit(int accountId, String token) {
        return Single.fromCallable(() -> {
            OkHttpClient client = clientFactory.createCustomVkHttpClient(accountId, token, VKGSON, proxyManager.getActiveProxy());
            return createDefaultVkApiRetrofit(client);
        });
    }

    @Override
    public Single<RetrofitWrapper> provideServiceRetrofit() {
        return Single.fromCallable(() -> {
            if (isNull(serviceRetrofit)) {
                synchronized (serviceRetrofitLock) {
                    if (isNull(serviceRetrofit)) {
                        OkHttpClient client = clientFactory.createServiceVkHttpClient(VKGSON, proxyManager.getActiveProxy());
                        serviceRetrofit = createDefaultVkApiRetrofit(client);
                    }
                }
            }

            return serviceRetrofit;
        });
    }

    @Override
    public Single<OkHttpClient> provideNormalHttpClient(int accountId) {
        return Single.fromCallable(() -> clientFactory.createDefaultVkHttpClient(accountId, VKGSON, proxyManager.getActiveProxy()));
    }

    private RetrofitWrapper createDefaultVkApiRetrofit(OkHttpClient okHttpClient) {
        return RetrofitWrapper.wrap(new Retrofit.Builder()
                .baseUrl("https://" + Settings.get().other().get_Api_Domain() + "/method/")
                .addConverterFactory(GSON_CONVERTER_FACTORY)
                .addCallAdapterFactory(RX_ADAPTER_FACTORY)
                .client(okHttpClient)
                .build());
    }
}