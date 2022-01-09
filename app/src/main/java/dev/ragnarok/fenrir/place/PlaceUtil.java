package dev.ragnarok.fenrir.place;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.AbsModel;
import dev.ragnarok.fenrir.model.EditingPostType;
import dev.ragnarok.fenrir.model.Post;
import dev.ragnarok.fenrir.model.WallEditorAttrs;
import dev.ragnarok.fenrir.spots.SpotsDialog;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.Disposable;

public class PlaceUtil {

    public static void goToPostEditor(@NonNull Activity activity, int accountId, Post post) {
        AlertDialog dialog = createProgressDialog(activity);
        WeakReference<Dialog> dialogWeakReference = new WeakReference<>(dialog);
        WeakReference<Activity> reference = new WeakReference<>(activity);

        int ownerId = post.getOwnerId();

        Set<Integer> ids = new HashSet<>();
        ids.add(accountId);
        ids.add(ownerId);

        Disposable disposable = Repository.INSTANCE.getOwners()
                .findBaseOwnersDataAsBundle(accountId, ids, IOwnersRepository.MODE_NET)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owners -> {
                    WallEditorAttrs attrs = new WallEditorAttrs(owners.getById(ownerId), owners.getById(accountId));

                    Dialog d = dialogWeakReference.get();
                    if (d != null) {
                        d.dismiss();
                    }

                    Activity a = reference.get();

                    if (a != null) {
                        PlaceFactory.getEditPostPlace(accountId, post, attrs).tryOpenWith(a);
                    }
                }, throwable -> safelyShowError(reference, throwable));

        dialog.setOnCancelListener(d -> disposable.dispose());
    }

    private static void safelyShowError(WeakReference<Activity> reference, Throwable throwable) {
        Activity a = reference.get();
        if (a != null) {
            new MaterialAlertDialogBuilder(a)
                    .setTitle(R.string.error)
                    .setMessage(Utils.getCauseIfRuntime(throwable).getMessage())
                    .setPositiveButton(R.string.button_ok, null)
                    .show();
        }
    }

    public static void goToPostCreation(@NonNull Activity activity, int accountId, int ownerId,
                                        @EditingPostType int editingType, @Nullable List<AbsModel> input) {
        goToPostCreation(activity, accountId, ownerId, editingType, input, null, null, null);
    }

    public static void goToPostCreation(@NonNull Activity activity, int accountId, int ownerId,
                                        @EditingPostType int editingType, @Nullable List<AbsModel> input, @Nullable ArrayList<Uri> streams, @Nullable String body, @Nullable String mime) {

        AlertDialog dialog = createProgressDialog(activity);
        WeakReference<Dialog> dialogWeakReference = new WeakReference<>(dialog);
        WeakReference<Activity> reference = new WeakReference<>(activity);

        Set<Integer> ids = new HashSet<>();
        ids.add(accountId);
        ids.add(ownerId);

        Disposable disposable = Repository.INSTANCE.getOwners()
                .findBaseOwnersDataAsBundle(accountId, ids, IOwnersRepository.MODE_NET)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owners -> {
                    WallEditorAttrs attrs = new WallEditorAttrs(owners.getById(ownerId), owners.getById(accountId));

                    Dialog d = dialogWeakReference.get();
                    if (d != null) {
                        d.dismiss();
                    }

                    Activity a = reference.get();
                    if (a != null) {
                        PlaceFactory.getCreatePostPlace(accountId, ownerId, editingType, input, attrs, streams, body, mime).tryOpenWith(a);
                    }
                }, throwable -> safelyShowError(reference, throwable));

        dialog.setOnCancelListener(d -> disposable.dispose());
    }

    private static AlertDialog createProgressDialog(Activity activity) {
        AlertDialog dialog = new SpotsDialog.Builder().setContext(activity).setMessage(activity.getString(R.string.message_obtaining_owner_information)).setCancelable(true).build();
        dialog.show();
        return dialog;
    }
}