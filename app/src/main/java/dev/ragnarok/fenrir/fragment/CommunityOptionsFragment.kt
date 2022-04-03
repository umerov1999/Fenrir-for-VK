package dev.ragnarok.fenrir.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import dev.ragnarok.fenrir.Extra
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.api.model.VKApiCommunity
import dev.ragnarok.fenrir.fragment.base.BaseMvpFragment
import dev.ragnarok.fenrir.model.Community
import dev.ragnarok.fenrir.model.Day
import dev.ragnarok.fenrir.model.GroupSettings
import dev.ragnarok.fenrir.model.IdOption
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory
import dev.ragnarok.fenrir.mvp.presenter.CommunityOptionsPresenter
import dev.ragnarok.fenrir.mvp.view.ICommunityOptionsView
import dev.ragnarok.fenrir.util.Month.getMonthTitle
import dev.ragnarok.fenrir.view.MySpinnerView

class CommunityOptionsFragment :
    BaseMvpFragment<CommunityOptionsPresenter, ICommunityOptionsView>(), ICommunityOptionsView {
    private var mName: TextInputEditText? = null
    private var mDescription: TextInputEditText? = null
    private var mCommunityTypeRoot: View? = null
    private var mAddress: TextInputEditText? = null
    private var mCategoryRoot: View? = null
    private var mCategory: MySpinnerView? = null
    private var mSubjectRoot: View? = null
    private var mSubjects: Array<MySpinnerView?> = arrayOfNulls(2)
    private var mWebsite: TextInputEditText? = null
    private var mPublicDateRoot: View? = null
    private var mDay: TextView? = null
    private var mMonth: TextView? = null
    private var mYear: TextView? = null
    private var mFeedbackCommentsRoot: View? = null
    private var mFeedbackComments: MaterialCheckBox? = null
    private var mObsceneFilter: MaterialCheckBox? = null
    private var mObsceneStopWords: MaterialCheckBox? = null
    private var mObsceneStopWordsEditText: TextInputEditText? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_community_options, container, false)
        mName = root.findViewById(R.id.name)
        mDescription = root.findViewById(R.id.description)
        mCommunityTypeRoot = root.findViewById(R.id.community_type_root)
        mAddress = root.findViewById(R.id.link)
        mCategoryRoot = root.findViewById(R.id.category_root)
        mCategory = root.findViewById(R.id.spinner_category)
        mCategory?.setIconOnClickListener {
            presenter?.onCategoryClick()
        }
        mSubjectRoot = root.findViewById(R.id.subject_root)
        mSubjects = arrayOfNulls(2)
        mSubjects[0] = root.findViewById(R.id.subject_0)
        mSubjects[1] = root.findViewById(R.id.subject_1)
        mWebsite = root.findViewById(R.id.website)
        mPublicDateRoot = root.findViewById(R.id.public_date_root)
        mDay = root.findViewById(R.id.day)
        mDay?.setOnClickListener {
            presenter?.fireDayClick()
        }
        mMonth = root.findViewById(R.id.month)
        mMonth?.setOnClickListener {
            presenter?.fireMonthClick()
        }
        mYear = root.findViewById(R.id.year)
        mYear?.setOnClickListener {
            presenter?.fireYearClick()
        }
        mFeedbackCommentsRoot = root.findViewById(R.id.feedback_comments_root)
        mFeedbackComments = root.findViewById(R.id.feedback_comments)
        mObsceneFilter = root.findViewById(R.id.obscene_filter)
        mObsceneStopWords = root.findViewById(R.id.obscene_stopwords)
        mObsceneStopWordsEditText = root.findViewById(R.id.obscene_stopwords_values)
        return root
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<CommunityOptionsPresenter> {
        return object : IPresenterFactory<CommunityOptionsPresenter> {
            override fun create(): CommunityOptionsPresenter {
                val accountId = requireArguments().getInt(Extra.ACCOUNT_ID)
                val community: VKApiCommunity = requireArguments().getParcelable(Extra.GROUP)!!
                val settings: GroupSettings = requireArguments().getParcelable(Extra.SETTINGS)!!
                return CommunityOptionsPresenter(accountId, community, settings, saveInstanceState)
            }
        }
    }

    override fun displayName(name: String?) {
        safelySetText(mName, name)
    }

    override fun displayDescription(description: String?) {
        safelySetText(mDescription, description)
    }

    override fun setCommunityTypeVisible(visible: Boolean) {
        safelySetVisibleOrGone(mCommunityTypeRoot, visible)
    }

    override fun displayAddress(address: String?) {
        safelySetText(mAddress, address)
    }

    override fun setCategoryVisible(visible: Boolean) {
        safelySetVisibleOrGone(mCategoryRoot, visible)
    }

    override fun displayCategory(categoryText: String?) {
        mCategory?.setValue(categoryText)
    }

    override fun showSelectOptionDialog(requestCode: Int, data: List<IdOption>) {
        val strings = arrayOfNulls<String>(data.size)
        for (i in data.indices) {
            strings[i] = data[i].title
        }
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.select_from_list_title)
            .setItems(strings) { _: DialogInterface?, which: Int ->
                presenter?.fireOptionSelected(
                    requestCode,
                    data[which]
                )
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    override fun setSubjectRootVisible(visible: Boolean) {
        safelySetVisibleOrGone(mSubjectRoot, visible)
    }

    override fun setSubjectVisible(index: Int, visible: Boolean) {
        safelySetVisibleOrGone(mSubjects[index], visible)
    }

    override fun displaySubjectValue(index: Int, value: String?) {
        mSubjects[index]?.setValue(value)
    }

    override fun displayWebsite(website: String?) {
        safelySetText(mWebsite, website)
    }

    override fun setPublicDateVisible(visible: Boolean) {
        safelySetVisibleOrGone(mPublicDateRoot, visible)
    }

    override fun dislayPublicDate(day: Day) {
        if (day.day > 0) {
            safelySetText(mDay, day.day.toString())
        } else {
            safelySetText(mDay, R.string.day)
        }
        if (day.year > 0) {
            safelySetText(mYear, day.year.toString())
        } else {
            safelySetText(mYear, R.string.year)
        }
        if (day.month > 0) {
            safelySetText(
                mMonth, getMonthTitle(
                    day.month
                )
            )
        } else {
            safelySetText(mMonth, R.string.month)
        }
    }

    override fun setFeedbackCommentsRootVisible(visible: Boolean) {
        safelySetVisibleOrGone(mFeedbackCommentsRoot, visible)
    }

    override fun setFeedbackCommentsChecked(checked: Boolean) {
        safelySetChecked(mFeedbackComments, checked)
    }

    override fun setObsceneFilterChecked(checked: Boolean) {
        safelySetChecked(mObsceneFilter, checked)
    }

    override fun setObsceneStopWordsChecked(checked: Boolean) {
        safelySetChecked(mObsceneStopWords, checked)
    }

    override fun setObsceneStopWordsVisible(visible: Boolean) {
        safelySetVisibleOrGone(mObsceneStopWordsEditText, visible)
    }

    override fun displayObsceneStopWords(words: String?) {
        safelySetText(mObsceneStopWordsEditText, words)
    }

    companion object {
        fun newInstance(
            accountId: Int,
            community: Community?,
            settings: GroupSettings?
        ): CommunityOptionsFragment {
            val args = Bundle()
            args.putParcelable(Extra.GROUP, community)
            args.putParcelable(Extra.SETTINGS, settings)
            args.putInt(Extra.ACCOUNT_ID, accountId)
            val fragment = CommunityOptionsFragment()
            fragment.arguments = args
            return fragment
        }
    }
}