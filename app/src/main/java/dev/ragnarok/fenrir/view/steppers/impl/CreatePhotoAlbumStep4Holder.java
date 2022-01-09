package dev.ragnarok.fenrir.view.steppers.impl;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.view.steppers.base.AbsStepHolder;
import dev.ragnarok.fenrir.view.steppers.base.BaseHolderListener;

public class CreatePhotoAlbumStep4Holder extends AbsStepHolder<CreatePhotoAlbumStepsHost> {

    private final ActionListener mActionListener;
    private View mRootView;
    private TextView mPrivacyCommentAllowed;
    private TextView mPrivacyComemntDisallowed;

    public CreatePhotoAlbumStep4Holder(@NonNull ViewGroup parent, @NonNull ActionListener actionListener) {
        super(parent, R.layout.content_create_photo_album_step_4, CreatePhotoAlbumStepsHost.STEP_PRIVACY_VIEW);
        mActionListener = actionListener;
    }

    @Override
    public void initInternalView(View contentView) {
        mPrivacyCommentAllowed = contentView.findViewById(R.id.commenting_allowed);
        mPrivacyComemntDisallowed = contentView.findViewById(R.id.commenting_disabled);

        mRootView = contentView.findViewById(R.id.root);
        mRootView.setOnClickListener(v -> mActionListener.onPrivacyCommentClick());
    }

    @Override
    protected void bindViews(CreatePhotoAlbumStepsHost host) {
        mRootView.setEnabled(host.isPrivacySettingsEnable());
        // TODO: 16-May-16 Сделать неактивным, если альбом в группе

        Context context = mPrivacyCommentAllowed.getContext();
        String text = host.getState().getPrivacyComment().createAllowedString(context);
        mPrivacyCommentAllowed.setText(text);
        mPrivacyComemntDisallowed.setText(host.getState().getPrivacyComment().createDisallowedString());
    }

    public interface ActionListener extends BaseHolderListener {
        void onPrivacyCommentClick();
    }
}
