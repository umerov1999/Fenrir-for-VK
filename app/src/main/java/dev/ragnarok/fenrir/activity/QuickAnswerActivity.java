package dev.ragnarok.fenrir.activity;

import static dev.ragnarok.fenrir.util.Objects.nonNull;
import static dev.ragnarok.fenrir.util.RxUtils.ignore;
import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;
import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.AttachmentsHolder;
import dev.ragnarok.fenrir.adapter.AttachmentsViewBinder;
import dev.ragnarok.fenrir.crypt.KeyLocationPolicy;
import dev.ragnarok.fenrir.domain.IMessagesRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.link.LinkHelper;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.longpoll.NotificationHelper;
import dev.ragnarok.fenrir.model.Article;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.model.AudioArtist;
import dev.ragnarok.fenrir.model.AudioPlaylist;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.model.Link;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.MarketAlbum;
import dev.ragnarok.fenrir.model.Message;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.PhotoAlbum;
import dev.ragnarok.fenrir.model.Poll;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.SaveMessageBuilder;
import dev.ragnarok.fenrir.model.Story;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.VoiceMessage;
import dev.ragnarok.fenrir.model.WallReply;
import dev.ragnarok.fenrir.model.WikiPage;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.player.MusicPlaybackService;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.settings.theme.ThemeOverlay;
import dev.ragnarok.fenrir.task.TextingNotifier;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.emoji.BotKeyboardView;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class QuickAnswerActivity extends AppCompatActivity {

    public static final String PARAM_BODY = "body";

    public static final String EXTRA_FOCUS_TO_FIELD = "focus_to_field";
    public static final String EXTRA_LIVE_DELAY = "live_delay";
    private final CompositeDisposable mLiveSubscription = new CompositeDisposable();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final AppPerms.doRequestPermissions requestWritePermission = AppPerms.requestPermissionsActivity(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
            () -> CustomToast.CreateCustomToast(this).showToast(R.string.permission_all_granted_text));
    private TextInputEditText etText;
    private TextingNotifier notifier;
    private int accountId;
    private Message msg;
    private boolean messageIsRead;
    private IMessagesRepository messagesRepository;

    public static Intent forStart(Context context, int accountId, Message msg, String body, String imgUrl, String title) {
        Intent intent = new Intent(context, QuickAnswerActivity.class);
        intent.putExtra(PARAM_BODY, body);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.MESSAGE, msg);
        intent.putExtra(Extra.TITLE, title);
        intent.putExtra(Extra.IMAGE, imgUrl);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(Utils.updateActivityContext(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        @StyleRes int theme;
        switch (Settings.get().main().getThemeOverlay()) {
            case ThemeOverlay.AMOLED:
                theme = R.style.QuickReply_Amoled;
                break;
            case ThemeOverlay.MD1:
                theme = R.style.QuickReply_MD1;
                break;
            case ThemeOverlay.OFF:
            default:
                theme = R.style.QuickReply;
        }
        setTheme(theme);
        super.onCreate(savedInstanceState);

        messagesRepository = Repository.INSTANCE.getMessages();

        boolean focusToField = getIntent().getBooleanExtra(EXTRA_FOCUS_TO_FIELD, true);

        if (!focusToField) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }

        msg = java.util.Objects.requireNonNull(getIntent().getExtras()).getParcelable(Extra.MESSAGE);
        accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
        notifier = new TextingNotifier(accountId);

        setContentView(R.layout.activity_quick_answer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.arrow_left);
        }

        setSupportActionBar(toolbar);

        TextView tvMessage = findViewById(R.id.item_message_text);
        TextView tvTime = findViewById(R.id.item_message_time);
        etText = findViewById(R.id.activity_quick_answer_edit_text);

        ImageView ivAvatar = findViewById(R.id.avatar);

        ImageButton btnToDialog = findViewById(R.id.activity_quick_answer_to_dialog);
        ImageButton btnSend = findViewById(R.id.activity_quick_answer_send);

        String messageTime = AppTextUtils.getDateFromUnixTime(this, msg.getDate());
        String title = getIntent().getStringExtra(Extra.TITLE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

        tvMessage.setText(getIntent().getStringExtra(PARAM_BODY), TextView.BufferType.SPANNABLE);
        tvTime.setText(messageTime);

        Transformation transformation = CurrentTheme.createTransformationForAvatar();
        String imgUrl = getIntent().getStringExtra(Extra.IMAGE);
        if (ivAvatar != null) {
            ViewUtils.displayAvatar(ivAvatar, transformation, imgUrl, null);
        }

        ViewGroup forwardMessagesRoot = findViewById(R.id.forward_messages);
        View attachmentsRoot = findViewById(R.id.item_message_attachment_container);
        AttachmentsHolder attachmentsHolder = new AttachmentsHolder();
        attachmentsHolder.setVgAudios(attachmentsRoot.findViewById(R.id.audio_attachments))
                .setVgVideos(attachmentsRoot.findViewById(R.id.video_attachments))
                .setVgDocs(attachmentsRoot.findViewById(R.id.docs_attachments))
                .setVgArticles(attachmentsRoot.findViewById(R.id.articles_attachments))
                .setVgPhotos(attachmentsRoot.findViewById(R.id.photo_attachments))
                .setVgPosts(attachmentsRoot.findViewById(R.id.posts_attachments))
                .setVoiceMessageRoot(attachmentsRoot.findViewById(R.id.voice_message_attachments));

        BotKeyboardView botKeyboardView = findViewById(R.id.input_keyboard_container);
        if (nonNull(botKeyboardView)) {
            if (nonNull(msg.getKeyboard()) && msg.getKeyboard().getInline() && msg.getKeyboard().getButtons().size() > 0) {
                botKeyboardView.setVisibility(View.VISIBLE);
                botKeyboardView.setButtons(msg.getKeyboard().getButtons(), false);
            } else {
                botKeyboardView.setVisibility(View.GONE);
            }

            botKeyboardView.setDelegate((button, needClose) -> {
                if (button.getType().equals("open_link")) {
                    LinkHelper.openLinkInBrowser(this, button.getLink());
                    return;
                }
                SaveMessageBuilder builder = new SaveMessageBuilder(accountId, msg.getPeerId())
                        .setPayload(button.getPayload()).setBody(button.getLabel());

                compositeDisposable.add(messagesRepository.put(builder)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::onMessageSaved, this::onSavingError));
            });
        }
        boolean hasAttachments = Utils.nonEmpty(msg.getFwd()) || (nonNull(msg.getAttachments()) && msg.getAttachments().size() > 0);
        attachmentsRoot.setVisibility(hasAttachments ? View.VISIBLE : View.GONE);

        if (hasAttachments) {
            AttachmentsViewBinder attachmentsViewBinder = new AttachmentsViewBinder(this, new AttachmentsViewBinder.OnAttachmentsActionCallback() {
                @Override
                public void onPollOpen(@NonNull Poll poll) {

                }

                @Override
                public void onVideoPlay(@NonNull Video video) {

                }

                @Override
                public void onAudioPlay(int position, @NonNull ArrayList<Audio> audios) {
                    MusicPlaybackService.startForPlayList(QuickAnswerActivity.this, audios, position, false);
                }

                @Override
                public void onForwardMessagesOpen(@NonNull ArrayList<Message> messages) {

                }

                @Override
                public void onOpenOwner(int userId) {

                }

                @Override
                public void onGoToMessagesLookup(@NonNull Message message) {

                }

                @Override
                public void onDocPreviewOpen(@NonNull Document document) {

                }

                @Override
                public void onPostOpen(@NonNull Post post) {

                }

                @Override
                public void onLinkOpen(@NonNull Link link) {

                }

                @Override
                public void onUrlOpen(@NonNull String url) {

                }

                @Override
                public void onFaveArticle(@NonNull Article article) {

                }

                @Override
                public void onShareArticle(@NonNull Article article) {

                }

                @Override
                public void onWikiPageOpen(@NonNull WikiPage page) {

                }

                @Override
                public void onPhotosOpen(@NonNull ArrayList<Photo> photos, int index, boolean refresh) {

                }

                @Override
                public void onUrlPhotoOpen(@NonNull String url, @NonNull String prefix, @NonNull String photo_prefix) {

                }

                @Override
                public void onStoryOpen(@NonNull Story story) {

                }

                @Override
                public void onWallReplyOpen(@NonNull WallReply reply) {

                }

                @Override
                public void onAudioPlaylistOpen(@NonNull AudioPlaylist playlist) {

                }

                @Override
                public void onPhotoAlbumOpen(@NonNull PhotoAlbum album) {

                }

                @Override
                public void onMarketAlbumOpen(@NonNull MarketAlbum market_album) {

                }

                @Override
                public void onMarketOpen(@NonNull Market market) {

                }

                @Override
                public void onArtistOpen(@NonNull AudioArtist artist) {

                }

                @Override
                public void onRequestWritePermissions() {
                    requestWritePermission.launch();
                }
            });
            attachmentsViewBinder.setVoiceActionListener(new AttachmentsViewBinder.VoiceActionListener() {
                @Override
                public void onVoiceHolderBinded(int voiceMessageId, int voiceHolderId) {

                }

                @Override
                public void onVoicePlayButtonClick(int voiceHolderId, int voiceMessageId, @NonNull VoiceMessage voiceMessage) {
                    Audio audio = new Audio().setId(voiceMessage.getId()).setOwnerId(voiceMessage.getOwnerId())
                            .setTitle(voiceMessage.getId() + "_" + voiceMessage.getOwnerId()).setArtist("Voice")
                            .setIsLocal().setDuration(voiceMessage.getDuration()).setUrl(firstNonEmptyString(voiceMessage.getLinkMp3(), voiceMessage.getLinkOgg()));
                    MusicPlaybackService.startForPlayList(QuickAnswerActivity.this, new ArrayList<>(Collections.singletonList(audio)), 0, false);
                }

                @Override
                public void onVoiceTogglePlaybackSpeed() {

                }

                @Override
                public void onTranscript(String voiceMessageId, int messageId) {

                }
            });
            attachmentsViewBinder.displayAttachments(msg.getAttachments(), attachmentsHolder, true, msg.getId());
            attachmentsViewBinder.displayForwards(msg.getFwd(), forwardMessagesRoot, this, true);
        }

        etText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!messageIsRead) {
                    setMessageAsRead();
                    messageIsRead = true;
                }

                cancelFinishWithDelay();

                if (nonNull(notifier)) {
                    notifier.notifyAboutTyping(msg.getPeerId());
                }
            }
        });

        btnSend.setOnClickListener(view -> send());
        btnToDialog.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setAction(MainActivity.ACTION_OPEN_PLACE);

            Place chatPlace = PlaceFactory.getChatPlace(accountId, accountId, new Peer(msg.getPeerId()).setAvaUrl(imgUrl).setTitle(title));
            intent.putExtra(Extra.PLACE, chatPlace);
            startActivity(intent);
            finish();
        });

        boolean liveDelay = getIntent().getBooleanExtra(EXTRA_LIVE_DELAY, false);
        if (liveDelay) {
            finishWithDelay();
        }
    }

    private void finishWithDelay() {
        mLiveSubscription.add(Observable.just(new Object())
                .delay(1, TimeUnit.MINUTES)
                .subscribe(o -> finish()));
    }

    private void cancelFinishWithDelay() {
        mLiveSubscription.dispose();
    }

    @Override
    protected void onDestroy() {
        mLiveSubscription.dispose();
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    /**
     * Отправка сообщения
     */
    private void send() {
        String trimmed_text = etText.getText().toString().trim();
        if (isEmpty(trimmed_text)) {
            Toast.makeText(this, getString(R.string.text_hint), Toast.LENGTH_SHORT).show();
            return;
        }

        boolean requireEncryption = Settings.get()
                .security()
                .isMessageEncryptionEnabled(accountId, msg.getPeerId());

        @KeyLocationPolicy
        int policy = KeyLocationPolicy.PERSIST;

        if (requireEncryption) {
            policy = Settings.get()
                    .security()
                    .getEncryptionLocationPolicy(accountId, msg.getPeerId());
        }

        SaveMessageBuilder builder = new SaveMessageBuilder(accountId, msg.getPeerId())
                .setBody(trimmed_text)
                .setForwardMessages(new ArrayList<>(Collections.singleton(msg)))
                .setRequireEncryption(requireEncryption)
                .setKeyLocationPolicy(policy);

        compositeDisposable.add(messagesRepository.put(builder)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onMessageSaved, this::onSavingError));
    }

    private void onSavingError(Throwable throwable) {
        Utils.showRedTopToast(this, throwable.toString());
    }

    @SuppressWarnings("unused")
    private void onMessageSaved(Message message) {
        NotificationHelper.tryCancelNotificationForPeer(this, accountId, msg.getPeerId());
        messagesRepository.runSendingQueue();
        finish();
    }

    private void setMessageAsRead() {
        compositeDisposable.add(messagesRepository.markAsRead(accountId, msg.getPeerId(), msg.getId())
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), ignore()));
    }
}
