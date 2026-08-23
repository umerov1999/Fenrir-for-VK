package dev.ragnarok.fenrir.dialog.directauth

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.annotation.StringRes
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.captcha.VKCaptcha
import dev.ragnarok.fenrir.activity.captcha.VKCaptchaResult
import dev.ragnarok.fenrir.activity.captcha.VKCaptchaResultListener
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemGetVerificationMethods
import dev.ragnarok.fenrir.api.model.ecosystem.EcosystemProfile
import dev.ragnarok.fenrir.fragment.base.BaseMvpDialogFragment
import dev.ragnarok.fenrir.listener.TextWatcherAdapter
import dev.ragnarok.fenrir.nonNullNoEmpty
import dev.ragnarok.fenrir.picasso.PicassoInstance
import dev.ragnarok.fenrir.settings.CurrentTheme
import dev.ragnarok.fenrir.view.RoundCornerLinearView

class DirectAuthIdVKDialog : BaseMvpDialogFragment<DirectAuthIdVKPresenter, IDirectAuthIdVKView>(),
    IDirectAuthIdVKView {
    private var mContentRoot: View? = null
    private var mErrorInfoRoot: RoundCornerLinearView? = null
    private var mErrorInfoText: MaterialTextView? = null
    private var mAuthProfileRoot: View? = null
    private var mAuthProfileAvatar: ImageView? = null
    private var mAuthProfileFirstname: MaterialTextView? = null
    private var mAuthProfileLastname: MaterialTextView? = null
    private var mLoadingRoot: View? = null
    private var mUserNameRoot: TextInputLayout? = null
    private var mUserName: TextInputEditText? = null
    private var mValidationMethodRoot: TextInputLayout? = null
    private var mValidationMethod: MaterialAutoCompleteTextView? = null
    private var mPasswordRoot: TextInputLayout? = null
    private var mPassword: TextInputEditText? = null
    private var mCodeRoot: TextInputLayout? = null
    private var mCode: TextInputEditText? = null
    private var mSavePassword: MaterialSwitch? = null
    private var mNextFlow: MaterialButton? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val view = View.inflate(requireActivity(), R.layout.dialog_direct_auth_vk_id, null)
        mContentRoot = view.findViewById(R.id.content_root)
        mErrorInfoRoot = view.findViewById(R.id.error_info_container)
        mErrorInfoText = view.findViewById(R.id.error_info_text)
        mAuthProfileRoot = view.findViewById(R.id.auth_profile_root)
        mAuthProfileAvatar = view.findViewById(R.id.auth_avatar)
        mAuthProfileFirstname = view.findViewById(R.id.auth_firstname)
        mAuthProfileLastname = view.findViewById(R.id.auth_lastname)
        mLoadingRoot = view.findViewById(R.id.loading_root)
        mUserNameRoot = view.findViewById(R.id.field_username_root)
        mUserName = view.findViewById(R.id.field_username)
        mValidationMethodRoot = view.findViewById(R.id.field_validation_method_root)
        mValidationMethod = view.findViewById(R.id.field_validation_method)
        mPasswordRoot = view.findViewById(R.id.field_password_root)
        mPassword = view.findViewById(R.id.field_password)
        mCodeRoot = view.findViewById(R.id.field_code_root)
        mCode = view.findViewById(R.id.field_code)
        mSavePassword = view.findViewById(R.id.save_password)
        mSavePassword?.setOnCheckedChangeListener { _, isChecked ->
            presenter?.fireSavePasswordChanged(
                isChecked
            )
        }
        mNextFlow = view.findViewById(R.id.item_next_flow)

        mUserName?.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.fireUserNameEdit(s)
            }
        })

        mPassword?.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.firePasswordEdit(s)
            }
        })

        mCode?.addTextChangedListener(object : TextWatcherAdapter() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                presenter?.fireValidationCodeEdit(s)
            }
        })

        mNextFlow?.setOnClickListener {
            presenter?.fireNextFlowClick()
        }

        builder.setView(view)
        builder.setTitle(R.string.login_title)
        builder.setIcon(R.drawable.logo_vk)
        val dialog = builder.create()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(false)
        fireViewCreated()
        return dialog
    }

    override fun setUserNameRootVisible(visible: Boolean) {
        mUserNameRoot?.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            mUserName?.requestFocus()
        }
    }

    override fun setValidationMethods(
        visible: Boolean,
        methods: List<EcosystemGetVerificationMethods.Method>?
    ) {
        mValidationMethodRoot?.visibility = if (visible) View.VISIBLE else View.GONE
        if (methods.nonNullNoEmpty()) {
            val array = Array(methods.size) { methods[it].getDisplayedName(requireActivity()) }
            val spinnerItems = ArrayAdapter(
                requireActivity(),
                R.layout.spinner_item,
                array
            )
            mValidationMethod?.setText(spinnerItems.getItem(0))
            mValidationMethod?.setAdapter(spinnerItems)
            mValidationMethod?.setOnItemClickListener { _, _, position, _ ->
                presenter?.fireSelectValidationMethod(position)
            }
        }
    }

    override fun setPasswordRootVisible(visible: Boolean) {
        mPasswordRoot?.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            mPassword?.requestFocus()
        }
    }

    override fun setCodeRootVisible(visible: Boolean) {
        mCodeRoot?.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            mCode?.requestFocus()
        }
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?) =
        DirectAuthIdVKPresenter(saveInstanceState)

    override fun setNextFlowButtonParams(enabled: Boolean, @StringRes text: Int) {
        mNextFlow?.isEnabled = enabled
        mNextFlow?.setText(text)
    }

    override fun setSavePasswordVisible(visible: Boolean) {
        mSavePassword?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun displayLoading(loading: Boolean) {
        mLoadingRoot?.visibility = if (loading) View.VISIBLE else View.GONE
        mContentRoot?.visibility = if (loading) View.INVISIBLE else View.VISIBLE
    }

    override fun updateAuthProfile(authProfile: EcosystemProfile?) {
        if (authProfile == null) {
            mAuthProfileRoot?.visibility = View.GONE
            mAuthProfileAvatar?.let {
                PicassoInstance.with().cancelRequest(it)
            }
        } else {
            mAuthProfileRoot?.visibility = View.VISIBLE
            mAuthProfileFirstname?.text = authProfile.firstName
            mAuthProfileLastname?.text = authProfile.lastName

            val transformation = CurrentTheme.createTransformationForAvatar()
            if (authProfile.photo200.nonNullNoEmpty()) {
                mAuthProfileAvatar?.let {
                    PicassoInstance.with()
                        .load(authProfile.photo200)
                        .transform(transformation)
                        .into(it)
                }
            } else {
                mAuthProfileAvatar?.setImageResource(R.drawable.ic_avatar_unknown)
            }
        }
    }

    override fun onSetInfoOrErrorMessage(
        currentInfoMessage: String?,
        currentInfoIsError: Boolean
    ) {
        if (currentInfoMessage == null) {
            mErrorInfoRoot?.visibility = View.GONE
        } else {
            mErrorInfoRoot?.visibility = View.VISIBLE
            mErrorInfoRoot?.setFillColor(
                if (currentInfoIsError) CurrentTheme.getColorInActive(
                    requireActivity()
                ) else CurrentTheme.getColorPrimaryFixedDim(requireActivity())
            )
            mErrorInfoText?.text = currentInfoMessage
        }
    }

    override fun openVKIdCaptcha(redirect_uri: String?, domain: String?) {
        redirect_uri ?: return
        domain ?: return
        VKCaptcha.openCaptcha(
            domain,
            redirect_uri, object : VKCaptchaResultListener {
                override fun onResult(result: VKCaptchaResult) {
                    result.token.nonNullNoEmpty {
                        lazyPresenter {
                            fireVKIdCaptchaSuccess(it)
                        }
                    }
                }

                override fun onUserCancel() {
                }
            }
        )
    }

    override fun hideKeyboard() {
        try {
            val im =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            im?.hideSoftInputFromWindow(mUserName?.windowToken, 0)
            im?.hideSoftInputFromWindow(mPassword?.windowToken, 0)
            im?.hideSoftInputFromWindow(mCode?.windowToken, 0)
        } catch (_: Exception) {
        }
    }

    override fun returnSuccessToParent(
        userId: Long,
        accessToken: String?,
        login: String?,
        password: String?,
        twoFA: String?,
        isSave: Boolean
    ) {
        val data = Bundle()
        data.putString(Extra.TOKEN, accessToken)
        data.putLong(Extra.USER_ID, userId)
        data.putString(Extra.LOGIN, login)
        data.putString(Extra.PASSWORD, password)
        data.putString(Extra.TWO_FA, twoFA)
        data.putBoolean(Extra.SAVE, isSave)
        returnResultAndDismiss(ACTION_LOGIN_COMPLETE, data)
    }

    override fun returnSuccessValidation(
        url: String?,
        login: String?,
        password: String?,
        twoFA: String?,
        isSave: Boolean
    ) {
        val data = Bundle()
        data.putString(Extra.URL, url)
        data.putString(Extra.LOGIN, login)
        data.putString(Extra.PASSWORD, password)
        data.putString(Extra.TWO_FA, twoFA)
        data.putBoolean(Extra.SAVE, isSave)
        returnResultAndDismiss(ACTION_VALIDATE_VIA_WEB, data)
    }

    override fun cleanCode() {
        mCode?.text?.clear()
    }

    private fun returnResultAndDismiss(key: String, data: Bundle) {
        parentFragmentManager.setFragmentResult(key, data)
        dismiss()
    }

    companion object {
        const val ACTION_LOGIN_COMPLETE = "ACTION_LOGIN_COMPLETE"
        const val ACTION_VALIDATE_VIA_WEB = "ACTION_VALIDATE_VIA_WEB"


        fun newInstance(): DirectAuthIdVKDialog {
            val args = Bundle()
            val fragment = DirectAuthIdVKDialog()
            fragment.arguments = args
            return fragment
        }
    }
}