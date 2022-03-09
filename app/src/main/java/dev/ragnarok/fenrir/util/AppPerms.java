package dev.ragnarok.fenrir.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;

import dev.ragnarok.fenrir.R;

public class AppPerms {
    public static boolean hasReadWriteStoragePermission(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        if (Utils.hasScopedStorage()) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
                return false;
            }
            return true;
        }
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int hasReadPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED && hasReadPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadStoragePermission(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        if (Utils.hasScopedStorage()) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                intent.setData(uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
                return false;
            }
            return true;
        }
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasReadStoragePermissionSimple(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        if (Utils.hasScopedStorage()) {
            return Environment.isExternalStorageManager();
        }
        int hasWritePermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        return hasWritePermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasCameraPermission(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        int hasCameraPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA);
        return hasReadWriteStoragePermission(context) && hasCameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasContactsPermission(@NonNull Context context) {
        if (!Utils.hasMarshmallow()) return true;
        return PermissionChecker.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PermissionChecker.PERMISSION_GRANTED;
    }

    public static doRequestPermissions requestPermissions(@NonNull Fragment fragment, @NonNull String[] permissions, @NonNull onPermissionsGranted callback) {
        ActivityResultLauncher<String[]> request = fragment.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (Utils.checkValues(result.values())) {
                callback.granted();
            } else {
                Utils.showRedTopToast(fragment.requireActivity(), R.string.not_permitted);
            }
        });
        return () -> request.launch(permissions);
    }

    public static doRequestPermissions requestPermissionsActivity(@NonNull AppCompatActivity activity, @NonNull String[] permissions, @NonNull onPermissionsGranted callback) {
        ActivityResultLauncher<String[]> request = activity.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (Utils.checkValues(result.values())) {
                callback.granted();
            } else {
                Utils.showRedTopToast(activity, R.string.not_permitted);
            }
        });
        return () -> request.launch(permissions);
    }

    public static doRequestPermissions requestPermissionsResult(@NonNull Fragment fragment, @NonNull String[] permissions, @NonNull onPermissionsResult callback) {
        ActivityResultLauncher<String[]> request = fragment.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            if (Utils.checkValues(result.values())) {
                callback.granted();
            } else {
                Utils.showRedTopToast(fragment.requireActivity(), R.string.not_permitted);
                callback.not_granted();
            }
        });
        return () -> request.launch(permissions);
    }

    public interface doRequestPermissions {
        void launch();
    }

    public interface onPermissionsGranted {
        void granted();
    }

    public interface onPermissionsResult {
        void granted();

        void not_granted();
    }
}
