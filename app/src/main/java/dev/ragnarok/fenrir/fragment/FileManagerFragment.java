package dev.ragnarok.fenrir.fragment;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.FileManagerAdapter;
import dev.ragnarok.fenrir.listener.BackPressCallback;
import dev.ragnarok.fenrir.model.FileItem;
import dev.ragnarok.fenrir.util.InputTextDialog;
import dev.ragnarok.fenrir.util.Logger;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.view.MySearchView;

public class FileManagerFragment extends Fragment implements FileManagerAdapter.ClickListener, BackPressCallback {

    public static final String EXTRA_START_DIRECTOTY = "start_directory";
    public static final String EXTRA_SHOW_CANNOT_READ = "show_cannot_read";
    public static final String EXTRA_FILTER_EXTENSION = "filter_extension";

    public static final String returnDirectoryParameter = "ua.com.vassiliev.androidfilebrowser.directoryPathRet";
    public static final String returnFileParameter = "ua.com.vassiliev.androidfilebrowser.filePathRet";

    public static final int SELECT_DIRECTORY = 1;
    public static final int SELECT_FILE = 0;
    private static final String SAVE_DATA = "save_data";
    private static final String SAVE_PATH = "save_path";
    private static final String SAVE_SCROLL_STATES = "scroll_states";
    private int currentAction;
    // Stores names of traversed directories
    private ArrayList<String> pathDirsList;
    private ArrayList<FileItem> fileList;
    private ArrayList<FileItem> fileList_search;

    private boolean showHiddenFilesAndDirs = true;
    private String filterFileExtension;
    private String q;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView empty;
    private TextView tvCurrentDir;
    private FileManagerAdapter mAdapter;
    private File path;
    private FilenameFilter filter;
    private DirectoryScrollPositions directoryScrollPositions;

    public static String formatBytes(long bytes) {
        // TODO: add flag to which part is needed (e.g. GB, MB, KB or bytes)

        String retStr = "";

        // One binary gigabyte equals 1,073,741,824 bytes.
        if (bytes > 1073741824) {// Add GB
            long gbs = bytes / 1073741824;
            retStr += (gbs) + "GB ";
            bytes = bytes - (gbs * 1073741824);
        }

        // One MB - 1048576 bytes
        if (bytes > 1048576) {// Add GB
            long mbs = bytes / 1048576;
            retStr += (mbs) + "MB ";
            bytes = bytes - (mbs * 1048576);
        }

        if (bytes > 1024) {
            long kbs = bytes / 1024;
            retStr += (kbs) + "KB";
            bytes = bytes - (kbs * 1024);
        } else {
            retStr += (bytes) + " bytes";
        }

        return retStr;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            restoreFromSavedInstanceState(savedInstanceState);
        }
        fileList_search = new ArrayList<>();

        setHasOptionsMenu(true);

        currentAction = requireArguments().getInt(Extra.ACTION);

        showHiddenFilesAndDirs = requireArguments().getBoolean(EXTRA_SHOW_CANNOT_READ, true);
        filterFileExtension = requireArguments().getString(EXTRA_FILTER_EXTENSION);

