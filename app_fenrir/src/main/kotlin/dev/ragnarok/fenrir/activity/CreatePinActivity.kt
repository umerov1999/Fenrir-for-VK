package dev.ragnarok.fenrir.activity

import android.os.Bundle
import dev.ragnarok.fenrir.fragment.pin.createpin.CreatePinFragment
import dev.ragnarok.fenrir.util.Utils

class CreatePinActivity : NoMainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(noMainContainerViewId, CreatePinFragment.newInstance())
                .commit()
        }

        Utils.applyEdgeToEdgeActivity(this)
    }
}