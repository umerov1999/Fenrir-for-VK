package dev.ragnarok.fenrir.fragment.addproxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.listener.TextWatcherAdapter
import kotlin.math.max

class AddProxyFragment : BaseMvpFragment<AddProxyPresenter, IAddProxyView>(), IAddProxyView {
    private var mAuth: MaterialSwitch? = null
    private var mAuthFieldsRoot: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_proxy_add, container, false)
        (requireActivity() as AppCompatActivity).setSupportActionBar(root.findViewById(R.id.toolbar))

        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val insets =
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            val imeFixedBottom =
                if (windowInsets.isVisible(WindowInsetsCompat.Type.ime())) max(
                    windowInsets.getInsets(
                        WindowInsetsCompat.Type.ime()
                    ).bottom, insets.bottom
                ) else insets.bottom
            root.findViewById<View>(R.id.actionbar)
                ?.setPadding(insets.left, 0, insets.right, 0)
            root.findViewById<View>(R.id.toolbar)
                ?.setPadding(0, insets.top, 0, 0)
            root.findViewById<View>(R.id.scrollView)
                ?.setPadding(insets.left, 0, insets.right, imeFixedBottom)
            WindowInsetsCompat.CONSUMED
        }

        mAuthFieldsRoot = root.findViewById(R.id.auth_fields_root)
        val mAddress: TextInputEditText = root.findViewById(R.id.address)
        mAddress.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.fireAddressEdit(
                    s
                )
            }
        })
        val mPort: TextInputEditText = root.findViewById(R.id.port)
        mPort.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.firePortEdit(
                    s
                )
            }
        })
        mAuth = root.findViewById(R.id.authorization)
        mAuth?.setOnCheckedChangeListener { _, isChecked ->
            presenter?.fireAuthChecked(
                isChecked
            )
        }
        val mUsername: TextInputEditText = root.findViewById(R.id.username)
        mUsername.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.fireUsernameEdit(
                    s
                )
            }
        })
        val mPassword: TextInputEditText = root.findViewById(R.id.password)
        mPassword.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.firePassEdit(
                    s
                )
            }
        })
        root.findViewById<View>(R.id.button_save).setOnClickListener {
            presenter?.fireSaveClick()
        }
        return root
    }

    override fun setAuthFieldsEnabled(enabled: Boolean) {
        mAuthFieldsRoot?.visibility = if (enabled) View.VISIBLE else View.GONE
    }

    override fun setAuthChecked(checked: Boolean) {
        mAuth?.isChecked = checked
    }

    override fun goBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        AddProxyPresenter(saveInstanceState)

    companion object {
        fun newInstance(): AddProxyFragment {
            val args = Bundle()
            val fragment = AddProxyFragment()
            fragment.arguments = args
            return fragment
        }
    }
}