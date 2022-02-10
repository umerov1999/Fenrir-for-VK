package dev.ragnarok.fenrir.filepicker.view;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.filepicker.controller.adapters.FileListAdapter;
import dev.ragnarok.fenrir.filepicker.model.DialogConfigs;
import dev.ragnarok.fenrir.filepicker.model.DialogProperties;
import dev.ragnarok.fenrir.filepicker.model.FileListItem;
import dev.ragnarok.fenrir.filepicker.model.MarkedItemList;
import dev.ragnarok.fenrir.filepicker.utils.ExtensionFilter;
import dev.ragnarok.fenrir.filepicker.utils.Utility;
import dev.ragnarok.fenrir.filepicker.widget.MaterialCheckbox;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.AppPerms;

/**
 * @author akshay sunil masram
 */
@SuppressWarnings("unused")
public class FilePickerDialog extends DialogFragment implements AdapterView.OnItemClickListener {

    public static final int EXTERNAL_READ_PERMISSION_GRANT = 112;
    public static final String RESULT_VALUE = "file_picker_result_value";
    private ListView listView;
    private TextView dname, dir_path, title;
    private DialogProperties properties;
    private ArrayList<FileListItem> internalList;
    private ExtensionFilter filter;
    private FileListAdapter mFileListAdapter;
    private Button select;
    private String positiveBtnNameStr;
    private String negativeBtnNameStr;

