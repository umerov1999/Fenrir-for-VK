package dev.ragnarok.fenrir.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.CreatePinFragment;

public class CreatePinActivity extends NoMainActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, CreatePinFragment.newInstance())
                    .commit();
        }
    }
}