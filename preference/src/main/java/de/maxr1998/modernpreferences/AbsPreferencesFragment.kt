package de.maxr1998.modernpreferences

import android.os.Bundle
import android.view.View
import androidx.annotation.ArrayRes
import androidx.fragment.app.Fragment
import de.maxr1998.modernpreferences.preferences.*
import de.maxr1998.modernpreferences.preferences.choice.MultiChoiceDialogPreference
import de.maxr1998.modernpreferences.preferences.choice.SelectionItem
import de.maxr1998.modernpreferences.preferences.choice.SingleChoiceDialogPreference


abstract class AbsPreferencesFragment : Fragment() {
    protected var preferencesAdapter: PreferencesAdapter? = null
    protected abstract val keyInstanceState: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            ExtraPref.EDIT_DIALOG_REQUEST, this
        ) { _: String?, result: Bundle ->
            preferencesAdapter?.applyToPreferenceInScreen(
                result.getString(ExtraPref.PREFERENCE_SCREEN_KEY),
                result.getString(ExtraPref.PREFERENCE_KEY)!!
            ) {
                (it as EditTextPreference).persist(result.getCharSequence(ExtraPref.RESULT_VALUE))
            }
        }
        parentFragmentManager.setFragmentResultListener(
            ExtraPref.SEPARATOR_DIALOG_REQUEST, this
        ) { _: String?, result: Bundle ->
            preferencesAdapter?.applyToPreferenceInScreen(
                result.getString(ExtraPref.PREFERENCE_SCREEN_KEY),
                result.getString(ExtraPref.PREFERENCE_KEY)!!
            ) {
                (it as SeparatorSpaceTextPreference).persist(result.getCharSequence(ExtraPref.RESULT_VALUE))
            }
        }
        parentFragmentManager.setFragmentResultListener(
            ExtraPref.MULTI_LINE_EDIT_DIALOG_REQUEST, this
        ) { _: String?, result: Bundle ->
            preferencesAdapter?.applyToPreferenceInScreen(
                result.getString(ExtraPref.PREFERENCE_SCREEN_KEY),
                result.getString(ExtraPref.PREFERENCE_KEY)!!
            ) {
                (it as MultiLineEditTextPreference).persist(result.getCharSequence(ExtraPref.RESULT_VALUE))
            }
        }
        parentFragmentManager.setFragmentResultListener(
            ExtraPref.CUSTOM_TEXT_DIALOG_REQUEST, this
        ) { _: String?, result: Bundle ->
            preferencesAdapter?.applyToPreferenceInScreen(
                result.getString(ExtraPref.PREFERENCE_SCREEN_KEY),
                result.getString(ExtraPref.PREFERENCE_KEY)!!
            ) {
                (it as CustomTextPreference).persist(result.getCharSequence(ExtraPref.RESULT_VALUE))
            }
        }
        parentFragmentManager.setFragmentResultListener(
            ExtraPref.MULTI_CHOOSE_DIALOG_REQUEST, this
        ) { _: String?, result: Bundle ->
            preferencesAdapter?.applyToPreferenceInScreen(
                result.getString(ExtraPref.PREFERENCE_SCREEN_KEY),
                result.getString(ExtraPref.PREFERENCE_KEY)!!
            ) {
                (it as MultiChoiceDialogPreference).persistSelection(
                    result.getStringArrayList(
                        ExtraPref.RESULT_VALUE
                    )!!
                )
            }
        }
        parentFragmentManager.setFragmentResultListener(
            ExtraPref.SINGLE_CHOOSE_DIALOG_REQUEST, this
        ) { _: String?, result: Bundle ->
            preferencesAdapter?.applyToPreferenceInScreen(
                result.getString(ExtraPref.PREFERENCE_SCREEN_KEY),
                result.getString(ExtraPref.PREFERENCE_KEY)!!
            ) {
                (it as SingleChoiceDialogPreference).persistSelection(
                    result.getParcelable(
                        ExtraPref.RESULT_VALUE
                    )
                )
            }
        }
        parentFragmentManager.setFragmentResultListener(
            ExtraPref.COLOR_DIALOG_REQUEST, this
        ) { _: String?, result: Bundle ->
            preferencesAdapter?.applyToPreferenceInScreen(
                result.getString(ExtraPref.PREFERENCE_SCREEN_KEY),
                result.getString(ExtraPref.PREFERENCE_KEY)!!
            ) {
                (it as ColorPickPreference).persist(result.getInt(ExtraPref.RESULT_VALUE))
            }
        }
        parentFragmentManager.setFragmentResultListener(
            ExtraPref.RECREATE_ACTIVITY_REQUEST, this
        ) { _: String?, _: Bundle ->
            requireActivity().recreate()
        }
    }

    protected fun createPreferenceAdapter(): Boolean {
        val ret = preferencesAdapter == null
        if (ret) {
            preferencesAdapter = PreferencesAdapter()
        }
        return ret
    }

    protected fun goBack(): Boolean {
        preferencesAdapter?.let {
            return it.goBack()
        }
        return true
    }

    protected fun canGoBack(): Boolean {
        preferencesAdapter?.let {
            return it.canGoBack()
        }
        return false
    }

    protected fun loadInstanceState(screen: MakeScreen, savedInstanceState: Bundle?, view: View?) {
        savedInstanceState?.getParcelable<PreferencesAdapter.SavedState>(keyInstanceState)
            ?.let {
                preferencesAdapter?.loadSavedState(
                    requireActivity(),
                    it,
                    screen.create(), view
                )
            }
            ?: preferencesAdapter?.setRootScreen(screen.create())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(keyInstanceState, preferencesAdapter?.getSavedState())
    }

    fun interface MakeScreen {
        fun create(): PreferenceScreen
    }

    protected fun selItems(@ArrayRes names: Int, @ArrayRes values: Int): ArrayList<SelectionItem> {
        val nameData = resources.getTextArray(names)
        val valueData = resources.getTextArray(values)
        return selItems(nameData, valueData)
    }

    protected fun selItems(
        names: Array<CharSequence>,
        values: Array<CharSequence>
    ): ArrayList<SelectionItem> {
        if (names.size != values.size) {
            throw UnsupportedOperationException("SelectionItem names and values has wrong count")
        }
        val ret = ArrayList<SelectionItem>(names.size)
        for (i in names.indices) {
            ret.add(SelectionItem(values[i].toString(), names[i], null))
        }
        return ret
    }
}
