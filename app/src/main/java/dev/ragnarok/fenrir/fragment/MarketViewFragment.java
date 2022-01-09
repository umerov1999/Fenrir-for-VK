package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.ActivityFeatures;
import dev.ragnarok.fenrir.activity.ActivityUtils;
import dev.ragnarok.fenrir.activity.SendAttachmentsActivity;
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.model.Market;
import dev.ragnarok.fenrir.model.Peer;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.MarketViewPresenter;
import dev.ragnarok.fenrir.mvp.view.IMarketViewView;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppTextUtils;
import dev.ragnarok.fenrir.util.AssertUtils;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.AspectRatioImageView;
import dev.ragnarok.fenrir.view.CircleCounterButton;

public class MarketViewFragment extends BaseMvpFragment<MarketViewPresenter, IMarketViewView>
        implements IMarketViewView {

    private AspectRatioImageView photo;
    private CircleCounterButton fave_button;
    private CircleCounterButton share_button;
    private MaterialButton marketer_button;
    private TextView price;
    private TextView title;
    private TextView available;
    private TextView description;
    private TextView time;
    private TextView sku;
    private TextView weight;
    private TextView dimensions;

    public static Bundle buildArgs(int aid, Market market) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Extra.MARKET, market);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static MarketViewFragment newInstance(Bundle bundle) {
        MarketViewFragment fragment = new MarketViewFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_market_view, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        photo = root.findViewById(R.id.item_market_image);
        fave_button = root.findViewById(R.id.fave_button);
        share_button = root.findViewById(R.id.share_button);
        marketer_button = root.findViewById(R.id.item_messaging_marketer);
        available = root.findViewById(R.id.item_available);
        price = root.findViewById(R.id.item_price);
        title = root.findViewById(R.id.item_title);
        description = root.findViewById(R.id.item_description);
        time = root.findViewById(R.id.item_time);
        sku = root.findViewById(R.id.item_sku);
        dimensions = root.findViewById(R.id.item_dimensions);
        weight = root.findViewById(R.id.item_weight);
        return root;
    }

    @Override
    public void displayMarket(Market market, int accountId) {
        if (nonNull(market.getThumb_photo())) {
            photo.setVisibility(View.VISIBLE);
            ViewUtils.displayAvatar(photo, null, market.getThumb_photo(), Constants.PICASSO_TAG);
            photo.setOnClickListener(v -> PlaceFactory.getSingleURLPhotoPlace(market.getThumb_photo(), market.getTitle(), "market").tryOpenWith(requireActivity()));
        } else {
            photo.setOnClickListener(v -> {
            });
            PicassoInstance.with().cancelRequest(photo);
            photo.setVisibility(View.GONE);
        }
        share_button.setOnClickListener(v -> callPresenter(p -> p.fireSendMarket(market)));
        fave_button.setIcon(market.isIs_favorite() ? R.drawable.favorite : R.drawable.star);
        marketer_button.setOnClickListener(v -> callPresenter(p -> p.fireWriteToMarketer(market, requireActivity())));
        fave_button.setOnClickListener(v -> callPresenter(MarketViewPresenter::fireFaveClick));

        switch (market.getAvailability()) {
            case 0:
                available.setTextColor(CurrentTheme.getColorOnSurface(requireActivity()));
                available.setText(R.string.markets_available);
                break;
            case 2:
                available.setTextColor(Color.parseColor("#ffaa00"));
                available.setText(R.string.markets_not_available);
                break;
            default:
                available.setTextColor(Color.parseColor("#ff0000"));
                available.setText(R.string.markets_deleted);
                break;
        }
        title.setText(market.getTitle());
        if (Utils.isEmpty(market.getPrice()))
            price.setVisibility(View.GONE);
        else {
            price.setVisibility(View.VISIBLE);
            price.setText(market.getPrice());
        }
        if (Utils.isEmpty(market.getDescription()))
            description.setVisibility(View.GONE);
        else {
            description.setVisibility(View.VISIBLE);
            description.setText(OwnerLinkSpanFactory.withSpans(requireActivity().getString(R.string.markets_description, market.getDescription()), true, false, new LinkActionAdapter() {
                @Override
                public void onOwnerClick(int ownerId) {
                    PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(requireActivity());
                }
            }));
        }
        if (market.getDate() == 0)
            time.setVisibility(View.GONE);
        else {
            time.setVisibility(View.VISIBLE);
            time.setText(AppTextUtils.getDateFromUnixTime(requireActivity(), market.getDate()));
        }

        if (Utils.isEmpty(market.getSku()))
            sku.setVisibility(View.GONE);
        else {
            sku.setVisibility(View.VISIBLE);
            sku.setText(requireActivity().getString(R.string.markets_sku, market.getSku()));
        }

        if (market.getWeight() == 0)
            weight.setVisibility(View.GONE);
        else {
            weight.setVisibility(View.VISIBLE);
            weight.setText(requireActivity().getString(R.string.markets_weight, market.getWeight()));
        }

        if (Utils.isEmpty(market.getDimensions()))
            dimensions.setVisibility(View.GONE);
        else {
            dimensions.setVisibility(View.VISIBLE);
            dimensions.setText(requireActivity().getString(R.string.markets_dimensions, market.getDimensions()));
        }
    }

    @Override
    public void sendMarket(int accountId, Market market) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, market);
    }

    @Override
    public void onWriteToMarketer(int accountId, Market market, Peer peer) {
        SendAttachmentsActivity.startForSendAttachmentsFor(requireActivity(), accountId, peer, market);
    }

    @Override
    public void displayLoading(boolean loading) {
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.product);
            actionBar.setSubtitle(null);
        }
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @NonNull
    @Override
    public IPresenterFactory<MarketViewPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            Market market = requireArguments().getParcelable(Extra.MARKET);
            AssertUtils.requireNonNull(market);
            return new MarketViewPresenter(aid, market, saveInstanceState);
        };
    }
}
