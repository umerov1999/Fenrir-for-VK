package dev.ragnarok.fenrir.util;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.IOwnersRepository;
import dev.ragnarok.fenrir.domain.Repository;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.ISettings;
import dev.ragnarok.fenrir.settings.Settings;

public class Accounts {

    public static void showAccountSwitchedToast(@NonNull Activity context) {
        int aid = Settings.get()
                .accounts()
                .getCurrent();

        if (aid == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }
        User user;

        try {
            user = (User) Repository.INSTANCE.getOwners()
                    .getBaseOwnerInfo(aid, aid, IOwnersRepository.MODE_CACHE)
                    .blockingGet();
        } catch (Exception e) {
            // NotFountException
            return;
        }

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Toast toast = new Toast(context);
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.setText(user.getFullName());
            toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
            toast.show();
            return;
        }
         */

        View view = View.inflate(context, R.layout.account_change_toast, null);

        ImageView avatar = view.findViewById(R.id.avatar);
        TextView subtitle = view.findViewById(R.id.subtitle);

        PicassoInstance.with()
                .load(user.getMaxSquareAvatar())
                .transform(CurrentTheme.createTransformationForAvatar())
                .into(avatar);

        subtitle.setText(user.getFullName());

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, 0);
        toast.show();
    }

    public static int fromArgs(Bundle bundle) {
        return bundle == null ? ISettings.IAccountsSettings.INVALID_ID : bundle.getInt(Extra.ACCOUNT_ID);
    }
}
