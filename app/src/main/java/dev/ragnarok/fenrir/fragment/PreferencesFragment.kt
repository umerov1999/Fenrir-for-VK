package dev.ragnarok.fenrir.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso3.BitmapSafeResize.isOverflowCanvas
import com.squareup.picasso3.BitmapSafeResize.setHardwareRendering
import com.squareup.picasso3.BitmapSafeResize.setMaxResolution
import dev.ragnarok.fenrir.Constants.API_VERSION
import dev.ragnarok.fenrir.Constants.USER_AGENT_ACCOUNT
import dev.ragnarok.fenrir.Extensions.Companion.fromIOToMain
import dev.ragnarok.fenrir.Extra.ACCOUNT_ID
import dev.ragnarok.fenrir.Extra.PHOTOS
import dev.ragnarok.fenrir.Injection.provideApplicationContext
import dev.ragnarok.fenrir.Injection.provideProxySettings
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.activity.*
import dev.ragnarok.fenrir.activity.alias.*
import dev.ragnarok.fenrir.api.model.LocalServerSettings
import dev.ragnarok.fenrir.api.model.PlayerCoverBackgroundSettings
import dev.ragnarok.fenrir.api.model.SlidrSettings
import dev.ragnarok.fenrir.db.DBHelper
import dev.ragnarok.fenrir.db.Stores
import dev.ragnarok.fenrir.domain.InteractorFactory
import dev.ragnarok.fenrir.filepicker.model.DialogConfigs
import dev.ragnarok.fenrir.filepicker.model.DialogProperties
import dev.ragnarok.fenrir.filepicker.view.FilePickerDialog
import dev.ragnarok.fenrir.listener.OnSectionResumeCallback
import dev.ragnarok.fenrir.longpoll.AppNotificationChannels
import dev.ragnarok.fenrir.media.record.AudioRecordWrapper
import dev.ragnarok.fenrir.model.LocalPhoto
import dev.ragnarok.fenrir.model.SwitchableCategory
import dev.ragnarok.fenrir.module.FenrirNative
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.clear_cache
import dev.ragnarok.fenrir.picasso.PicassoInstance.Companion.with
import dev.ragnarok.fenrir.picasso.transforms.EllipseTransformation
import dev.ragnarok.fenrir.picasso.transforms.RoundTransformation
import dev.ragnarok.fenrir.place.Place
import dev.ragnarok.fenrir.place.PlaceFactory
import dev.ragnarok.fenrir.service.KeepLongpollService
import dev.ragnarok.fenrir.settings.AvatarStyle
import dev.ragnarok.fenrir.settings.CurrentTheme.getColorPrimary
import dev.ragnarok.fenrir.settings.CurrentTheme.getColorSecondary
import dev.ragnarok.fenrir.settings.ISettings
import dev.ragnarok.fenrir.settings.NightMode
import dev.ragnarok.fenrir.settings.Settings
import dev.ragnarok.fenrir.settings.theme.ThemesController
import dev.ragnarok.fenrir.util.AppPerms
import dev.ragnarok.fenrir.util.CustomToast.Companion.CreateCustomToast
import dev.ragnarok.fenrir.util.RxUtils
import dev.ragnarok.fenrir.util.Utils
import dev.ragnarok.fenrir.util.refresh.RefreshToken
import dev.ragnarok.fenrir.view.MySearchView
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class PreferencesFragment : PreferenceFragmentCompat() {
    private val disposables = CompositeDisposable()
    private val requestLightBackground = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            changeDrawerBackground(false, result.data)
            //requireActivity().recreate()
        }
    }
    private val requestDarkBackground = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            changeDrawerBackground(true, result.data)
            //requireActivity().recreate()
        }
    }
    private val requestPin = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            PlaceFactory.getSecuritySettingsPlace().tryOpenWith(requireActivity())
        }
    }
    private val requestContactsPermission = AppPerms.requestPermissions(
        this, arrayOf(Manifest.permission.READ_CONTACTS)
    ) { PlaceFactory.getFriendsByPhonesPlace(accountId).tryOpenWith(requireActivity()) }
    private val requestReadPermission = AppPerms.requestPermissions(
        this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    ) { CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        val searchView: MySearchView = root.findViewById(R.id.searchview)
        searchView.setOnQueryTextListener(object : MySearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                findPreferenceByName<Preference>(query)?.let { scrollToPreference(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                findPreferenceByName<Preference>(newText)?.let { scrollToPreference(it) }
                return false
            }
        })
        searchView.setRightButtonVisibility(false)
        searchView.setLeftIcon(R.drawable.magnify)
        searchView.setQuery("", true)
        return root
    }

    override fun getLayoutId(): Int {
        return R.layout.preference_fenrir_list_fragment
    }

    private fun selectLocalImage(isDark: Boolean) {
        if (!AppPerms.hasReadStoragePermission(requireActivity())) {
            requestReadPermission.launch()
            return
        }
        val intent = Intent(activity, PhotosActivity::class.java)
        intent.putExtra(PhotosActivity.EXTRA_MAX_SELECTION_COUNT, 1)
        if (isDark) {
            requestDarkBackground.launch(intent)
        } else {
            requestLightBackground.launch(intent)
        }
    }

    private fun enableChatPhotoBackground(index: Int) {
        val bEnable: Boolean = when (index) {
            0, 1, 2, 3 -> false
            else -> true
        }
        val prefLightChat = findPreference<Preference>("chat_light_background")
        val prefDarkChat = findPreference<Preference>("chat_dark_background")
        val prefResetPhotoChat = findPreference<Preference>("reset_chat_background")
        if (prefDarkChat == null || prefLightChat == null || prefResetPhotoChat == null) return
        prefDarkChat.isEnabled = bEnable
        prefLightChat.isEnabled = bEnable
        prefResetPhotoChat.isEnabled = bEnable
    }

    private fun lunchScopedControl() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        } else {
            TODO("VERSION.SDK_INT < R")
        }
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        requireActivity().startActivity(intent)
    }

    private fun doFixDirTime(dir: String, isRoot: Boolean) {
        val root = File(dir)
        val list: ArrayList<Long> = ArrayList()
        if (root.exists() && root.isDirectory) {
            val children = root.list()
            if (children != null) {
                for (child in children) {
                    val rem = File(root, child)
                    if (rem.isFile && !rem.isHidden && !isRoot) {
                        list.add(rem.lastModified())
                    } else if (rem.isDirectory && !rem.isHidden && rem.name != "." && rem.name != "..") {
                        doFixDirTime(rem.absolutePath, false)
                    }
                }
            }
        } else {
            return
        }
        if (isRoot) {
            return
        }

        val res = list.maxOrNull()
        res?.let {
            root.setLastModified(it)
        }
    }

    @Suppress("DEPRECATION")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings)
        findPreference<Preference>(KEY_NIGHT_SWITCH)?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                when (newValue.toString().toInt()) {
                    NightMode.DISABLE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    NightMode.ENABLE -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    NightMode.AUTO -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                    NightMode.FOLLOW_SYSTEM -> AppCompatDelegate.setDefaultNightMode(
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
                }
                true
            }
        findPreference<Preference>("messages_menu_down")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("is_side_no_stroke")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("is_side_transition")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("donate_anim_set")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("theme_overlay")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("show_mini_player")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("vk_auth_domain")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                provideProxySettings()
                    .setActive(provideProxySettings().activeProxy)
                true
            }
        findPreference<Preference>("vk_api_domain")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                provideProxySettings()
                    .setActive(provideProxySettings().activeProxy)
                true
            }

        findPreference<Preference>("delete_dynamic_shortcuts")?.let {
            it.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                        val manager: ShortcutManager = requireActivity().getSystemService(
                            ShortcutManager::class.java
                        )
                        manager.removeAllDynamicShortcuts()
                        CreateCustomToast(context).showToast(R.string.success)
                    }
                    true
                }
            it.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1
        }

        findPreference<Preference>("slidr_settings")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val view = View.inflate(requireActivity(), R.layout.entry_slidr_settings, null)

                val verticalSensitive = view.findViewById<SeekBar>(R.id.edit_vertical_sensitive)
                val horizontalSensitive = view.findViewById<SeekBar>(R.id.edit_horizontal_sensitive)
                val textHorizontalSensitive: MaterialTextView =
                    view.findViewById(R.id.text_horizontal_sensitive)
                val textVerticalSensitive: MaterialTextView =
                    view.findViewById(R.id.text_vertical_sensitive)

                val verticalVelocityThreshold =
                    view.findViewById<SeekBar>(R.id.edit_vertical_velocity_threshold)
                val horizontalVelocityThreshold =
                    view.findViewById<SeekBar>(R.id.edit_horizontal_velocity_threshold)
                val textHorizontalVelocityThreshold: MaterialTextView =
                    view.findViewById(R.id.text_horizontal_velocity_threshold)
                val textVerticalVelocityThreshold: MaterialTextView =
                    view.findViewById(R.id.text_vertical_velocity_threshold)

                val verticalDistanceThreshold =
                    view.findViewById<SeekBar>(R.id.edit_vertical_distance_threshold)
                val horizontalDistanceThreshold =
                    view.findViewById<SeekBar>(R.id.edit_horizontal_distance_threshold)
                val textHorizontalDistanceThreshold: MaterialTextView =
                    view.findViewById(R.id.text_horizontal_distance_threshold)
                val textVerticalDistanceThreshold: MaterialTextView =
                    view.findViewById(R.id.text_vertical_distance_threshold)

                verticalSensitive.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textVerticalSensitive.text = getString(R.string.slidr_sensitive, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                horizontalSensitive.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textHorizontalSensitive.text = getString(R.string.slidr_sensitive, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                verticalVelocityThreshold.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textVerticalVelocityThreshold.text =
                            getString(R.string.slidr_velocity_threshold, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                horizontalVelocityThreshold.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textHorizontalVelocityThreshold.text =
                            getString(R.string.slidr_velocity_threshold, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                verticalDistanceThreshold.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textVerticalDistanceThreshold.text =
                            getString(R.string.slidr_distance_threshold, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                horizontalDistanceThreshold.setOnSeekBarChangeListener(object :
                    OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textHorizontalDistanceThreshold.text =
                            getString(R.string.slidr_distance_threshold, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                val settings = Settings.get()
                    .other().slidrSettings
                verticalSensitive.progress = (settings.vertical_sensitive * 100).toInt()
                horizontalSensitive.progress = (settings.horizontal_sensitive * 100).toInt()

                textHorizontalSensitive.text = getString(
                    R.string.slidr_sensitive,
                    (settings.horizontal_sensitive * 100).toInt()
                )
                textVerticalSensitive.text =
                    getString(R.string.slidr_sensitive, (settings.vertical_sensitive * 100).toInt())

                verticalVelocityThreshold.progress =
                    (settings.vertical_velocity_threshold * 10).toInt()
                horizontalVelocityThreshold.progress =
                    (settings.horizontal_velocity_threshold * 10).toInt()

                textHorizontalVelocityThreshold.text = getString(
                    R.string.slidr_velocity_threshold,
                    (settings.horizontal_velocity_threshold * 10).toInt()
                )
                textVerticalVelocityThreshold.text = getString(
                    R.string.slidr_velocity_threshold,
                    (settings.vertical_velocity_threshold * 10).toInt()
                )

                verticalDistanceThreshold.progress =
                    (settings.vertical_distance_threshold * 100).toInt()
                horizontalDistanceThreshold.progress =
                    (settings.horizontal_distance_threshold * 100).toInt()

                textHorizontalDistanceThreshold.text = getString(
                    R.string.slidr_distance_threshold,
                    (settings.horizontal_distance_threshold * 100).toInt()
                )
                textVerticalDistanceThreshold.text = getString(
                    R.string.slidr_distance_threshold,
                    (settings.vertical_distance_threshold * 100).toInt()
                )

                MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .setCancelable(true)
                    .setNegativeButton(R.string.button_cancel, null)
                    .setNeutralButton(R.string.set_default) { _: DialogInterface?, _: Int ->
                        Settings.get()
                            .other().slidrSettings =
                            SlidrSettings().set_default()
                        requireActivity().recreate()
                    }
                    .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                        val st = SlidrSettings()
                        st.horizontal_sensitive = horizontalSensitive.progress.toFloat() / 100
                        st.vertical_sensitive = verticalSensitive.progress.toFloat() / 100

                        st.horizontal_velocity_threshold =
                            horizontalVelocityThreshold.progress.toFloat() / 10
                        st.vertical_velocity_threshold =
                            verticalVelocityThreshold.progress.toFloat() / 10

                        st.horizontal_distance_threshold =
                            horizontalDistanceThreshold.progress.toFloat() / 100
                        st.vertical_distance_threshold =
                            verticalDistanceThreshold.progress.toFloat() / 100
                        Settings.get()
                            .other().slidrSettings = st
                        requireActivity().recreate()
                    }
                    .show()
                true
            }

        findPreference<Preference>("player_background")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val view = View.inflate(requireActivity(), R.layout.entry_player_background, null)
                val enabledRotation: SwitchMaterial = view.findViewById(R.id.enabled_anim)
                val invertRotation: SwitchMaterial = view.findViewById(R.id.edit_invert_rotation)
                val rotationSpeed = view.findViewById<SeekBar>(R.id.edit_rotation_speed)
                val zoom = view.findViewById<SeekBar>(R.id.edit_zoom)
                val blur = view.findViewById<SeekBar>(R.id.edit_blur)
                val textRotationSpeed: MaterialTextView =
                    view.findViewById(R.id.text_rotation_speed)
                val textZoom: MaterialTextView = view.findViewById(R.id.text_zoom)
                val textBlur: MaterialTextView = view.findViewById(R.id.text_blur)
                zoom.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textZoom.text = getString(R.string.rotate_scale, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                rotationSpeed.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textRotationSpeed.text = getString(R.string.rotate_speed, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                blur.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                    override fun onProgressChanged(
                        seekBar: SeekBar?,
                        progress: Int,
                        fromUser: Boolean
                    ) {
                        textBlur.text = getString(R.string.player_blur, progress)
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                })
                val settings = Settings.get()
                    .other().playerCoverBackgroundSettings
                enabledRotation.isChecked = settings.enabled_rotation
                invertRotation.isChecked = settings.invert_rotation
                blur.progress = settings.blur
                rotationSpeed.progress = (settings.rotation_speed * 1000).toInt()
                zoom.progress = ((settings.zoom - 1) * 10).toInt()
                textZoom.text =
                    getString(R.string.rotate_scale, ((settings.zoom - 1) * 10).toInt())
                textRotationSpeed.text =
                    getString(R.string.rotate_speed, (settings.rotation_speed * 1000).toInt())
                textBlur.text = getString(R.string.player_blur, settings.blur)
                MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .setCancelable(true)
                    .setNegativeButton(R.string.button_cancel, null)
                    .setNeutralButton(R.string.set_default) { _: DialogInterface?, _: Int ->
                        Settings.get()
                            .other().playerCoverBackgroundSettings =
                            PlayerCoverBackgroundSettings().set_default()
                    }
                    .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                        val st = PlayerCoverBackgroundSettings()
                        st.blur = blur.progress
                        st.invert_rotation = invertRotation.isChecked
                        st.enabled_rotation = enabledRotation.isChecked
                        st.rotation_speed = rotationSpeed.progress.toFloat() / 1000
                        st.zoom = zoom.progress.toFloat() / 10 + 1f
                        Settings.get()
                            .other().playerCoverBackgroundSettings = st
                    }
                    .show()
                true
            }
        findPreference<Preference>("local_media_server")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val view = View.inflate(requireActivity(), R.layout.entry_local_server, null)
                val url: TextInputEditText = view.findViewById(R.id.edit_url)
                val password: TextInputEditText = view.findViewById(R.id.edit_password)
                val enabled: SwitchMaterial = view.findViewById(R.id.enabled_server)
                val settings = Settings.get().other().localServer
                url.setText(settings.url)
                password.setText(settings.password)
                enabled.isChecked = settings.enabled
                MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .setCancelable(true)
                    .setNegativeButton(R.string.button_cancel, null)
                    .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                        val enabledVal = enabled.isChecked
                        val urlVal = url.editableText.toString()
                        val passVal = password.editableText.toString()
                        if (enabledVal && (Utils.isEmpty(urlVal) || Utils.isEmpty(passVal))) {
                            return@setPositiveButton
                        }
                        val srv = LocalServerSettings()
                        srv.enabled = enabledVal
                        srv.password = passVal
                        srv.url = urlVal
                        Settings.get().other().localServer = srv
                        provideProxySettings()
                            .setActive(provideProxySettings().activeProxy)
                    }
                    .show()
                true
            }
        findPreference<Preference>("max_bitmap_resolution")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                var sz = -1
                try {
                    sz = newValue.toString().trim { it <= ' ' }.toInt()
                } catch (ignored: NumberFormatException) {
                }
                if (isOverflowCanvas(sz) || sz in 0..99) {
                    return@OnPreferenceChangeListener false
                } else {
                    setMaxResolution(sz)
                }
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("new_loading_dialog")?.isVisible = Utils.hasMarshmallow()

        findPreference<Preference>("rendering_mode")?.let { preference ->
            preference.isVisible = Utils.hasPie()
            preference.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    var sz = 0
                    try {
                        sz = newValue.toString().trim { it <= ' ' }.toInt()
                    } catch (ignored: NumberFormatException) {
                    }
                    setHardwareRendering(sz)
                    requireActivity().recreate()
                    true
                }
        }
        findPreference<Preference>("audio_round_icon")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("show_profile_in_additional_page")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("show_recent_dialogs")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("do_zoom_photo")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("font_size")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("language_ui")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("snow_mode")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                requireActivity().recreate()
                true
            }
        findPreference<Preference>("photo_preview_size")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference?, _: Any? ->
                Settings.get().main().notifyPrefPreviewSizeChanged()
                true
            }
        val defCategory = findPreference<ListPreference>(KEY_DEFAULT_CATEGORY)
        initStartPagePreference(defCategory)

        findPreference<Preference>(KEY_NOTIFICATION)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                if (Utils.hasOreo()) {
                    val intent = Intent()
                    intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                    intent.putExtra(
                        "android.provider.extra.APP_PACKAGE",
                        requireContext().packageName
                    )
                    requireContext().startActivity(intent)
                } else {
                    PlaceFactory.getNotificationSettingsPlace().tryOpenWith(requireActivity())
                }
                true
            }

        findPreference<Preference>(KEY_SECURITY)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                onSecurityClick()
                true
            }

        findPreference<Preference>(KEY_DRAWER_ITEMS)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                PlaceFactory.getDrawerEditPlace().tryOpenWith(requireActivity())
                true
            }

        findPreference<Preference>(KEY_SIDE_DRAWER_ITEMS)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                PlaceFactory.getSideDrawerEditPlace().tryOpenWith(requireActivity())
                true
            }

        findPreference<Preference>(KEY_AVATAR_STYLE)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                showAvatarStyleDialog()
                true
            }

        findPreference<Preference>(KEY_APP_THEME)?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                PlaceFactory.getSettingsThemePlace().tryOpenWith(requireActivity())
                true
            }

        findPreference<Preference>("refresh_audio_token")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                disposables.add(
                    RefreshToken.upgradeTokenRx(
                        accountId,
                        Settings.get().accounts().getAccessToken(accountId)
                    )
                        .fromIOToMain()
                        .subscribe({
                            CreateCustomToast(requireActivity()).showToast(if (it) R.string.success else (R.string.error))
                        }, RxUtils.ignore())
                )
                true
            }

        findPreference<Preference>("version")?.let {
            it.summary =
                Utils.getAppVersionName(requireActivity()) + ", VK API " + API_VERSION
            it.onPreferenceClickListener = Preference.OnPreferenceClickListener {
                val view = View.inflate(requireActivity(), R.layout.dialog_about_us, null)
                val anim: RLottieImageView = view.findViewById(R.id.lottie_animation)
                if (FenrirNative.isNativeLoaded()) {
                    anim.fromRes(
                        R.raw.fenrir,
                        Utils.dp(100f),
                        Utils.dp(100f),
                        intArrayOf(
                            0x333333,
                            getColorPrimary(requireActivity()),
                            0x777777,
                            getColorSecondary(requireActivity())
                        )
                    )
                    anim.playAnimation()
                } else {
                    anim.setImageResource(R.drawable.ic_cat)
                }
                MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .show()
                true
            }
        }

        findPreference<Preference>("additional_debug")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                showAdditionalInfo()
                true
            }

        findPreference<Preference>("notifications_sync")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                disposables.add(
                    InteractorFactory.createAccountInteractor()
                        .getPushSettings(accountId)
                        .fromIOToMain()
                        .subscribe({
                            Settings.get().notifications().resetAccount(accountId)
                            for (i in it) {
                                if (i.disabled_until < 0) {
                                    Settings.get().notifications()
                                        .forceDisable(accountId, i.peer_id)
                                }
                            }
                            CreateCustomToast(requireActivity()).showToast(R.string.success)
                        }, { Utils.showErrorInAdapter(requireActivity(), it) })
                )
                true
            }

        findPreference<Preference>("notification_bubbles")?.isVisible =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

        findPreference<Preference>("scoped_storage")?.let {
            val hasScoped = Utils.hasScopedStorage()
            it.isVisible = hasScoped
            if (hasScoped) {
                it.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        lunchScopedControl()
                        true
                    }
            }
        }

        findPreference<Preference>("reset_notifications_groups")?.let {
            val hasOreo = Utils.hasOreo()
            it.isVisible = hasOreo
            if (hasOreo) {
                it.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        AppNotificationChannels.invalidateSoundChannels(requireActivity())
                        CreateCustomToast(requireActivity())
                            .setDuration(Toast.LENGTH_LONG)
                            .showToastSuccessBottom(R.string.success)
                        true
                    }
            }
        }

        findPreference<Preference>("select_custom_icon")?.let {
            val hasOreo = Utils.hasOreo()
            it.isVisible = hasOreo
            if (hasOreo) {
                it.onPreferenceClickListener =
                    Preference.OnPreferenceClickListener {
                        showSelectIcon()
                        true
                    }
            }
        }

        findPreference<ListPreference>("chat_background")?.let {
            enableChatPhotoBackground(it.value.toInt())
            it.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference?, newValue: Any ->
                    val `val` = newValue.toString()
                    val index = `val`.toInt()
                    enableChatPhotoBackground(index)
                    true
                }
        }

        findPreference<Preference>("fix_dir_time")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                if (!AppPerms.hasReadStoragePermission(requireActivity())) {
                    requestReadPermission.launch()
                    return@OnPreferenceClickListener true
                }
                val properties = DialogProperties()
                properties.selection_mode = DialogConfigs.MULTI_MODE
                properties.selection_type = DialogConfigs.DIR_SELECT
                properties.root = Environment.getExternalStorageDirectory()
                properties.error_dir = Environment.getExternalStorageDirectory()
                properties.offset =
                    Environment.getExternalStorageDirectory()
                properties.extensions = null
                properties.show_hidden_files = false
                val dialog = FilePickerDialog(
                    requireActivity(),
                    properties,
                    ThemesController.currentStyle()
                )
                dialog.setTitle(R.string.fix_dir_time)
                dialog.setDialogSelectionListener {
                    try {
                        for (i in it) {
                            doFixDirTime(i, true)
                        }
                        CreateCustomToast(requireActivity())
                            .setDuration(Toast.LENGTH_LONG)
                            .showToastSuccessBottom(R.string.success)
                    } catch (e: Exception) {
                        CreateCustomToast(requireActivity())
                            .setDuration(Toast.LENGTH_LONG)
                            .showToastError(e.localizedMessage)
                    }
                }
                dialog.show()
                true
            }

        findPreference<Preference>("chat_light_background")?.let {
            it.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    selectLocalImage(false)
                    true
                }
            val bitmap = getDrawerBackgroundFile(requireActivity(), true)
            if (bitmap.exists()) {
                val d = Drawable.createFromPath(bitmap.absolutePath)
                it.icon = d
            } else it.setIcon(R.drawable.dir_photo)
        }

        findPreference<Preference>("chat_dark_background")?.let {
            it.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    selectLocalImage(true)
                    true
                }
            val bitmap = getDrawerBackgroundFile(requireActivity(), false)
            if (bitmap.exists()) {
                val d = Drawable.createFromPath(bitmap.absolutePath)
                it.icon = d
            } else it.setIcon(R.drawable.dir_photo)
        }

        findPreference<Preference>("reset_chat_background")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val chatLight = getDrawerBackgroundFile(requireActivity(), true)
                val chatDark = getDrawerBackgroundFile(requireActivity(), false)
                try {
                    tryDeleteFile(chatLight)
                    tryDeleteFile(chatDark)
                } catch (e: Exception) {
                    CreateCustomToast(activity).setDuration(Toast.LENGTH_LONG)
                        .showToastError(e.message)
                }

                findPreference<Preference>("chat_light_background")?.let { vs ->
                    val bitmap = getDrawerBackgroundFile(requireActivity(), true)
                    if (bitmap.exists()) {
                        val d = Drawable.createFromPath(bitmap.absolutePath)
                        vs.icon = d
                    } else vs.setIcon(R.drawable.dir_photo)
                }

                findPreference<Preference>("chat_dark_background")?.let { vs ->
                    val bitmap = getDrawerBackgroundFile(requireActivity(), false)
                    if (bitmap.exists()) {
                        val d = Drawable.createFromPath(bitmap.absolutePath)
                        vs.icon = d
                    } else vs.setIcon(R.drawable.dir_photo)
                }
                true
            }

        findPreference<CustomTextPreference>("music_dir")?.let { uit ->
            uit.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!AppPerms.hasReadStoragePermission(requireActivity())) {
                        requestReadPermission.launch()
                        return@OnPreferenceClickListener true
                    }
                    val properties = DialogProperties()
                    properties.selection_mode = DialogConfigs.SINGLE_MODE
                    properties.selection_type = DialogConfigs.DIR_SELECT
                    properties.root = Environment.getExternalStorageDirectory()
                    properties.error_dir = Environment.getExternalStorageDirectory()
                    properties.offset =
                        File(Settings.get().other().musicDir)
                    properties.extensions = null
                    properties.show_hidden_files = true
                    val dialog = FilePickerDialog(
                        requireActivity(),
                        properties,
                        ThemesController.currentStyle()
                    )
                    dialog.setTitle(R.string.music_dir)
                    dialog.setDialogSelectionListener {
                        PreferenceManager.getDefaultSharedPreferences(provideApplicationContext())
                            .edit().putString("music_dir", it[0]).apply()
                        uit.refresh()
                    }
                    dialog.show()
                    true
                }
        }
        findPreference<CustomTextPreference>("photo_dir")?.let { uit ->
            uit.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!AppPerms.hasReadStoragePermission(requireActivity())) {
                        requestReadPermission.launch()
                        return@OnPreferenceClickListener true
                    }
                    val properties = DialogProperties()
                    properties.selection_mode = DialogConfigs.SINGLE_MODE
                    properties.selection_type = DialogConfigs.DIR_SELECT
                    properties.root = Environment.getExternalStorageDirectory()
                    properties.error_dir = Environment.getExternalStorageDirectory()
                    properties.offset =
                        File(Settings.get().other().photoDir)
                    properties.extensions = null
                    properties.show_hidden_files = true
                    val dialog = FilePickerDialog(
                        requireActivity(),
                        properties,
                        ThemesController.currentStyle()
                    )
                    dialog.setTitle(R.string.photo_dir)
                    dialog.setDialogSelectionListener {
                        PreferenceManager.getDefaultSharedPreferences(provideApplicationContext())
                            .edit().putString("photo_dir", it[0]).apply()
                        uit.refresh()
                    }
                    dialog.show()
                    true
                }
        }
        findPreference<CustomTextPreference>("video_dir")?.let { uit ->
            uit.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!AppPerms.hasReadStoragePermission(requireActivity())) {
                        requestReadPermission.launch()
                        return@OnPreferenceClickListener true
                    }
                    val properties = DialogProperties()
                    properties.selection_mode = DialogConfigs.SINGLE_MODE
                    properties.selection_type = DialogConfigs.DIR_SELECT
                    properties.root = Environment.getExternalStorageDirectory()
                    properties.error_dir = Environment.getExternalStorageDirectory()
                    properties.offset =
                        File(Settings.get().other().videoDir)
                    properties.extensions = null
                    properties.show_hidden_files = true
                    val dialog = FilePickerDialog(
                        requireActivity(),
                        properties,
                        ThemesController.currentStyle()
                    )
                    dialog.setTitle(R.string.video_dir)
                    dialog.setDialogSelectionListener {
                        PreferenceManager.getDefaultSharedPreferences(provideApplicationContext())
                            .edit().putString("video_dir", it[0]).apply()
                        uit.refresh()
                    }
                    dialog.show()
                    true
                }
        }
        findPreference<CustomTextPreference>("docs_dir")?.let { uit ->
            uit.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!AppPerms.hasReadStoragePermission(requireActivity())) {
                        requestReadPermission.launch()
                        return@OnPreferenceClickListener true
                    }
                    val properties = DialogProperties()
                    properties.selection_mode = DialogConfigs.SINGLE_MODE
                    properties.selection_type = DialogConfigs.DIR_SELECT
                    properties.root = Environment.getExternalStorageDirectory()
                    properties.error_dir = Environment.getExternalStorageDirectory()
                    properties.offset =
                        File(Settings.get().other().docDir)
                    properties.extensions = null
                    properties.show_hidden_files = true
                    val dialog = FilePickerDialog(
                        requireActivity(),
                        properties,
                        ThemesController.currentStyle()
                    )
                    dialog.setTitle(R.string.docs_dir)
                    dialog.setDialogSelectionListener {
                        PreferenceManager.getDefaultSharedPreferences(provideApplicationContext())
                            .edit().putString("docs_dir", it[0]).apply()
                        uit.refresh()
                    }
                    dialog.show()
                    true
                }
        }
        findPreference<CustomTextPreference>("sticker_dir")?.let { uit ->
            uit.onPreferenceClickListener =
                Preference.OnPreferenceClickListener {
                    if (!AppPerms.hasReadStoragePermission(requireActivity())) {
                        requestReadPermission.launch()
                        return@OnPreferenceClickListener true
                    }
                    val properties = DialogProperties()
                    properties.selection_mode = DialogConfigs.SINGLE_MODE
                    properties.selection_type = DialogConfigs.DIR_SELECT
                    properties.root = Environment.getExternalStorageDirectory()
                    properties.error_dir = Environment.getExternalStorageDirectory()
                    properties.offset =
                        File(Settings.get().other().stickerDir)
                    properties.extensions = null
                    properties.show_hidden_files = true
                    val dialog = FilePickerDialog(
                        requireActivity(),
                        properties,
                        ThemesController.currentStyle()
                    )
                    dialog.setTitle(R.string.docs_dir)
                    dialog.setDialogSelectionListener {
                        PreferenceManager.getDefaultSharedPreferences(provideApplicationContext())
                            .edit().putString("sticker_dir", it[0]).apply()
                        uit.refresh()
                    }
                    dialog.show()
                    true
                }
        }

        findPreference<Preference>("show_logs")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                PlaceFactory.getLogsPlace().tryOpenWith(requireActivity())
                true
            }
        findPreference<Preference>("request_executor")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                PlaceFactory.getRequestExecutorPlace(
                    accountId
                ).tryOpenWith(requireActivity())
                true
            }
        findPreference<Preference>("cache_cleaner")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                Stores.getInstance().tempStore().clearAll()
                Stores.getInstance().searchQueriesStore().clearAll()
                cleanUICache(requireActivity(), false)
                cleanCache(requireActivity(), true)
                true
            }
        findPreference<Preference>("ui_cache_cleaner")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                cleanUICache(requireActivity(), true)
                true
            }
        findPreference<Preference>("account_cache_cleaner")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                DBHelper.removeDatabaseFor(requireActivity(), accountId)
                cleanUICache(requireActivity(), false)
                cleanCache(requireActivity(), true)
                true
            }
        findPreference<Preference>("blacklist")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                PlaceFactory.getUserBlackListPlace(
                    accountId
                ).tryOpenWith(requireActivity())
                true
            }
        findPreference<Preference>("friends_by_phone")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                if (!AppPerms.hasContactsPermission(requireActivity())) {
                    requestContactsPermission.launch()
                } else {
                    PlaceFactory.getFriendsByPhonesPlace(accountId).tryOpenWith(requireActivity())
                }
                true
            }
        findPreference<Preference>("proxy")?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                startActivity(Intent(requireActivity(), ProxyManagerActivity::class.java))
                true
            }
        findPreference<Preference>("keep_longpoll")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference: Preference, newValue: Any ->
                val keep = newValue as Boolean
                if (keep) {
                    KeepLongpollService.start(preference.context)
                } else {
                    KeepLongpollService.stop(preference.context)
                }
                true
            }
        findPreference<Preference>("compress_traffic")?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _: Preference, newValue: Any ->
                val enable = newValue as Boolean
                Utils.setCompressTraffic(enable)
                true
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).setSupportActionBar(view.findViewById(R.id.toolbar))
    }

    private fun onSecurityClick() {
        if (Settings.get().security().isUsePinForSecurity) {
            requestPin.launch(Intent(requireActivity(), EnterPinActivity::class.java))
        } else {
            PlaceFactory.getSecuritySettingsPlace().tryOpenWith(requireActivity())
        }
    }

    @Throws(IOException::class)
    private fun tryDeleteFile(file: File) {
        if (file.exists() && !file.delete()) {
            throw IOException("Can't delete file $file")
        }
    }

    private fun changeDrawerBackground(isDark: Boolean, data: Intent?) {
        val photos: ArrayList<LocalPhoto?>? = data?.getParcelableArrayListExtra(PHOTOS)
        if (Utils.isEmpty(photos)) {
            return
        }
        photos ?: return
        val photo = photos[0]
        photo ?: return
        val light = !isDark
        val file = getDrawerBackgroundFile(requireActivity(), light)
        var original: Bitmap?
        try {
            original = BitmapFactory.decodeFile(photo.fullImageUri.path)
            original = checkBitmap(original)
            original?.let {
                FileOutputStream(file).use { fos ->
                    it.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                    fos.flush()
                    fos.close()
                    it.recycle()
                    val d = Drawable.createFromPath(file.absolutePath)
                    if (light) {
                        findPreference<Preference>("chat_light_background")?.icon = d
                    } else {
                        findPreference<Preference>("chat_dark_background")?.icon = d
                    }
                }
            }
        } catch (e: Exception) {
            CreateCustomToast(requireActivity()).setDuration(Toast.LENGTH_LONG)
                .showToastError(e.message)
        }
    }

    private fun pushToken(): String? {
        val accountId = Settings.get().accounts().current
        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return null
        }
        val available = Settings.get().pushSettings().registrations
        val can = available.size == 1 && available[0].userId == accountId
        return if (can) available[0].gmcToken else null
    }

    @SuppressLint("SetTextI18n")
    private fun showAdditionalInfo() {
        val view = View.inflate(requireActivity(), R.layout.dialog_additional_us, null)
        (view.findViewById<View>(R.id.item_user_agent) as TextView).text =
            "User-Agent: " + USER_AGENT_ACCOUNT()
        (view.findViewById<View>(R.id.item_device_id) as TextView).text =
            "Device-ID: " + Utils.getDeviceId(requireActivity())
        (view.findViewById<View>(R.id.item_gcm_token) as TextView).text =
            "GMS-Token: " + pushToken()
        MaterialAlertDialogBuilder(requireActivity())
            .setView(view)
            .show()
    }

    private fun showSelectIcon() {
        val view = View.inflate(requireActivity(), R.layout.icon_select_alert, null)
        view.findViewById<View>(R.id.default_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                DefaultFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.blue_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                BlueFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.green_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                GreenFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.violet_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                VioletFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.red_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                RedFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.yellow_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                YellowFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.black_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                BlackFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.vk_official).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                VKFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.white_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                WhiteFenrirAlias::class.java
            )
        }
        view.findViewById<View>(R.id.lineage_icon).setOnClickListener {
            ToggleAlias().toggleTo(
                requireActivity(),
                LineageFenrirAlias::class.java
            )
        }
        MaterialAlertDialogBuilder(requireActivity())
            .setView(view)
            .show()
    }

    private fun resolveAvatarStyleViews(style: Int, circle: ImageView, oval: ImageView) {
        when (style) {
            AvatarStyle.CIRCLE -> {
                circle.visibility = View.VISIBLE
                oval.visibility = View.INVISIBLE
            }
            AvatarStyle.OVAL -> {
                circle.visibility = View.INVISIBLE
                oval.visibility = View.VISIBLE
            }
        }
    }

    private fun showAvatarStyleDialog() {
        val current = Settings.get()
            .ui()
            .avatarStyle
        val view = View.inflate(requireActivity(), R.layout.dialog_avatar_style, null)
        val ivCircle = view.findViewById<ImageView>(R.id.circle_avatar)
        val ivOval = view.findViewById<ImageView>(R.id.oval_avatar)
        val ivCircleSelected = view.findViewById<ImageView>(R.id.circle_avatar_selected)
        val ivOvalSelected = view.findViewById<ImageView>(R.id.oval_avatar_selected)
        ivCircle.setOnClickListener {
            resolveAvatarStyleViews(
                AvatarStyle.CIRCLE,
                ivCircleSelected,
                ivOvalSelected
            )
        }
        ivOval.setOnClickListener {
            resolveAvatarStyleViews(
                AvatarStyle.OVAL,
                ivCircleSelected,
                ivOvalSelected
            )
        }
        resolveAvatarStyleViews(current, ivCircleSelected, ivOvalSelected)
        with()
            .load(R.drawable.ava_settings)
            .transform(RoundTransformation())
            .into(ivCircle)
        with()
            .load(R.drawable.ava_settings)
            .transform(EllipseTransformation())
            .into(ivOval)
        MaterialAlertDialogBuilder(requireActivity())
            .setTitle(R.string.avatar_style_title)
            .setView(view)
            .setPositiveButton(R.string.button_ok) { _: DialogInterface?, _: Int ->
                val circle = ivCircleSelected.visibility == View.VISIBLE
                Settings.get()
                    .ui()
                    .storeAvatarStyle(if (circle) AvatarStyle.CIRCLE else AvatarStyle.OVAL)
                requireActivity().recreate()
            }
            .setNegativeButton(R.string.button_cancel, null)
            .show()
    }

    private val accountId: Int
        get() = requireArguments().getInt(ACCOUNT_ID)

    private fun initStartPagePreference(lp: ListPreference?) {
        val drawerSettings = Settings.get()
            .drawerSettings()
        val enabledCategoriesName = ArrayList<String>()
        val enabledCategoriesValues = ArrayList<String>()
        enabledCategoriesName.add(getString(R.string.last_closed_page))
        enabledCategoriesValues.add("last_closed")
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.FRIENDS)) {
            enabledCategoriesName.add(getString(R.string.friends))
            enabledCategoriesValues.add("1")
        }
        enabledCategoriesName.add(getString(R.string.dialogs))
        enabledCategoriesValues.add("2")
        enabledCategoriesName.add(getString(R.string.feed))
        enabledCategoriesValues.add("3")
        enabledCategoriesName.add(getString(R.string.drawer_feedback))
        enabledCategoriesValues.add("4")
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.GROUPS)) {
            enabledCategoriesName.add(getString(R.string.groups))
            enabledCategoriesValues.add("5")
        }
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.PHOTOS)) {
            enabledCategoriesName.add(getString(R.string.photos))
            enabledCategoriesValues.add("6")
        }
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.VIDEOS)) {
            enabledCategoriesName.add(getString(R.string.videos))
            enabledCategoriesValues.add("7")
        }
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.MUSIC)) {
            enabledCategoriesName.add(getString(R.string.music))
            enabledCategoriesValues.add("8")
        }
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.DOCS)) {
            enabledCategoriesName.add(getString(R.string.attachment_documents))
            enabledCategoriesValues.add("9")
        }
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.BOOKMARKS)) {
            enabledCategoriesName.add(getString(R.string.bookmarks))
            enabledCategoriesValues.add("10")
        }
        enabledCategoriesName.add(getString(R.string.search))
        enabledCategoriesValues.add("11")
        if (drawerSettings.isCategoryEnabled(SwitchableCategory.NEWSFEED_COMMENTS)) {
            enabledCategoriesName.add(getString(R.string.drawer_newsfeed_comments))
            enabledCategoriesValues.add("12")
        }
        lp?.entries = enabledCategoriesName.toTypedArray<CharSequence>()
        lp?.entryValues = enabledCategoriesValues.toTypedArray<CharSequence>()
    }

    override fun onResume() {
        super.onResume()
        Settings.get().ui().notifyPlaceResumed(Place.PREFERENCES)
        val actionBar = ActivityUtils.supportToolbarFor(this)
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings)
            actionBar.subtitle = null
        }
        if (requireActivity() is OnSectionResumeCallback) {
            (requireActivity() as OnSectionResumeCallback).onSectionResume(AbsNavigationFragment.SECTION_ITEM_SETTINGS)
        }
        ActivityFeatures.Builder()
            .begin()
            .setHideNavigationMenu(false)
            .setBarsColored(requireActivity(), true)
            .build()
            .apply(requireActivity())
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose()
    }

    companion object {
        const val KEY_DEFAULT_CATEGORY = "default_category"
        const val KEY_AVATAR_STYLE = "avatar_style"
        private const val KEY_APP_THEME = "app_theme"
        private const val KEY_NIGHT_SWITCH = "night_switch"
        private const val KEY_NOTIFICATION = "notifications"
        private const val KEY_SECURITY = "security"
        private const val KEY_DRAWER_ITEMS = "drawer_categories"
        private const val KEY_SIDE_DRAWER_ITEMS = "side_drawer_categories"

        @JvmStatic
        fun buildArgs(accountId: Int): Bundle {
            val args = Bundle()
            args.putInt(ACCOUNT_ID, accountId)
            return args
        }

        @JvmStatic
        fun newInstance(args: Bundle?): PreferencesFragment {
            val fragment = PreferencesFragment()
            fragment.arguments = args
            return fragment
        }

        fun getDrawerBackgroundFile(context: Context, light: Boolean): File {
            return File(context.filesDir, if (light) "chat_light.jpg" else "chat_dark.jpg")
        }

        @JvmStatic
        fun cleanCache(context: Context, notify: Boolean) {
            try {
                clear_cache()
                var cache = File(context.cacheDir, "notif-cache")
                if (cache.exists() && cache.isDirectory) {
                    val children = cache.list()
                    if (children != null) {
                        for (child in children) {
                            val rem = File(cache, child)
                            if (rem.isFile) {
                                rem.delete()
                            }
                        }
                    }
                }
                cache = File(context.cacheDir, "lottie_cache")
                if (cache.exists() && cache.isDirectory) {
                    val children = cache.list()
                    if (children != null) {
                        for (child in children) {
                            val rem = File(cache, child)
                            if (rem.isFile) {
                                rem.delete()
                            }
                        }
                    }
                }
                cache = File(context.cacheDir, "video_network_cache")
                if (cache.exists() && cache.isDirectory) {
                    val children = cache.list()
                    if (children != null) {
                        for (child in children) {
                            val rem = File(cache, child)
                            if (rem.isFile) {
                                rem.delete()
                            }
                        }
                    }
                }
                cache = AudioRecordWrapper.getRecordingDirectory(context)
                if (cache.exists() && cache.isDirectory) {
                    val children = cache.list()
                    if (children != null) {
                        for (child in children) {
                            val rem = File(cache, child)
                            if (rem.isFile) {
                                rem.delete()
                            }
                        }
                    }
                }
                if (notify) CreateCustomToast(context).showToast(R.string.success)
            } catch (e: Exception) {
                e.printStackTrace()
                if (notify) CreateCustomToast(context).showToastError(e.localizedMessage)
            }
        }

        @JvmStatic
        fun cleanUICache(context: Context, notify: Boolean) {
            try {
                var cache = File(context.cacheDir, "lottie_cache/rendered")
                if (cache.exists() && cache.isDirectory) {
                    val children = cache.list()
                    if (children != null) {
                        for (child in children) {
                            val rem = File(cache, child)
                            if (rem.isFile) {
                                rem.delete()
                            }
                        }
                    }
                }
                cache = File(context.cacheDir, "video_resource_cache")
                if (cache.exists() && cache.isDirectory) {
                    val children = cache.list()
                    if (children != null) {
                        for (child in children) {
                            val rem = File(cache, child)
                            if (rem.isFile) {
                                rem.delete()
                            }
                        }
                    }
                }
                if (notify) CreateCustomToast(context).showToast(R.string.success)
            } catch (e: Exception) {
                e.printStackTrace()
                if (notify) CreateCustomToast(context).showToastError(e.localizedMessage)
            }
        }

        private fun checkBitmap(bitmap: Bitmap?): Bitmap? {
            bitmap ?: return null
            if (bitmap.width <= 0 || bitmap.height <= 0 || bitmap.width <= 4000 && bitmap.height <= 4000) {
                return bitmap
            }
            var mWidth = bitmap.width
            var mHeight = bitmap.height
            val mCo = mHeight.coerceAtMost(mWidth).toFloat() / mHeight.coerceAtLeast(mWidth)
            if (mWidth > mHeight) {
                mWidth = 4000
                mHeight = (4000 * mCo).toInt()
            } else {
                mHeight = 4000
                mWidth = (4000 * mCo).toInt()
            }
            if (mWidth <= 0 || mHeight <= 0) {
                return bitmap
            }
            val tmp = Bitmap.createScaledBitmap(bitmap, mWidth, mHeight, true)
            bitmap.recycle()
            return tmp
        }
    }
}
