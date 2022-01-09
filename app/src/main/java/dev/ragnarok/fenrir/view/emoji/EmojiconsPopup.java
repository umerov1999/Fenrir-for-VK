package dev.ragnarok.fenrir.view.emoji;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.listener.PicassoPauseOnScrollListener;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.model.StickerSet;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.emoji.section.Cars;
import dev.ragnarok.fenrir.view.emoji.section.Electronics;
import dev.ragnarok.fenrir.view.emoji.section.Emojicon;
import dev.ragnarok.fenrir.view.emoji.section.Food;
import dev.ragnarok.fenrir.view.emoji.section.Nature;
import dev.ragnarok.fenrir.view.emoji.section.People;
import dev.ragnarok.fenrir.view.emoji.section.Sport;
import dev.ragnarok.fenrir.view.emoji.section.Symbols;

public class EmojiconsPopup {

    private static final String KEY_PAGE = "emoji_page";
    private final Activity mContext;
    private int keyBoardHeight;
    private boolean isOpened;
    private OnEmojiconClickedListener onEmojiconClickedListener;
    private OnStickerClickedListener onStickerClickedListener;
    private OnMyStickerClickedListener onMyStickerClickedListener;
    private OnEmojiconBackspaceClickedListener onEmojiconBackspaceClickedListener;
    private OnSoftKeyboardOpenCloseListener onSoftKeyboardOpenCloseListener;
    private View rootView;
    private View emojiContainer;
    private final OnGlobalLayoutListener onGlobalLayoutListener = new OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            if (rootView == null) {
                return;
            }
            rootView.getWindowVisibleDisplayFrame(r);

            int screenHeight = rootView.getRootView().getHeight();
            int heightDifference = screenHeight - (r.bottom - r.top);

            int navBarHeight = mContext.getResources().getIdentifier("navigation_bar_height", "dimen", "android");

            if (navBarHeight > 0) {
                heightDifference -= mContext.getResources().getDimensionPixelSize(navBarHeight);
            }

