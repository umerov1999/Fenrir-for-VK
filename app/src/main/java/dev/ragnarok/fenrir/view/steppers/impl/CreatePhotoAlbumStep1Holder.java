package dev.ragnarok.fenrir.view.steppers.impl;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.listener.TextWatcherAdapter;
import dev.ragnarok.fenrir.view.steppers.base.AbsStepHolder;
import dev.ragnarok.fenrir.view.steppers.base.BaseHolderListener;

public class CreatePhotoAlbumStep1Holder extends AbsStepHolder<CreatePhotoAlbumStepsHost> {

    private final ActionListener mActionListener;
    private TextView mTitle;
    private TextView mDescription;

    public CreatePhotoAlbumStep1Holder(ViewGroup parent, @NonNull ActionListener actionListener) {
        super(parent, R.layout.step_create_photo_album_1, CreatePhotoAlbumStepsHost.STEP_TITLE_AND_DESCRIPTION);
        mActionListener = actionListener;
    }

    @Override
    public void initInternalView(View contentView) {
        mTitle = contentView.findViewById(R.id.title);
        mDescription = contentView.findViewById(R.id.description);

        mTitle.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mActionListener.onTitleEdited(s);
            }
        });

        mTitle.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mActionListener.onDescriptionEdited(s);
            }
        });
    }

    @Override
    protected void bindViews(CreatePhotoAlbumStepsHost host) {
        mTitle.setText(host.getState().getTitle());
        mDescription.setText(host.getState().getDescription());
    }

    public interface ActionListener extends BaseHolderListener {
        void onTitleEdited(CharSequence text);

        void onDescriptionEdited(CharSequence text);
    }
}
