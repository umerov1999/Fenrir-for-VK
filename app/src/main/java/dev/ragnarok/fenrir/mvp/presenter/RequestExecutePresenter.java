package dev.ragnarok.fenrir.mvp.presenter;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.join;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.api.Apis;
import dev.ragnarok.fenrir.api.interfaces.INetworker;
import dev.ragnarok.fenrir.mvp.presenter.base.AccountDependencyPresenter;
import dev.ragnarok.fenrir.mvp.view.IProgressView;
import dev.ragnarok.fenrir.mvp.view.IRequestExecuteView;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Pair;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;


public class RequestExecutePresenter extends AccountDependencyPresenter<IRequestExecuteView> {

    private final INetworker networker;
    private String body;
    private String method;
    private String fullResponseBody;
    private String trimmedResponseBody;
    private boolean loadingNow;

    public RequestExecutePresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        networker = Apis.get();
    }

    /**
     * Convert a JSON string to pretty print version
     */
    private static String toPrettyFormat(String jsonString) {
        JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    private void executeRequest() {
        String trimmedMethod = nonEmpty(method) ? method.trim() : null;
        String trimmedBody = nonEmpty(body) ? body.trim() : null;

        if (isEmpty(trimmedMethod)) {
            callView(v -> showError(v, new Exception("Method can't be empty")));
            return;
        }

        int accountId = getAccountId();

        Map<String, String> params = new HashMap<>();

        if (nonEmpty(trimmedBody)) {
            try {
                String[] lines = trimmedBody.split("\\r?\\n");

                for (String line : lines) {
                    String[] parts = line.split("=>");
                    String name = parts[0].toLowerCase().trim();
                    String value = parts[1].trim();
                    value = value.replaceAll("\"", "");

                    if ((name.equals("user_id") || name.equals("peer_id") || name.equals("owner_id")) && (value.equalsIgnoreCase("my") || value.equalsIgnoreCase("я")))
                        value = String.valueOf(accountId);
                    params.put(name, value);
                }
            } catch (Exception e) {
                callView(v -> showError(v, e));
                return;
            }
        }

        setLoadingNow(true);

        appendDisposable(executeSingle(accountId, trimmedMethod, params)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onRequestResponse, throwable -> onRequestError(Utils.getCauseIfRuntime(throwable))));
    }

    private boolean hasWritePermission() {
        return AppPerms.hasReadWriteStoragePermission(getApplicationContext());
    }

    private void saveToFile() {
        if (!hasWritePermission()) {
            callView(IRequestExecuteView::requestWriteExternalStoragePermission);
            return;
        }

        FileOutputStream out = null;

        try {
            String filename = DownloadWorkUtils.makeLegalFilename(method, "json");

            File file = new File(Environment.getExternalStorageDirectory(), filename);
            file.delete();

            byte[] bytes = fullResponseBody.getBytes(StandardCharsets.UTF_8);

            out = new FileOutputStream(file);
            out.write(bytes);
            out.flush();

            getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

            callView(v -> v.getCustomToast().showToast(R.string.saved_to_param_file_name, file.getAbsolutePath()));
        } catch (Exception e) {
            callView(v -> showError(v, e));
        } finally {
            Utils.safelyClose(out);
        }
    }

    @Override
    public void onGuiCreated(@NonNull IRequestExecuteView view) {
        super.onGuiCreated(view);
        view.displayBody(trimmedResponseBody);

        resolveProgressDialog();
    }

    private void onRequestResponse(Pair<String, String> body) {
        setLoadingNow(false);

        fullResponseBody = body.getFirst();
        trimmedResponseBody = body.getSecond();

        callView(view -> view.displayBody(trimmedResponseBody));
    }

    private void onRequestError(Throwable throwable) {
        setLoadingNow(false);
        callView(v -> showError(v, throwable));
    }

    private void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveProgressDialog();
    }

    private void resolveProgressDialog() {
        if (loadingNow) {
            callView(v -> v.displayProgressDialog(R.string.please_wait, R.string.waiting_for_response_message, false));
        } else {
            callView(IProgressView::dismissProgressDialog);
        }
    }

    private Single<Pair<String, String>> executeSingle(int accountId, String method, Map<String, String> params) {
        return networker.vkDefault(accountId)
                .other()
                .rawRequest(method, params)
                .map(optional -> {
                    String responseString = optional.get();

                    String fullJson = Objects.isNull(responseString) ? null : toPrettyFormat(responseString);

                    String trimmedJson = null;

                    if (nonEmpty(fullJson)) {
                        String[] lines = fullJson.split("\\r?\\n");

                        List<String> trimmed = new ArrayList<>();

                        for (String line : lines) {
                            if (trimmed.size() > 1500) {
                                trimmed.add("\n");
                                trimmed.add("... and more " + (lines.length - 1500) + " lines");
                                break;
                            }

                            trimmed.add(line);
                        }

                        trimmedJson = join("\n", trimmed);
                    }

                    return Pair.Companion.create(fullJson, trimmedJson);
                });
    }

    public void fireSaveClick() {
        saveToFile();
    }

    public void fireWritePermissionResolved() {
        if (hasWritePermission()) {
            saveToFile();
        }
    }

    public void fireExecuteClick() {
        callView(IRequestExecuteView::hideKeyboard);

        executeRequest();
    }

    public void fireMethodEdit(CharSequence s) {
        method = s.toString();
    }

    public void fireBodyEdit(CharSequence s) {
        body = s.toString();
    }

    public void fireCopyClick() {
        ClipboardManager clipboard = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("response", fullResponseBody);
        clipboard.setPrimaryClip(clip);

        callView(v -> v.getCustomToast().showToast(R.string.copied_to_clipboard));
    }
}