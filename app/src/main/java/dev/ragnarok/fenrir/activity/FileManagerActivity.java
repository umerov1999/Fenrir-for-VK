package dev.ragnarok.fenrir.activity;

import android.os.Bundle;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.fragment.FileManagerFragment;

public class FileManagerActivity extends NoMainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            attachFragment();
        }
    }

    private void attachFragment() {
        Bundle args = new Bundle();
        args.putInt(Extra.ACTION, FileManagerFragment.SELECT_FILE);
        args.putBoolean(FileManagerFragment.EXTRA_SHOW_CANNOT_READ, true);

        FileManagerFragment fileManagerFragment = new FileManagerFragment();
        fileManagerFragment.setArguments(args);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, fileManagerFragment)
                .commit();
    }
}
