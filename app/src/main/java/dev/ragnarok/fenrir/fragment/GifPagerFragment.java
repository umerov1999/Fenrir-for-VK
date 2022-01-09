package dev.ragnarok.fenrir.fragment;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.media.gif.IGifPlayer;
import dev.ragnarok.fenrir.model.Document;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.GifPagerPresenter;
import dev.ragnarok.fenrir.mvp.view.IGifPagerView;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.AlternativeAspectRatioFrameLayout;
import dev.ragnarok.fenrir.view.CircleCounterButton;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;

public class GifPagerFragment extends AbsDocumentPreviewFragment<GifPagerPresenter, IGifPagerView>
        implements IGifPagerView {

    private final SparseArray<WeakReference<Holder>> mHolderSparseArray = new SparseArray<>();
    private ViewPager2 mViewPager;
    private Toolbar mToolbar;
    private View mButtonsRoot;
    private CircleCounterButton mButtonAddOrDelete;
    private boolean mFullscreen;

    public static GifPagerFragment newInstance(Bundle args) {
        GifPagerFragment fragment = new GifPagerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static Bundle buildArgs(int aid, @NonNull ArrayList<Document> documents, int index) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putInt(Extra.INDEX, index);
        args.putParcelableArrayList(Extra.DOCS, documents);
        return args;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mFullscreen = savedInstanceState.getBoolean("mFullscreen");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gif_pager, container, false);

        mToolbar = root.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);

        mButtonsRoot = root.findViewById(R.id.buttons);

        mButtonAddOrDelete = root.findViewById(R.id.button_add_or_delete);
        mButtonAddOrDelete.setOnClickListener(v -> callPresenter(GifPagerPresenter::fireAddDeleteButtonClick));

        mViewPager = root.findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setPageTransformer(Utils.createPageTransform(
                Settings.get().main().getViewpager_page_transform()
        ));

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                callPresenter(p -> p.firePageSelected(position));
            }
        });

        root.findViewById(R.id.button_share).setOnClickListener(v -> callPresenter(GifPagerPresenter::fireShareButtonClick));
        root.findViewById(R.id.button_download).setOnClickListener(v -> callPresenter(p -> p.fireDownloadButtonClick(requireActivity(), requireView())));


        resolveFullscreenViews();
        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mFullscreen", mFullscreen);
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(false, false)
                .build()
                .apply(requireActivity());
    }

    private void toggleFullscreen() {
        mFullscreen = !mFullscreen;
        resolveFullscreenViews();
    }

    private void resolveFullscreenViews() {
        if (Objects.nonNull(mToolbar)) {
            mToolbar.setVisibility(mFullscreen ? View.GONE : View.VISIBLE);
        }

        if (Objects.nonNull(mButtonsRoot)) {
            mButtonsRoot.setVisibility(mFullscreen ? View.GONE : View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public IPresenterFactory<GifPagerPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            int index = requireArguments().getInt(Extra.INDEX);

            ArrayList<Document> documents = requireArguments().getParcelableArrayList(Extra.DOCS);
            AssertUtils.requireNonNull(documents);

            return new GifPagerPresenter(aid, documents, index, saveInstanceState);
        };
    }

    @Override
    public void displayData(int pageCount, int selectedIndex) {
        if (Objects.nonNull(mViewPager)) {
            Adapter adapter = new Adapter(pageCount);
            mViewPager.setAdapter(adapter);
            mViewPager.setCurrentItem(selectedIndex, false);
        }
    }

    @Override
    public void setAspectRatioAt(int position, int w, int h) {
        Holder holder = findByPosition(position);
        if (Objects.nonNull(holder)) {
            holder.mAspectRatioLayout.setAspectRatio(w, h);
        }
    }

    @Override
    public void setPreparingProgressVisible(int position, boolean preparing) {
        for (int i = 0; i < mHolderSparseArray.size(); i++) {
            int key = mHolderSparseArray.keyAt(i);
            Holder holder = findByPosition(key);

            if (Objects.nonNull(holder)) {
                boolean isCurrent = position == key;
                boolean progressVisible = isCurrent && preparing;

                holder.setProgressVisible(progressVisible);
                holder.mSurfaceView.setVisibility(isCurrent && !preparing ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public void setupAddRemoveButton(boolean addEnable) {
        if (Objects.nonNull(mButtonAddOrDelete)) {
            mButtonAddOrDelete.setIcon(addEnable ? R.drawable.plus : R.drawable.ic_outline_delete);
        }
    }

    @Override
    public void attachDisplayToPlayer(int adapterPosition, IGifPlayer gifPlayer) {
        Holder holder = findByPosition(adapterPosition);
        if (Objects.nonNull(holder) && Objects.nonNull(gifPlayer) && holder.isSurfaceReady()) {
            gifPlayer.setDisplay(holder.mSurfaceHolder);
        }
    }

    @Override
    public void setToolbarTitle(@StringRes int titleRes, Object... params) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (Objects.nonNull(actionBar)) {
            actionBar.setTitle(getString(titleRes, params));
        }
    }

    @Override
    public void setToolbarSubtitle(@StringRes int titleRes, Object... params) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (Objects.nonNull(actionBar)) {
            actionBar.setSubtitle(getString(titleRes, params));
        }
    }

    @Override
    public void configHolder(int adapterPosition, boolean progress, int aspectRatioW, int aspectRatioH) {
        Holder holder = findByPosition(adapterPosition);
        if (Objects.nonNull(holder)) {
            holder.setProgressVisible(progress);
            holder.mAspectRatioLayout.setAspectRatio(aspectRatioW, aspectRatioH);
            holder.mSurfaceView.setVisibility(progress ? View.GONE : View.VISIBLE);
        }
    }

    private void fireHolderCreate(@NonNull Holder holder) {
        callPresenter(p -> p.fireHolderCreate(holder.getBindingAdapterPosition()));
    }

    public Holder findByPosition(int position) {
        WeakReference<Holder> weak = mHolderSparseArray.get(position);
        return Objects.isNull(weak) ? null : weak.get();
    }

    private final class Holder extends RecyclerView.ViewHolder implements SurfaceHolder.Callback {
        final SurfaceView mSurfaceView;
        final SurfaceHolder mSurfaceHolder;
        final RLottieImageView mProgressBar;
        final AlternativeAspectRatioFrameLayout mAspectRatioLayout;

        boolean mSurfaceReady;

        Holder(View rootView) {
            super(rootView);

            mSurfaceView = rootView.findViewById(R.id.surface_view);
            mSurfaceHolder = mSurfaceView.getHolder();
            mSurfaceHolder.addCallback(this);

            mAspectRatioLayout = rootView.findViewById(R.id.aspect_ratio_layout);
            mProgressBar = rootView.findViewById(R.id.preparing_progress_bar);
            mAspectRatioLayout.setOnClickListener(v -> toggleFullscreen());
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceReady = true;
            callPresenter(p -> p.fireSurfaceCreated(getBindingAdapterPosition()));
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceReady = false;
        }

        boolean isSurfaceReady() {
            return mSurfaceReady;
        }

        void setProgressVisible(boolean visible) {
            mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                mProgressBar.fromRes(R.raw.loading, Utils.dp(100F), Utils.dp(100F), new int[]{0x000000, CurrentTheme.getColorPrimary(requireActivity()), 0x777777, CurrentTheme.getColorSecondary(requireActivity())});
                mProgressBar.playAnimation();
            } else {
                mProgressBar.clearAnimationDrawable();
            }
        }
    }

    private class Adapter extends RecyclerView.Adapter<Holder> {

        final int mPageCount;

        Adapter(int count) {
            mPageCount = count;
            mHolderSparseArray.clear();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup container, int viewType) {
            return new Holder(LayoutInflater.from(container.getContext()).inflate(R.layout.content_gif_page, container, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return mPageCount;
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull Holder holder) {
            super.onViewDetachedFromWindow(holder);
            mHolderSparseArray.remove(holder.getBindingAdapterPosition());
        }

        @Override
        public void onViewAttachedToWindow(@NonNull Holder holder) {
            super.onViewAttachedToWindow(holder);
            mHolderSparseArray.put(holder.getBindingAdapterPosition(), new WeakReference<>(holder));
            fireHolderCreate(holder);
        }
    }
}
