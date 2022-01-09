package dev.ragnarok.fenrir.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;

public class ActivityUtils {

    public static boolean isMimeVideo(String mime) {
        if (Utils.isEmpty(mime))
            return false;
        return mime.contains("video/");
    }

    public static boolean isMimeAudio(String mime) {
        if (Utils.isEmpty(mime))
            return false;
        return mime.contains("audio/");
    }

    public static StreamData checkLocalStreams(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return null;
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        String mime = intent.getType();
        if (extras == null || action == null || mime == null) {
            return null;
        }

        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                return new StreamData(intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM), mime);
            }
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    ArrayList<Uri> streams = new ArrayList<>(1);
                    streams.add(uri);
                    return new StreamData(streams, mime);
                }
            }
        }

        return null;
    }

    public static String checkLinks(Activity activity) {
        Intent intent = activity.getIntent();
        String link = null;
        if (intent == null) {
            return null;
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras == null || action == null) {
            return null;
        }

        if (Intent.ACTION_SEND.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_TEXT)) {
                link = intent.getStringExtra(Intent.EXTRA_TEXT);
            }
        }

        return link;
    }

    public static boolean checkInputExist(Activity activity) {
        Intent intent = activity.getIntent();
        if (intent == null) {
            return false;
        }

        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras == null || action == null) {
            return false;
        }

        if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            if (extras.containsKey(Intent.EXTRA_STREAM)) {
                return true;
            }
        }

        if (Intent.ACTION_SEND.equals(action)) {
            return extras.containsKey(Intent.EXTRA_STREAM) || extras.containsKey(Intent.EXTRA_TEXT);
        }

        return false;
    }

    public static void resetInputPhotos(Activity activity) {
        if (activity.getIntent() == null) {
            return;
        }

        activity.getIntent().removeExtra(Intent.EXTRA_STREAM);
    }

    public static void resetInputText(Activity activity) {
        if (activity.getIntent() == null) {
            return;
        }

        activity.getIntent().removeExtra(Intent.EXTRA_TEXT);
    }

    public static boolean safeHasInputAttachments(Activity activity) {
        return activity.getIntent() != null
                && activity.getIntent().getExtras() != null
                && activity.getIntent().getExtras().containsKey(MainActivity.EXTRA_INPUT_ATTACHMENTS);
    }

    @Nullable
    public static androidx.appcompat.app.ActionBar supportToolbarFor(Fragment fragment) {
        if (fragment.getActivity() == null) {
            return null;
        }

        return ((AppCompatActivity) fragment.requireActivity()).getSupportActionBar();
    }

    public static void setToolbarTitle(Fragment fragment, @StringRes int res) {
        ActionBar actionBar = supportToolbarFor(fragment);
        if (Objects.nonNull(actionBar)) {
            actionBar.setTitle(res);
        }
    }

    public static void setToolbarTitle(Fragment fragment, String title) {
        ActionBar actionBar = supportToolbarFor(fragment);
        if (Objects.nonNull(actionBar)) {
            actionBar.setTitle(title);
        }
    }

    public static void setToolbarSubtitle(Fragment fragment, @StringRes int res) {
        ActionBar actionBar = supportToolbarFor(fragment);
        if (Objects.nonNull(actionBar)) {
            actionBar.setSubtitle(res);
        }
    }

    public static void setToolbarSubtitle(Fragment fragment, String title) {
        ActionBar actionBar = supportToolbarFor(fragment);
        if (Objects.nonNull(actionBar)) {
            actionBar.setSubtitle(title);
        }
    }

    public static void hideSoftKeyboard(Activity activity) {
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    public static void hideSoftKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static class StreamData {
        public final ArrayList<Uri> uris;
        public final String mime;

        public StreamData(ArrayList<Uri> uris, String mime) {
            this.uris = uris;
            this.mime = mime;
        }
    }
}
