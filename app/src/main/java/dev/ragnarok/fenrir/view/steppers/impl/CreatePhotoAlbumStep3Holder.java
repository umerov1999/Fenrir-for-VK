package dev.ragnarok.fenrir.view.steppers.impl;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.view.steppers.base.AbsStepHolder;
import dev.ragnarok.fenrir.view.steppers.base.BaseHolderListener;

public class CreatePhotoAlbumStep3Holder extends AbsStepHolder<CreatePhotoAlbumStepsHost> {

    private final ActionListener mActionListener;
    private View mRootView;
    private TextView mPrivacyViewAllowed;
    private TextView mPrivacyViewDisabled;

    public CreatePhotoAlbumStep3Holder(@NonNull ViewGroup parent, @NonNull ActionListener actionListener) {
        super(parent, R.layout.content_create_photo_album_step_3, CreatePhotoAlbumStepsHost.STEP_PRIVACY_VIEW);
        mActionListener = actionListener;
    }

    @Override
    public void initInternalView(View contentView) {
        mPrivacyViewAllowed = contentView.findViewById(R.id.view_allowed);
        mPrivacyViewDisabled = contentView.findViewById(R.id.view_disabled);

        mRootView = contentView.findViewById(R.id.root);
        mRootView.setOnClickListener(v -> mActionListener.onPrivacyViewClick());
    }

    @Override
    protected void bindViews(CreatePhotoAlbumStepsHost host) {
        mRootView.setEnabled(host.isPrivacySettingsEnable());
        // TODO: 16-May-16 Сделать неактивным, если альбом в группе

        Context context = mPrivacyViewAllowed.getContext();


        String text = host.getState().getPrivacyView().createAllowedString(context);

        mPrivacyViewAllowed.setText(text);
        mPrivacyViewDisabled.setText(host.getState().getPrivacyView().createDisallowedString());
    }

    public interface ActionListener extends BaseHolderListener {
        void onPrivacyViewClick();
    }
}
