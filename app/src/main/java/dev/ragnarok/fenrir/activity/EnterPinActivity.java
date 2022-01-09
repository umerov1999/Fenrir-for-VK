package dev.ragnarok.fenrir.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.EnterPinFragment;
import dev.ragnarok.fenrir.util.Utils;

public class EnterPinActivity extends NoMainActivity {

    public static Class<?> getClass(Context context) {
        return Utils.is600dp(context) ? EnterPinActivity.class : EnterPinActivityPortraitOnly.class;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, EnterPinFragment.newInstance())
                    .commit();
        }
    }
}
