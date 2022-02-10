package dev.ragnarok.fenrir.settings;

import android.content.Context;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.maxr1998.modernpreferences.PreferenceScreen;

class PushSettings implements ISettings.IPushSettings {

    private static final String KEY_REGISTERED_FOR = "push_registered_for";
    private final Context app;
    private final Gson gson;

    PushSettings(Context context) {
        app = context.getApplicationContext();
        gson = new Gson();
    }

    @Override
    public void savePushRegistations(Collection<VkPushRegistration> data) {
        Set<String> target = new HashSet<>(data.size());

        for (VkPushRegistration registration : data) {
            target.add(gson.toJson(registration));
        }

        PreferenceScreen.getPreferences(app)
                .edit()
                .putStringSet(KEY_REGISTERED_FOR, target)
                .apply();
    }

    @Override
    public List<VkPushRegistration> getRegistrations() {
        Set<String> set = PreferenceScreen.getPreferences(app)
                .getStringSet(KEY_REGISTERED_FOR, null);

        List<VkPushRegistration> result = new ArrayList<>(set == null ? 0 : set.size());
        if (set != null) {
            for (String s : set) {
                VkPushRegistration registration = gson.fromJson(s, VkPushRegistration.class);
                result.add(registration);
            }
        }

        return result;
    }
}