            int statusbarHeight = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusbarHeight > 0) {
                heightDifference -= mContext.getResources().getDimensionPixelSize(statusbarHeight);
            }

            if (heightDifference > 200) {
                keyBoardHeight = heightDifference;

                if (Objects.nonNull(emojiContainer) && !Settings.get().ui().isEmojis_full_screen()) {
                    ViewGroup.LayoutParams layoutParams = emojiContainer.getLayoutParams();
                    layoutParams.height = keyBoardHeight;
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    emojiContainer.setLayoutParams(layoutParams);
                }

                if (!isOpened) {
                    if (onSoftKeyboardOpenCloseListener != null) {
                        onSoftKeyboardOpenCloseListener.onKeyboardOpen();
                    }
                }
                isOpened = true;
            } else {
                isOpened = false;
                if (onSoftKeyboardOpenCloseListener != null) {
                    onSoftKeyboardOpenCloseListener.onKeyboardClose();
                }
            }
        }
    };
    private ViewPager2 emojisPager;

    public EmojiconsPopup(View rootView, Activity context) {
        mContext = context;
        this.rootView = rootView;
        listenKeyboardSize();
    }

    public static void input(TextInputEditText editText, Emojicon emojicon) {
        if (editText == null || emojicon == null) {
            return;
        }

        int start = editText.getSelectionStart();
        int end = editText.getSelectionEnd();
        if (start < 0) {
            editText.append(emojicon.getEmoji());
        } else {
            editText.getText().replace(Math.min(start, end), Math.max(start, end), emojicon.getEmoji(), 0, emojicon.getEmoji().length());
        }
    }

    public static void backspace(TextInputEditText editText) {
        KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
        editText.dispatchKeyEvent(event);
    }

    public void storeState() {
        if (Objects.nonNull(emojisPager)) {
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putInt(KEY_PAGE, emojisPager.getCurrentItem())
                    .apply();
        }
    }

    public OnEmojiconClickedListener getOnEmojiconClickedListener() {
        return onEmojiconClickedListener;
    }

    public void setOnEmojiconClickedListener(OnEmojiconClickedListener listener) {
        onEmojiconClickedListener = listener;
    }

    public OnStickerClickedListener getOnStickerClickedListener() {
        return onStickerClickedListener;
    }

    public void setOnStickerClickedListener(OnStickerClickedListener onStickerClickedListener) {
        this.onStickerClickedListener = onStickerClickedListener;
    }

    public OnMyStickerClickedListener getOnMyStickerClickedListener() {
        return onMyStickerClickedListener;
    }

    public void setMyOnStickerClickedListener(OnMyStickerClickedListener onMyStickerClickedListener) {
        this.onMyStickerClickedListener = onMyStickerClickedListener;
    }

    public OnEmojiconBackspaceClickedListener getOnEmojiconBackspaceClickedListener() {
        return onEmojiconBackspaceClickedListener;
    }

    public void setOnEmojiconBackspaceClickedListener(OnEmojiconBackspaceClickedListener listener) {
        onEmojiconBackspaceClickedListener = listener;
    }

    public OnSoftKeyboardOpenCloseListener getOnSoftKeyboardOpenCloseListener() {
        return onSoftKeyboardOpenCloseListener;
    }

    public void setOnSoftKeyboardOpenCloseListener(OnSoftKeyboardOpenCloseListener listener) {
        onSoftKeyboardOpenCloseListener = listener;
    }

    public boolean isKeyBoardOpen() {
        return isOpened;
    }

    private void listenKeyboardSize() {
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public View getEmojiView(ViewGroup emojiParentView) {
        if (Objects.isNull(emojiContainer)) {
            emojiContainer = createCustomView(emojiParentView);

            int finalKeyboardHeight = Settings.get().ui().isEmojis_full_screen() ? ViewGroup.LayoutParams.MATCH_PARENT : (keyBoardHeight > 0 ? keyBoardHeight : (int) mContext.getResources().getDimension(R.dimen.keyboard_height));
            ViewGroup.LayoutParams layoutParams = emojiContainer.getLayoutParams();
            layoutParams.height = finalKeyboardHeight;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            emojiContainer.setLayoutParams(layoutParams);
        }

        return emojiContainer;
    }

    private View createCustomView(ViewGroup parent) {
        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        List<StickerSet> stickerSets = InteractorFactory.createStickersInteractor()
                .getStickers(accountId)
                .blockingGet();

        View view = LayoutInflater.from(mContext).inflate(R.layout.emojicons, parent, false);
        emojisPager = view.findViewById(R.id.emojis_pager);

        List<Emojicon[]> views = Arrays.asList(
                People.DATA,
                Nature.DATA,
                Food.DATA,
                Sport.DATA,
                Cars.DATA,
                Electronics.DATA,
                Symbols.DATA
        );

        List<AbsSection> sections = new ArrayList<>();
        sections.add(new EmojiSection(EmojiSection.TYPE_PEOPLE, getTintedDrawable(R.drawable.ic_emoji_people_vector)));
        sections.add(new EmojiSection(EmojiSection.TYPE_NATURE, getTintedDrawable(R.drawable.pine_tree)));
        sections.add(new EmojiSection(EmojiSection.TYPE_FOOD, getTintedDrawable(R.drawable.pizza)));
        sections.add(new EmojiSection(EmojiSection.TYPE_SPORT, getTintedDrawable(R.drawable.bike)));
        sections.add(new EmojiSection(EmojiSection.TYPE_CARS, getTintedDrawable(R.drawable.car)));
        sections.add(new EmojiSection(EmojiSection.TYPE_ELECTRONICS, getTintedDrawable(R.drawable.laptop_chromebook)));
        sections.add(new EmojiSection(EmojiSection.TYPE_SYMBOLS, getTintedDrawable(R.drawable.pound_box)));
        sections.add(new EmojiSection(EmojiSection.TYPE_MY_STICKERS, getTintedDrawable(R.drawable.dir_sticker)));

        List<StickerSet> stickersGridViews = new ArrayList<>();

        for (StickerSet stickerSet : stickerSets) {
            stickersGridViews.add(stickerSet);
            sections.add(new StickerSection(stickerSet));
        }


        EmojisPagerAdapter mEmojisAdapter = new EmojisPagerAdapter(views, stickersGridViews, this);
        emojisPager.setAdapter(mEmojisAdapter);

        int storedPage = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(KEY_PAGE, 0);
        if (mEmojisAdapter.getItemCount() > storedPage) {
            emojisPager.setCurrentItem(storedPage);
        }

        RecyclerView recyclerView = view.findViewById(R.id.recycleView);
        LinearLayoutManager manager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(manager);

        recyclerView.getItemAnimator().setChangeDuration(0);
        recyclerView.getItemAnimator().setAddDuration(0);
        recyclerView.getItemAnimator().setMoveDuration(0);
        recyclerView.getItemAnimator().setRemoveDuration(0);

        sections.get(emojisPager.getCurrentItem()).active = true;

        SectionsAdapter topSectionAdapter = new SectionsAdapter(sections, mContext);
        recyclerView.setAdapter(topSectionAdapter);

        view.findViewById(R.id.backspace).setOnTouchListener(new RepeatListener(700, 50, v -> {
            if (onEmojiconBackspaceClickedListener != null) {
                onEmojiconBackspaceClickedListener.onEmojiconBackspaceClicked(v);
            }
        }));

        topSectionAdapter.setListener(position -> emojisPager.setCurrentItem(position));

        emojisPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                int oldSelectionIndex = -1;
                for (int i = 0; i < sections.size(); i++) {
                    AbsSection section = sections.get(i);

                    if (section.active) {
                        oldSelectionIndex = i;
                    }

                    section.active = position == i;
                }

                topSectionAdapter.notifyItemChanged(position);
                if (oldSelectionIndex != -1) {
                    topSectionAdapter.notifyItemChanged(oldSelectionIndex);
                }

                manager.scrollToPosition(position);
            }
        });

        return view;
    }

    private Drawable getTintedDrawable(int resourceID) {
        Drawable drawable = ContextCompat.getDrawable(mContext, resourceID);
        drawable.setTint(CurrentTheme.getColorOnSurface(mContext));
        return drawable;
    }

    public void destroy() {
        storeState();

        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        rootView = null;
    }

    public interface OnStickerClickedListener {
        void onStickerClick(Sticker stickerId);
    }

    public interface OnMyStickerClickedListener {
        void onMyStickerClick(@NonNull Sticker.LocalSticker file);
    }

    public interface OnEmojiconClickedListener {
        void onEmojiconClicked(Emojicon emojicon);
    }

    public interface OnEmojiconBackspaceClickedListener {
        void onEmojiconBackspaceClicked(View v);
    }

    public interface OnSoftKeyboardOpenCloseListener {
        void onKeyboardOpen();

        void onKeyboardClose();
    }

    private static final class Holder extends RecyclerView.ViewHolder {
        Holder(View rootView) {
            super(rootView);
        }
    }

    private static class EmojisPagerAdapter extends RecyclerView.Adapter<Holder> {

        private final List<Emojicon[]> views;
        private final List<StickerSet> stickersGridViews;
        private final EmojiconsPopup mEmojiconPopup;

        EmojisPagerAdapter(List<Emojicon[]> views, List<StickerSet> stickersGridViews, EmojiconsPopup mEmojiconPopup) {
            this.views = views;
            this.stickersGridViews = stickersGridViews;
            this.mEmojiconPopup = mEmojiconPopup;
        }

        @Override
        public int getItemViewType(int position) {
            switch (position) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    return 0;
                case 7:
                    return 2;
                default:
                    return 1;
            }
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int res;
            switch (viewType) {
                case 0:
                    res = R.layout.emojicon_grid;
                    break;
                case 2:
                    res = R.layout.my_stickers_grid;
                    break;
                default:
                    res = R.layout.stickers_grid;
                    break;
            }
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(res, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            switch (position) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                    GridView gridView = holder.itemView.findViewById(R.id.Emoji_GridView);
                    Emojicon[] mData;
                    if (views.get(position) == null) {
                        mData = People.DATA;
                    } else {
                        mData = views.get(position).clone();
                    }

                    EmojiAdapter mAdapter = new EmojiAdapter(holder.itemView.getContext(), mData);
                    mAdapter.setEmojiClickListener(emojicon -> {
                        if (mEmojiconPopup.getOnEmojiconClickedListener() != null) {
                            mEmojiconPopup.getOnEmojiconClickedListener().onEmojiconClicked(emojicon);
                        }
                    });

                    gridView.setAdapter(mAdapter);
                    break;
                case 7:
                    RecyclerView recyclerViewMy = holder.itemView.findViewById(R.id.grid_stickers);
                    recyclerViewMy.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
                    MyStickersAdapter myStickersAdapter = new MyStickersAdapter(holder.itemView.getContext());
                    myStickersAdapter.setMyStickerClickedListener(file -> {
                        if (mEmojiconPopup.getOnMyStickerClickedListener() != null) {
                            mEmojiconPopup.getOnMyStickerClickedListener().onMyStickerClick(file);
                        }
                    });
                    GridLayoutManager gridMyLayoutManager = new GridLayoutManager(holder.itemView.getContext(), 4);
                    recyclerViewMy.setLayoutManager(gridMyLayoutManager);
                    ((TextView) holder.itemView.findViewById(R.id.header_sticker)).setText(recyclerViewMy.getContext().getString(R.string.my));
                    recyclerViewMy.setAdapter(myStickersAdapter);
                    break;
                default:
                    RecyclerView recyclerView = holder.itemView.findViewById(R.id.grid_stickers);
                    recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
                    StickersAdapter mAdaptert = new StickersAdapter(holder.itemView.getContext(), stickersGridViews.get(position - 8));
                    mAdaptert.setStickerClickedListener(stickerId -> {
                        if (mEmojiconPopup.getOnStickerClickedListener() != null) {
                            mEmojiconPopup.getOnStickerClickedListener().onStickerClick(stickerId);
                        }
                    });

                    GridLayoutManager gridLayoutManager = new GridLayoutManager(holder.itemView.getContext(), 4);
                    recyclerView.setLayoutManager(gridLayoutManager);
                    String title = stickersGridViews.get(position - 8).getTitle();
                    if (!Utils.isEmpty(title) && title.equals("recent"))
                        title = recyclerView.getContext().getString(R.string.usages);

                    ((TextView) holder.itemView.findViewById(R.id.header_sticker)).setText(title);

                    recyclerView.setAdapter(mAdaptert);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return views.size() + stickersGridViews.size() + 1;
        }
    }

    /**
     * A class, that can be used as a TouchListener on any view (e.g. a Button).
     * It cyclically runs a clickListener, emulating keyboard-like behaviour. First
     * click is fired immediately, next before initialInterval, and subsequent before
     * normalInterval.
     * <p/>
     * <p>Interval is scheduled before the onClick completes, so it has to run fast.
     * If it runs slow, it does not generate skipped onClicks.
     */
    public static class RepeatListener implements View.OnTouchListener {

        private final int normalInterval;
        private final OnClickListener clickListener;
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final int initialInterval;
        private View downView;
        private final Runnable handlerRunnable = new Runnable() {
            @Override
            public void run() {
                if (downView == null) {
                    return;
                }
                handler.removeCallbacksAndMessages(downView);
                handler.postAtTime(this, downView, SystemClock.uptimeMillis() + normalInterval);
                clickListener.onClick(downView);
            }
        };

        /**
         * @param initialInterval The interval before first click event
         * @param normalInterval  The interval before second and subsequent click
         *                        events
         * @param clickListener   The OnMessageActionListener, that will be called
         *                        periodically
         */
        public RepeatListener(int initialInterval, int normalInterval, OnClickListener clickListener) {
            if (clickListener == null)
                throw new IllegalArgumentException("null runnable");
            if (initialInterval < 0 || normalInterval < 0)
                throw new IllegalArgumentException("negative interval");

            this.initialInterval = initialInterval;
            this.normalInterval = normalInterval;
            this.clickListener = clickListener;
        }

        @SuppressLint("ClickableViewAccessibility")
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downView = view;
                    handler.removeCallbacks(handlerRunnable);
                    handler.postAtTime(handlerRunnable, downView, SystemClock.uptimeMillis() + initialInterval);
                    clickListener.onClick(view);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_OUTSIDE:
                    handler.removeCallbacksAndMessages(downView);
                    downView = null;
                    return true;
            }
            return false;
        }
    }
}
