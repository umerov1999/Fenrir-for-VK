package dev.ragnarok.fenrir.util.existfile;

import static dev.ragnarok.fenrir.util.Objects.nonNull;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;

public class FileExistJVM implements AbsFileExist {
    private final List<String> CachedAudios = new LinkedList<>();
    private final List<String> RemoteAudios = new LinkedList<>();
    private final List<String> CachedPhotos = new LinkedList<>();
    private final Object isBusyLock = new Object();
    private boolean isBusy;

    private boolean setBusy(boolean nBusy) {
        synchronized (isBusyLock) {
            if (isBusy && nBusy) {
                return false;
            }
            isBusy = nBusy;
        }
        return true;
    }

    private void findRemoteAudios(Context context, boolean needLock) throws IOException {
        if (needLock) {
            if (!AppPerms.hasReadStoragePermissionSimple(context))
                return;
            if (!setBusy(true)) {
                return;
            }
        }
        RemoteAudios.clear();
        File audios = new File(Settings.get().other().getMusicDir(), "local_server_audio_list.json");
        if (!audios.exists()) {
            if (needLock) {
                setBusy(false);
            }
            return;
        }
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(audios), StandardCharsets.UTF_8));
        reader.beginArray();
        while (reader.hasNext()) {
            RemoteAudios.add(reader.nextString().toLowerCase(Locale.getDefault()));
        }
        if (needLock) {
            setBusy(false);
        }
    }

    @Override
    public void findRemoteAudios(Context context) throws IOException {
        findRemoteAudios(context, true);
    }

    private String transform_owner(int owner_id) {
        if (owner_id < 0)
            return "club" + Math.abs(owner_id);
        else
            return "id" + owner_id;
    }

    private boolean existPhoto(Photo photo) {
        for (String i : CachedPhotos) {
            if (i.contains(transform_owner(photo.getOwnerId()) + "_" + photo.getId())) {
                return true;
            }
        }
        return false;
    }

    private void loadDownloadPath(String Path) {
        File temp = new File(Path);
        if (!temp.exists())
            return;
        File[] file_list = temp.listFiles();
        if (file_list == null || file_list.length <= 0)
            return;
        for (File u : file_list) {
            if (u.isFile())
                CachedPhotos.add(u.getName());
            else if (u.isDirectory()) {
                loadDownloadPath(u.getAbsolutePath());
            }
        }
    }

    @Override
    public Completable findLocalImages(@Nullable List<SelectablePhotoWrapper> photos) {
        return Completable.create(t -> {
            if (!setBusy(true)) {
                return;
            }
            File temp = new File(Settings.get().other().getPhotoDir());
            if (!temp.exists()) {
                setBusy(false);
                t.onComplete();
                return;
            }
            File[] file_list = temp.listFiles();
            if (file_list == null || file_list.length <= 0) {
                setBusy(false);
                t.onComplete();
                return;
            }
            CachedPhotos.clear();
            for (File u : file_list) {
                if (u.isFile())
                    CachedPhotos.add(u.getName());
                else if (u.isDirectory()) {
                    loadDownloadPath(u.getAbsolutePath());
                }
            }
            if (nonNull(photos)) {
                for (SelectablePhotoWrapper i : photos) {
                    i.setDownloaded(existPhoto(i.getPhoto()));
                }
            }
            setBusy(false);
            t.onComplete();
        });
    }

    @Override
    public void addAudio(@NonNull String file) {
        if (!setBusy(true)) {
            return;
        }
        CachedAudios.add(file.toLowerCase(Locale.getDefault()));
        setBusy(false);
    }

    @Override
    public Completable findAllAudios(Context context) {
        if (!AppPerms.hasReadStoragePermissionSimple(context))
            return Completable.complete();
        return Completable.create(t -> {
            if (!setBusy(true)) {
                return;
            }
            findRemoteAudios(context, false);
            File temp = new File(Settings.get().other().getMusicDir());
            if (!temp.exists()) {
                setBusy(false);
                t.onComplete();
                return;
            }
            File[] file_list = temp.listFiles();
            if (file_list == null || file_list.length <= 0) {
                setBusy(false);
                t.onComplete();
                return;
            }
            CachedAudios.clear();
            for (File u : file_list) {
                if (u.isFile())
                    CachedAudios.add(u.getName().toLowerCase(Locale.getDefault()));
            }
            setBusy(false);
        });
    }

    @Override
    public void markExistPhotos(@NonNull List<SelectablePhotoWrapper> photos) {
        synchronized (isBusyLock) {
            if (isBusy) {
                return;
            }
            if (Utils.isEmpty(photos)) {
                return;
            }
            for (SelectablePhotoWrapper i : photos) {
                i.setDownloaded(existPhoto(i.getPhoto()));
            }
        }
    }

    @Override
    public boolean isExistRemoteAudio(@NonNull String file) {
        synchronized (isBusyLock) {
            if (isBusy) {
                return false;
            }
            String res = file.toLowerCase(Locale.getDefault());
            if (!Utils.isEmpty(RemoteAudios)) {
                for (String i : RemoteAudios) {
                    if (i.equals(res)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @Override
    public int isExistAllAudio(@NonNull String file) {
        synchronized (isBusyLock) {
            if (isBusy) {
                return 0;
            }
            String res = file.toLowerCase(Locale.getDefault());
            if (!Utils.isEmpty(CachedAudios)) {
                for (String i : CachedAudios) {
                    if (i.equals(res)) {
                        return 1;
                    }
                }
            }
            if (!Utils.isEmpty(RemoteAudios)) {
                for (String i : RemoteAudios) {
                    if (i.equals(res)) {
                        return 2;
                    }
                }
            }
            return 0;
        }
    }
}
