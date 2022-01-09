package dev.ragnarok.fenrir.fragment.search;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.SearchOptionsAdapter;
import dev.ragnarok.fenrir.dialog.SelectChairsDialog;
import dev.ragnarok.fenrir.dialog.SelectCityDialog;
import dev.ragnarok.fenrir.dialog.SelectCountryDialog;
import dev.ragnarok.fenrir.dialog.SelectFacultyDialog;
import dev.ragnarok.fenrir.dialog.SelectSchoolClassesDialog;
import dev.ragnarok.fenrir.dialog.SelectSchoolsDialog;
import dev.ragnarok.fenrir.dialog.SelectUniversityDialog;
import dev.ragnarok.fenrir.fragment.search.options.BaseOption;
import dev.ragnarok.fenrir.fragment.search.options.DatabaseOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleBooleanOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleDateOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleGPSOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleNumberOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleTextOption;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.InputTextDialog;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.DateTimePicker;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class FilterEditFragment extends BottomSheetDialogFragment implements SearchOptionsAdapter.OptionClickListener {

    public static final String REQUEST_FILTER_EDIT = "request_filter_edit";
    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private final AppPerms.doRequestPermissions requestGPSPermission = AppPerms.requestPermissions(this,
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
            () -> CustomToast.CreateCustomToast(requireActivity()).showToast(R.string.permission_all_granted_text));
    private ArrayList<BaseOption> mData;
    private SearchOptionsAdapter mAdapter;
    private int mAccountId;
    private TextView mEmptyText;

    public static FilterEditFragment newInstance(int accountId, ArrayList<BaseOption> options) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelableArrayList(Extra.LIST, options);
        FilterEditFragment fragment = new FilterEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private void onDialogResult(@NonNull Bundle result) {
        int key = result.getInt(Extra.KEY);
        Integer id = result.containsKey(Extra.ID) ? result.getInt(Extra.ID) : null;
        String title = result.containsKey(Extra.TITLE) ? result.getString(Extra.TITLE) : null;

        mergeDatabaseOptionValue(key, id == null ? null : new DatabaseOption.Entry(id, title));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID);
        mData = requireArguments().getParcelableArrayList(Extra.LIST);
    }

    private void resolveEmptyTextVisibility() {
        if (Objects.nonNull(mEmptyText)) {
            mEmptyText.setVisibility(Utils.isEmpty(mData) ? View.VISIBLE : View.GONE);
        }
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View root = View.inflate(requireActivity(), R.layout.sheet_filter_edirt, null);

        Toolbar toolbar = root.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.search_options);

        toolbar.setNavigationIcon(R.drawable.check);
        toolbar.setNavigationOnClickListener(menuItem ->
                onSaveClick());

        mEmptyText = root.findViewById(R.id.empty_text);

        RecyclerView mRecyclerView = root.findViewById(R.id.recycler_view);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);

        mAdapter = new SearchOptionsAdapter(mData);
        mAdapter.setOptionClickListener(this);

        mRecyclerView.setAdapter(mAdapter);
        resolveEmptyTextVisibility();

        dialog.setContentView(root);
    }

    private void onSaveClick() {
        Bundle data = new Bundle();
        data.putParcelableArrayList(Extra.LIST, mData);
        getParentFragmentManager().setFragmentResult(REQUEST_FILTER_EDIT, data);
        dismiss();
    }

    @Override
    public void onSpinnerOptionClick(SpinnerOption spinnerOption) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(spinnerOption.title)
                .setItems(spinnerOption.createAvailableNames(requireActivity()), (dialog, which) -> {
                    spinnerOption.value = spinnerOption.available.get(which);
                    mAdapter.notifyDataSetChanged();
                })
                .setNegativeButton(R.string.clear, (dialog, which) -> {
                    spinnerOption.value = null;
                    mAdapter.notifyDataSetChanged();
                })
                .setPositiveButton(R.string.button_cancel, null)
                .show();
    }

    private void mergeDatabaseOptionValue(int key, DatabaseOption.Entry value) {
        for (BaseOption option : mData) {
            if (option.key == key && option instanceof DatabaseOption) {
                DatabaseOption databaseOption = (DatabaseOption) option;
                databaseOption.value = value;
                resetChildDependensies(databaseOption.childDependencies);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void mergeGPSOptionValue(SimpleGPSOption value) {
        for (BaseOption option : mData) {
            if (option.key == value.key && option instanceof SimpleGPSOption) {
                SimpleGPSOption gpsOption = (SimpleGPSOption) option;
                gpsOption.lat_gps = value.lat_gps;
                gpsOption.long_gps = value.long_gps;
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void mergeDateOptionValue(SimpleDateOption value) {
        for (BaseOption option : mData) {
            if (option.key == value.key && option instanceof SimpleDateOption) {
                SimpleDateOption dateOption = (SimpleDateOption) option;
                dateOption.timeUnix = value.timeUnix;
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    private void resetChildDependensies(int... childs) {
        if (childs != null) {
            boolean changed = false;
            for (int key : childs) {
                for (BaseOption option : mData) {
                    if (option.key == key) {
                        option.reset();
                        changed = true;
                    }
                }
            }

            if (changed) {
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDatabaseOptionClick(DatabaseOption databaseOption) {
        BaseOption dependency = findDependencyByKey(databaseOption.parentDependencyKey);

        switch (databaseOption.type) {
            case DatabaseOption.TYPE_COUNTRY:
                SelectCountryDialog selectCountryDialog = new SelectCountryDialog();

                Bundle args = new Bundle();
                args.putInt(Extra.KEY, databaseOption.key);
                args.putInt(Extra.ACCOUNT_ID, mAccountId);
                selectCountryDialog.setArguments(args);
                getParentFragmentManager().setFragmentResultListener(SelectCountryDialog.REQUEST_CODE_COUNTRY, selectCountryDialog, (requestKey, result) -> onDialogResult(result));
                selectCountryDialog.show(getParentFragmentManager(), "countries");
                break;

            case DatabaseOption.TYPE_CITY:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showCitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_UNIVERSITY:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showUniversitiesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_FACULTY:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int universityId = ((DatabaseOption) dependency).value.id;
                    showFacultiesDialog(databaseOption, universityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_CHAIR:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int facultyId = ((DatabaseOption) dependency).value.id;
                    showChairsDialog(databaseOption, facultyId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int cityId = ((DatabaseOption) dependency).value.id;
                    showSchoolsDialog(databaseOption, cityId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;

            case DatabaseOption.TYPE_SCHOOL_CLASS:
                if (dependency instanceof DatabaseOption && ((DatabaseOption) dependency).value != null) {
                    int countryId = ((DatabaseOption) dependency).value.id;
                    showSchoolClassesDialog(databaseOption, countryId);
                } else {
                    String message = getString(R.string.please_select_option, getString(dependency.title));
                    Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    @Override
    public void onSimpleNumberOptionClick(SimpleNumberOption option) {
        new InputTextDialog.Builder(requireActivity())
                .setTitleRes(option.title)
                .setAllowEmpty(true)
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setValue(option.value == null ? null : String.valueOf(option.value))
                .setCallback(newValue -> {
                    option.value = getIntFromEditable(newValue);
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    private Integer getIntFromEditable(String line) {
        if (line == null || TextUtils.getTrimmedLength(line) == 0) {
            return null;
        }

        try {
            return Integer.valueOf(line);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    public void onSimpleTextOptionClick(SimpleTextOption option) {
        new InputTextDialog.Builder(requireActivity())
                .setTitleRes(option.title)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setValue(option.value)
                .setAllowEmpty(true)
                .setCallback(newValue -> {
                    option.value = newValue;
                    mAdapter.notifyDataSetChanged();
                })
                .show();
    }

    @Override
    public void onSimpleBooleanOptionChanged(SimpleBooleanOption option) {

    }

    @Override
    public void onOptionCleared(BaseOption option) {
        resetChildDependensies(option.childDependencies);
    }

    @Override
    public void onGPSOptionClick(SimpleGPSOption gpsOption) {
        new MyLocation().getLocation(requireActivity(), location -> {
            Handler uiHandler = new Handler(requireActivity().getMainLooper());
            uiHandler.post(() -> {
                if (location != null) {
                    gpsOption.lat_gps = location.getLatitude();
                    gpsOption.long_gps = location.getLongitude();
                }
                mergeGPSOptionValue(gpsOption);
            });
        });
    }

    @Override
    public void onDateOptionClick(SimpleDateOption dateOption) {
        new DateTimePicker.Builder(requireActivity())
                .setTime(dateOption.timeUnix == 0 ? Calendar.getInstance().getTime().getTime() / 1000 : dateOption.timeUnix)
                .setCallback(unixtime -> {
                    dateOption.timeUnix = unixtime;
                    mergeDateOptionValue(dateOption);
                })
                .show();
    }

    private void showCitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectCityDialog selectCityDialog = SelectCityDialog.newInstance(mAccountId, countryId, args);
        getParentFragmentManager().setFragmentResultListener(SelectCityDialog.REQUEST_CODE_CITY, selectCityDialog, (requestKey, result) -> onDialogResult(result));
        selectCityDialog.show(getParentFragmentManager(), "cities");
    }

    private void showUniversitiesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectUniversityDialog dialog = SelectUniversityDialog.newInstance(mAccountId, countryId, args);
        getParentFragmentManager().setFragmentResultListener(SelectUniversityDialog.REQUEST_CODE_UNIVERSITY, dialog, (requestKey, result) -> onDialogResult(result));
        dialog.show(getParentFragmentManager(), "universities");
    }

    private void showSchoolsDialog(DatabaseOption databaseOption, int cityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolsDialog dialog = SelectSchoolsDialog.newInstance(mAccountId, cityId, args);
        getParentFragmentManager().setFragmentResultListener(SelectSchoolsDialog.REQUEST_CODE_SCHOOL, dialog, (requestKey, result) -> onDialogResult(result));
        dialog.show(getParentFragmentManager(), "schools");
    }

    private void showFacultiesDialog(DatabaseOption databaseOption, int universityId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectFacultyDialog dialog = SelectFacultyDialog.newInstance(mAccountId, universityId, args);
        getParentFragmentManager().setFragmentResultListener(SelectFacultyDialog.REQUEST_CODE_FACULTY, dialog, (requestKey, result) -> onDialogResult(result));
        dialog.show(getParentFragmentManager(), "faculties");
    }

    private void showChairsDialog(DatabaseOption databaseOption, int facultyId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectChairsDialog dialog = SelectChairsDialog.newInstance(mAccountId, facultyId, args);
        getParentFragmentManager().setFragmentResultListener(SelectChairsDialog.REQUEST_CODE_CHAIRS, dialog, (requestKey, result) -> onDialogResult(result));
        dialog.show(getParentFragmentManager(), "chairs");
    }

    private void showSchoolClassesDialog(DatabaseOption databaseOption, int countryId) {
        Bundle args = new Bundle();
        args.putInt(Extra.KEY, databaseOption.key);

        SelectSchoolClassesDialog dialog = SelectSchoolClassesDialog.newInstance(mAccountId, countryId, args);
        getParentFragmentManager().setFragmentResultListener(SelectSchoolClassesDialog.REQUEST_CODE_SCHOOL_CLASSES, dialog, (requestKey, result) -> onDialogResult(result));
        dialog.show(getParentFragmentManager(), "school-classes");
    }

    private BaseOption findDependencyByKey(int key) {
        if (key == BaseOption.NO_DEPENDENCY) {
            return null;
        }

        for (BaseOption baseOption : mData) {
            if (baseOption.key == key) {
                return baseOption;
            }
        }

        return null;
    }

    @Override
    public void onDestroy() {
        mCompositeDisposable.dispose();
        super.onDestroy();
    }

    public interface LocationResult {
        void gotLocation(Location location);
    }

    class MyLocation {
        Timer timer1;
        LocationManager lm;
        LocationResult locationResult;
        boolean gps_enabled;
        boolean network_enabled;
        LocationListener networkLocation = new LocationListener() {
            public void onLocationChanged(Location location) {
                timer1.cancel();
                locationResult.gotLocation(location);
                lm.removeUpdates(this);
                lm.removeUpdates(gpsLocation);
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            @Deprecated
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        LocationListener gpsLocation = new LocationListener() {
            public void onLocationChanged(Location location) {
                timer1.cancel();
                locationResult.gotLocation(location);
                lm.removeUpdates(this);
                lm.removeUpdates(networkLocation);
            }

            public void onProviderDisabled(String provider) {
            }

            public void onProviderEnabled(String provider) {
            }

            @Deprecated
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        public boolean getLocation(Context context, LocationResult result) {
            if (Utils.hasMarshmallow() && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestGPSPermission.launch();
                return false;
            }
            // I use LocationResult callback class to pass location value from
            // MyLocation to user code.
            locationResult = result;
            if (lm == null)
                lm = (LocationManager) context
                        .getSystemService(Context.LOCATION_SERVICE);

            // Exceptions will be thrown if the provider is not permitted.
            try {
                gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ignored) {
            }
            try {
                network_enabled = lm
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ignored) {
            }

            // Don't start listeners if no provider is enabled.
            if (!gps_enabled && !network_enabled)
                return false;

            if (gps_enabled)
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,
                        gpsLocation);
            if (network_enabled)
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                        networkLocation);
            timer1 = new Timer();
            timer1.schedule(new GetLastLocation(), 5000);
            return true;
        }

        class GetLastLocation extends TimerTask {
            @Override
            public void run() {
                if (Utils.hasMarshmallow() && ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestGPSPermission.launch();
                    return;
                }
                lm.removeUpdates(gpsLocation);
                lm.removeUpdates(networkLocation);

                Location net_loc = null, gps_loc = null;
                if (gps_enabled)
                    gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (network_enabled)
                    net_loc = lm
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                // If there are both values, use the latest one.
                if (gps_loc != null && net_loc != null) {
                    if (gps_loc.getTime() > net_loc.getTime())
                        locationResult.gotLocation(gps_loc);
                    else
                        locationResult.gotLocation(net_loc);
                    return;
                }

                if (gps_loc != null) {
                    locationResult.gotLocation(gps_loc);
                    return;
                }
                if (net_loc != null) {
                    locationResult.gotLocation(net_loc);
                    return;
                }
                locationResult.gotLocation(null);
            }
        }
    }
}