        filter = (dir, filename) -> {
            File sel = new File(dir, filename);
            boolean showReadableFile = showHiddenFilesAndDirs || sel.canRead();
            // Filters based on whether the file is hidden or not
            if (currentAction == SELECT_DIRECTORY) {
                return sel.isDirectory() && showReadableFile;
            }

            if (currentAction == SELECT_FILE) {
                // If it is a file check the extension if provided
                if (sel.isFile() && filterFileExtension != null) {
                    return showReadableFile && sel.getName().endsWith(filterFileExtension);
                }

                return (showReadableFile);
            }

            return true;
        };
    }

    public void fireSearchRequestChanged(String q) {
        if (fileList == null) {
            return;
        }
        String query = q == null ? null : q.trim();

        if (Objects.safeEquals(q, this.q)) {
            return;
        }
        this.q = query;
        fileList_search.clear();
        for (FileItem i : fileList) {
            if (isEmpty(i.file)) {
                continue;
            }
            if (i.file.toLowerCase().contains(q.toLowerCase())) {
                fileList_search.add(i);
            }
        }

        if (!isEmpty(q))
            mAdapter.setItems(fileList_search);
        else
            mAdapter.setItems(fileList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_file_explorer, container, false);

        mRecyclerView = root.findViewById(R.id.list);
        empty = root.findViewById(R.id.empty);

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fireSearchRequestChanged(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fireSearchRequestChanged(newText);
                return false;
            }
        });

        mLinearLayoutManager = new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setHasFixedSize(Boolean.TRUE);

        ImageView btnSelectCurrentDir = root.findViewById(R.id.select_current_directory_button);
        tvCurrentDir = root.findViewById(R.id.current_path);

        btnSelectCurrentDir.setVisibility(currentAction == SELECT_DIRECTORY ? View.VISIBLE : View.GONE);

        return root;
    }

    private void resolveEmptyText() {
        empty.setVisibility(Utils.safeIsEmpty(fileList) ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (directoryScrollPositions == null) {
            directoryScrollPositions = new DirectoryScrollPositions();
        }

        if (fileList == null) {
            fileList = new ArrayList<>();
            setInitialDirectory();
            loadFileList();
        } else {
            resolveEmptyText();
        }

        mAdapter = new FileManagerAdapter(fileList);
        mAdapter.setClickListener(this);
        mRecyclerView.setAdapter(mAdapter);

        parseDirectoryPath();
        updateCurrentDirectoryTextView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(SAVE_DATA, fileList);
        outState.putSerializable(SAVE_PATH, path);
        outState.putParcelable(SAVE_SCROLL_STATES, directoryScrollPositions);
    }

    private void restoreFromSavedInstanceState(Bundle state) {
        fileList = state.getParcelableArrayList(SAVE_DATA);
        path = (File) state.getSerializable(SAVE_PATH);
        directoryScrollPositions = state.getParcelable(SAVE_SCROLL_STATES);
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Toast.makeText(requireActivity(), e.toString(), Toast.LENGTH_LONG).show();
        }

        fileList.clear();

        if (path.exists() && path.canRead()) {

            String[] fList = path.list(filter);
            if (fList == null)
                return;

            for (int i = 0; i < fList.length; i++) {
                // Convert into file path
                File file = new File(path, fList[i]);
                int drawableID = R.drawable.file_fm;
                boolean canRead = file.canRead();
                boolean isDirectory = file.isDirectory();
                long mod = file.lastModified();

                // Set drawables
                if (isDirectory) {
                    if (canRead) {
                        drawableID = R.drawable.directory_can_read;
                    } else {
                        drawableID = R.drawable.directory_cant_read;
                    }
                }

                String details = isDirectory ? null : formatBytes(file.length());
                fileList.add(i, new FileItem(isDirectory, fList[i], details, drawableID, mod, file.getAbsolutePath(), canRead));
            }
            ArrayList<FileItem> dirsList = new ArrayList<>();
            ArrayList<FileItem> flsList = new ArrayList<>();
            for (FileItem i : fileList) {
                if (i.directory)
                    dirsList.add(i);
                else
                    flsList.add(i);
            }
            Collections.sort(dirsList, new ItemModificationComparator());
            Collections.sort(flsList, new ItemModificationComparator());
            fileList.clear();
            fileList.addAll(dirsList);
            fileList.addAll(flsList);
        }

        resolveEmptyText();
    }

    private void returnDirectoryFinishActivity() {
        Intent retIntent = new Intent();
        retIntent.putExtra(returnDirectoryParameter, path.getAbsolutePath());
        requireActivity().setResult(Activity.RESULT_OK, retIntent);
        requireActivity().finish();
    }

    private void returnFileFinishActivity(String filePath) {
        Intent retIntent = new Intent();
        retIntent.putExtra(returnFileParameter, filePath);
        requireActivity().setResult(Activity.RESULT_OK, retIntent);
        requireActivity().finish();
    }

    private void parseDirectoryPath() {
        pathDirsList = new ArrayList<>();
        String pathString = path.getAbsolutePath();
        String[] parts = pathString.split("/");

        pathDirsList.addAll(Arrays.asList(parts));
    }

    private void updateCurrentDirectoryTextView() {
        String curDirString = TextUtils.join("/", pathDirsList) + "/";

        if (pathDirsList.isEmpty()) {
            curDirString = "/";
        }

        tvCurrentDir.setText(curDirString);
    }

    /*private void resolveToolbar() {
        if (!isAdded()) return;

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.file_explorer);
            switch (currentAction) {
                case SELECT_DIRECTORY:
                    actionBar.setSubtitle(R.string.select_directory);
                    break;
                case SELECT_FILE:
                    actionBar.setSubtitle(R.string.select_file);
                    break;
            }
        }
    }*/

    @Override
    public void onResume() {
        super.onResume();
        //resolveToolbar();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.add(R.string.create_dir)
                .setIcon(R.drawable.plus)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener(item -> {
                    showTextInputDialog();
                    return true;
                });
    }

    private void loadDirectoryUp() {
        mRecyclerView.stopScroll();

        // present directory removed from list
        String s = pathDirsList.remove(pathDirsList.size() - 1);
        // path modified to exclude present directory
        path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));

        loadFileList();
        mAdapter.notifyDataSetChanged();
        updateCurrentDirectoryTextView();

        Parcelable managerState = directoryScrollPositions.states.get(path.getAbsolutePath());
        if (managerState != null) {
            mLinearLayoutManager.onRestoreInstanceState(managerState);
            directoryScrollPositions.states.remove(path.getAbsolutePath());
        }
    }

    /**
     * Отображение диалога для ввода имени новой папки
     */
    private void showTextInputDialog() {
        new InputTextDialog.Builder(requireActivity())
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setTitleRes(R.string.enter_dir_name)
                .setAllowEmpty(false)
                .setCallback(newValue -> {
                    File file = new File(path.getAbsolutePath() + "/" + newValue);
                    if (!file.exists() && file.mkdir()) {
                        loadFileList();
                        mAdapter.notifyDataSetChanged();
                        updateCurrentDirectoryTextView();
                    } else {
                        Toast.makeText(requireActivity(), R.string.cannot_create_catalog, Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }

    private void setInitialDirectory() {
        Intent intent = requireActivity().getIntent();
        String requestedStartDir = intent.getStringExtra(EXTRA_START_DIRECTOTY);

        if (!TextUtils.isEmpty(requestedStartDir)) {
            File tempFile = new File(requestedStartDir);
            if (tempFile.isDirectory()) {
                path = tempFile;
            }
        }

        if (path == null) {
            if (Environment.getExternalStorageDirectory().isDirectory() && Environment.getExternalStorageDirectory().canRead()) {
                path = Environment.getExternalStorageDirectory();
            } else {
                path = new File("/");
            }
        }
    }

    @Override
    public void onClick(int position, FileItem item) {
        String chosenFile = item.file;
        File sel = new File(path + "/" + chosenFile);

        if (sel.isDirectory()) {
            if (sel.canRead()) {
                directoryScrollPositions.states.put(path.getAbsolutePath(), mLinearLayoutManager.onSaveInstanceState());

                pathDirsList.add(chosenFile);
                path = new File(sel.getAbsolutePath());
                loadFileList();
                mAdapter.notifyDataSetChanged();

                if (!fileList.isEmpty()) {
                    mLinearLayoutManager.scrollToPosition(0);
                }

                updateCurrentDirectoryTextView();
            } else {
                Toast.makeText(requireActivity(), R.string.path_not_exist, Toast.LENGTH_LONG).show();
            }
        } else {
            returnFileFinishActivity(sel.getAbsolutePath());
        }
    }

    @Override
    public boolean onBackPressed() {
        Logger.d("FileManager", "onBackPressed");

        if (path != null) {
            boolean root = path.toString().equalsIgnoreCase("/");
            if (!root) {
                loadDirectoryUp();
            }
            return root;
        }

        return true;
    }

    private static class DirectoryScrollPositions implements Parcelable {

        public static final Creator<DirectoryScrollPositions> CREATOR = new Creator<DirectoryScrollPositions>() {
            @Override
            public DirectoryScrollPositions createFromParcel(Parcel in) {
                return new DirectoryScrollPositions(in);
            }

            @Override
            public DirectoryScrollPositions[] newArray(int size) {
                return new DirectoryScrollPositions[size];
            }
        };
        private final Map<String, Parcelable> states;

        DirectoryScrollPositions() {
            states = new HashMap<>();
        }

        DirectoryScrollPositions(Parcel in) {
            int size = in.readInt();
            states = new HashMap<>(size);
            for (int i = 0; i < size; i++) {
                String key = in.readString();
                Parcelable value = in.readParcelable(LinearLayoutManager.SavedState.class.getClassLoader());
                states.put(key, value);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(states.size());

            for (Map.Entry<String, Parcelable> entry : states.entrySet()) {
                String key = entry.getKey();
                Parcelable value = entry.getValue();
                dest.writeString(key);
                dest.writeParcelable(value, flags);
            }
        }
    }

    private static class ItemModificationComparator implements Comparator<FileItem> {
        @Override
        public int compare(FileItem lhs, FileItem rhs) {
            return Long.compare(rhs.Modification, lhs.Modification);
        }
    }
}