    public static FilePickerDialog newInstance(@NonNull DialogProperties properties) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.READONLY, properties);
        FilePickerDialog dialog = new FilePickerDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View root = View.inflate(requireActivity(), R.layout.file_picker_dialog_main, null);
        listView = root.findViewById(R.id.fileList);
        select = root.findViewById(R.id.select);
        int size = MarkedItemList.getFileCount();
        if (size == 0) {
            select.setEnabled(false);
            int color = CurrentTheme.getColorOnPrimary(requireActivity());
            select.setTextColor(Color.argb(128, Color.red(color), Color.green(color),
                    Color.blue(color)));
        }
        dname = root.findViewById(R.id.dname);
        title = root.findViewById(R.id.title);
        dir_path = root.findViewById(R.id.dir_path);
        Button cancel = root.findViewById(R.id.cancel);
        if (negativeBtnNameStr != null) {
            cancel.setText(negativeBtnNameStr);
        }
        select.setOnClickListener(view -> {
            String[] paths = MarkedItemList.getSelectedPaths();
            Bundle bundle = new Bundle();
            bundle.putStringArray(RESULT_VALUE, paths);
            getParentFragmentManager().setFragmentResult(properties.request, bundle);
            dismiss();
        });
        cancel.setOnClickListener(view -> dismiss());
        mFileListAdapter = new FileListAdapter(internalList, requireActivity(), properties);
        mFileListAdapter.setNotifyItemCheckedListener(() -> {
            positiveBtnNameStr = positiveBtnNameStr == null ?
                    requireActivity().getResources().getString(R.string.choose_button_label) : positiveBtnNameStr;
            int size1 = MarkedItemList.getFileCount();
            if (size1 == 0) {
                select.setEnabled(false);
                int color = CurrentTheme.getColorOnPrimary(requireActivity());
                select.setTextColor(Color.argb(128, Color.red(color), Color.green(color),
                        Color.blue(color)));
                select.setText(positiveBtnNameStr);
            } else {
                select.setEnabled(true);
                int color = CurrentTheme.getColorOnPrimary(requireActivity());
                select.setTextColor(color);
                String button_label = positiveBtnNameStr + " (" + size1 + ") ";
                select.setText(button_label);
            }
            if (properties.selection_mode == DialogConfigs.SINGLE_MODE) {
                /*  If a single file has to be selected, clear the previously checked
                 *  checkbox from the list.
                 */
                mFileListAdapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(mFileListAdapter);

        //Title method added in version 1.0.5
        setTitle();
        AlertDialog dlg = new Dlg(requireActivity());
        if (properties.tittle != -1) {
            dlg.setTitle(properties.tittle);
        }
        dlg.setView(root);
        return new MaterialAlertDialogBuilder(requireActivity()).create(dlg);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!AppPerms.hasReadStoragePermission(requireActivity())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requireActivity().requestPermissions(new String[]{Manifest.permission
                        .READ_EXTERNAL_STORAGE}, EXTERNAL_READ_PERMISSION_GRANT);
            }
        } else {
            positiveBtnNameStr = positiveBtnNameStr == null ?
                    requireActivity().getResources().getString(R.string.choose_button_label) : positiveBtnNameStr;
            select.setText(positiveBtnNameStr);
            int size = MarkedItemList.getFileCount();
            if (size == 0) {
                select.setText(positiveBtnNameStr);
            } else {
                String button_label = positiveBtnNameStr + " (" + size + ") ";
                select.setText(button_label);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        properties = requireArguments().getParcelable(Extra.READONLY);
        filter = new ExtensionFilter(properties);
        internalList = new ArrayList<>();
    }

    private void setTitle() {
        if (title == null || dname == null) {
            return;
        }

        if (title.getVisibility() == View.VISIBLE) {
            title.setVisibility(View.INVISIBLE);
        }
        if (dname.getVisibility() == View.INVISIBLE) {
            dname.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        positiveBtnNameStr = (
                positiveBtnNameStr == null ?
                        requireActivity().getResources().getString(R.string.choose_button_label) :
                        positiveBtnNameStr
        );
        select.setText(positiveBtnNameStr);
        if (AppPerms.hasReadStoragePermission(requireActivity())) {
            File currLoc;
            internalList.clear();
            if (new File(properties.offset).isDirectory() && validateOffsetPath()) {
                currLoc = new File(properties.offset);
                FileListItem parent = new FileListItem();
                parent.setFilename(requireActivity().getString(R.string.label_parent_dir));
                parent.setDirectory(true);
                parent.setLocation(Objects.requireNonNull(currLoc.getParentFile())
                        .getAbsolutePath());
                parent.setTime(currLoc.lastModified());
                internalList.add(parent);
            } else if (new File(properties.root).exists() && (new File(properties.root).isDirectory())) {
                currLoc = new File(properties.root);
            } else {
                currLoc = new File(properties.error_dir);
            }
            dname.setText(currLoc.getName());
            dir_path.setText(currLoc.getAbsolutePath());
            setTitle();
            internalList = Utility.prepareFileListEntries(internalList, currLoc, filter,
                    properties.show_hidden_files);
            mFileListAdapter.notifyDataSetChanged();
            listView.setOnItemClickListener(this);
        }
    }

    private boolean validateOffsetPath() {
        String offset_path = properties.offset;
        String root_path = properties.root;
        return !offset_path.equals(root_path) && offset_path.contains(root_path);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (internalList.size() > i) {
            FileListItem fitem = internalList.get(i);
            if (fitem.isDirectory()) {
                if (new File(fitem.getLocation()).canRead()) {
                    File currLoc = new File(fitem.getLocation());
                    dname.setText(currLoc.getName());
                    setTitle();
                    dir_path.setText(currLoc.getAbsolutePath());
                    internalList.clear();
                    if (!currLoc.getName().equals(new File(properties.root).getName())) {
                        FileListItem parent = new FileListItem();
                        parent.setFilename(requireActivity().getString(R.string.label_parent_dir));
                        parent.setDirectory(true);
                        parent.setLocation(Objects.requireNonNull(currLoc
                                .getParentFile()).getAbsolutePath());
                        parent.setTime(currLoc.lastModified());
                        internalList.add(parent);
                    }
                    internalList = Utility.prepareFileListEntries(internalList, currLoc, filter,
                            properties.show_hidden_files);
                    mFileListAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(requireActivity(), R.string.error_dir_access,
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                MaterialCheckbox fmark = view.findViewById(R.id.file_mark);
                fmark.performClick();
            }
        }
    }

    public void setPositiveBtnName(CharSequence positiveBtnNameStr) {
        if (positiveBtnNameStr != null) {
            this.positiveBtnNameStr = positiveBtnNameStr.toString();
        } else {
            this.positiveBtnNameStr = null;
        }
    }

    public void setNegativeBtnName(CharSequence negativeBtnNameStr) {
        if (negativeBtnNameStr != null) {
            this.negativeBtnNameStr = negativeBtnNameStr.toString();
        } else {
            this.negativeBtnNameStr = null;
        }
    }

    public void markFiles(List<String> paths) {
        if (paths != null && paths.size() > 0) {
            if (properties.selection_mode == DialogConfigs.SINGLE_MODE) {
                File temp = new File(paths.get(0));
                switch (properties.selection_type) {
                    case DialogConfigs.DIR_SELECT:
                        if (temp.exists() && temp.isDirectory()) {
                            FileListItem item = new FileListItem();
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            item.setLocation(temp.getAbsolutePath());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;

                    case DialogConfigs.FILE_SELECT:
                        if (temp.exists() && temp.isFile()) {
                            FileListItem item = new FileListItem();
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            item.setLocation(temp.getAbsolutePath());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;

                    case DialogConfigs.FILE_AND_DIR_SELECT:
                        if (temp.exists()) {
                            FileListItem item = new FileListItem();
                            item.setFilename(temp.getName());
                            item.setDirectory(temp.isDirectory());
                            item.setMarked(true);
                            item.setTime(temp.lastModified());
                            item.setLocation(temp.getAbsolutePath());
                            MarkedItemList.addSelectedItem(item);
                        }
                        break;
                }
            } else {
                for (String path : paths) {
                    switch (properties.selection_type) {
                        case DialogConfigs.DIR_SELECT:
                            File temp = new File(path);
                            if (temp.exists() && temp.isDirectory()) {
                                FileListItem item = new FileListItem();
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                item.setLocation(temp.getAbsolutePath());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;

                        case DialogConfigs.FILE_SELECT:
                            temp = new File(path);
                            if (temp.exists() && temp.isFile()) {
                                FileListItem item = new FileListItem();
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                item.setLocation(temp.getAbsolutePath());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;

                        case DialogConfigs.FILE_AND_DIR_SELECT:
                            temp = new File(path);
                            if (temp.exists() && (temp.isFile() || temp.isDirectory())) {
                                FileListItem item = new FileListItem();
                                item.setFilename(temp.getName());
                                item.setDirectory(temp.isDirectory());
                                item.setMarked(true);
                                item.setTime(temp.lastModified());
                                item.setLocation(temp.getAbsolutePath());
                                MarkedItemList.addSelectedItem(item);
                            }
                            break;
                    }
                }
            }
        }
    }

    @Override
    public void dismiss() {
        MarkedItemList.clearSelectionList();
        internalList.clear();
        super.dismiss();
    }

    private class Dlg extends AlertDialog {
        protected Dlg(@NonNull Context context) {
            super(context);
        }

        protected Dlg(@NonNull Context context, int themeResId) {
            super(context, themeResId);
        }

        protected Dlg(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
            super(context, cancelable, cancelListener);
        }

        @Override
        public void onBackPressed() {
            //currentDirName is dependent on dname
            String currentDirName = dname.getText().toString();
            if (internalList.size() > 0) {
                FileListItem fitem = internalList.get(0);
                File currLoc = new File(fitem.getLocation());
                if (currentDirName.equals(new File(properties.root).getName()) ||
                        !currLoc.canRead()) {
                    super.onBackPressed();
                } else {
                    dname.setText(currLoc.getName());
                    dir_path.setText(currLoc.getAbsolutePath());
                    internalList.clear();
                    if (!currLoc.getName().equals(new File(properties.root).getName())) {
                        FileListItem parent = new FileListItem();
                        parent.setFilename(requireActivity().getString(R.string.label_parent_dir));
                        parent.setDirectory(true);
                        parent.setLocation(Objects.requireNonNull(currLoc.getParentFile())
                                .getAbsolutePath());
                        parent.setTime(currLoc.lastModified());
                        internalList.add(parent);
                    }
                    internalList = Utility.prepareFileListEntries(internalList, currLoc, filter,
                            properties.show_hidden_files);
                    mFileListAdapter.notifyDataSetChanged();
                }
                FilePickerDialog.this.setTitle();
            } else {
                super.onBackPressed();
            }
        }
    }
}
