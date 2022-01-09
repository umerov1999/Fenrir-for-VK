package dev.ragnarok.fenrir.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.FeedbackLinkAdapter;
import dev.ragnarok.fenrir.model.Comment;
import dev.ragnarok.fenrir.model.Commented;
import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.Topic;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.feedback.Feedback;
import dev.ragnarok.fenrir.model.feedback.ParcelableFeedbackWrapper;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.util.Utils;

public class FeedbackLinkDialog extends DialogFragment implements FeedbackLinkAdapter.ActionListener {

    private Feedback mFeedback;

    public static FeedbackLinkDialog newInstance(int accountId, Feedback feedback) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        bundle.putParcelable("feedback", new ParcelableFeedbackWrapper(feedback));
        FeedbackLinkDialog feedbackLinkDialog = new FeedbackLinkDialog();
        feedbackLinkDialog.setArguments(bundle);
        return feedbackLinkDialog;
    }

    private static void fillClassFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            fillClassFields(fields, type.getSuperclass());
        }
    }

    private static boolean isSupport(Object o) {
        return o instanceof User ||
                o instanceof Post ||
                o instanceof Photo ||
                o instanceof Comment ||
                o instanceof Video ||
                o instanceof Topic;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ParcelableFeedbackWrapper wrapper = requireArguments().getParcelable("feedback");
        mFeedback = wrapper.get();
    }

    private List<Object> getAllModels(Feedback notification) {
        List<Object> models = new ArrayList<>();

        List<Field> fields = new ArrayList<>();
        fillClassFields(fields, notification.getClass());

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                Object o = field.get(notification);

                if (o instanceof List) {
                    List<?> list = (List<?>) o;
                    for (Object listItem : list) {
                        if (isSupport(listItem) && !models.contains(listItem)) {
                            models.add(listItem);
                        }
                    }
                }

                if (isSupport(o) && !models.contains(o)) {
                    models.add(o);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return models;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(requireActivity(), R.layout.fragment_feedback_links, null);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        FeedbackLinkAdapter adapter = new FeedbackLinkAdapter(requireActivity(), getAllModels(mFeedback), this);
        recyclerView.setAdapter(adapter);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.choose_action)
                .setNegativeButton(R.string.button_cancel, null)
                .setView(view);

        return builder.create();
    }

    private int getAccountId() {
        return requireArguments().getInt(Extra.ACCOUNT_ID);
    }

    private void close() {
        dismiss();
    }

    @Override
    public void onPostClick(@NonNull Post post) {
        close();
        PlaceFactory.getPostPreviewPlace(getAccountId(), post.getVkid(), post.getOwnerId(), post).tryOpenWith(requireActivity());
    }

    @Override
    public void onCommentClick(@NonNull Comment comment) {
        close();
        PlaceFactory.getCommentsPlace(getAccountId(), comment.getCommented(), comment.getId()).tryOpenWith(requireActivity());
    }

    @Override
    public void onTopicClick(@NonNull Topic topic) {
        close();
        PlaceFactory.getCommentsPlace(getAccountId(), Commented.from(topic), null).tryOpenWith(requireActivity());
    }

    @Override
    public void onPhotoClick(@NonNull Photo photo) {
        close();
        PlaceFactory.getSimpleGalleryPlace(getAccountId(), Utils.singletonArrayList(photo), 0, true).tryOpenWith(requireActivity());
    }

    @Override
    public void onVideoClick(@NonNull Video video) {
        close();
        PlaceFactory.getVideoPreviewPlace(getAccountId(), video).tryOpenWith(requireActivity());
    }

    @Override
    public void onUserClick(@NonNull User user) {
        close();
        PlaceFactory.getOwnerWallPlace(getAccountId(), user).tryOpenWith(requireActivity());
    }
}