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
import java.util.List;
import java.util.Locale;

import dev.ragnarok.fenrir.model.Photo;
import dev.ragnarok.fenrir.model.wrappers.SelectablePhotoWrapper;
import dev.ragnarok.fenrir.module.StringExist;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Completable;

public class FileExistNative implements AbsFileExist {
    private final StringExist CachedAudios = new StringExist(true);
    private final StringExist RemoteAudios = new StringExist(true);
    private final StringExist CachedPhotos = new StringExist(true);

    private void findRemoteAudios(Context context, boolean requestPermission) throws IOException {
        if (requestPermission) {
            if (!AppPerms.hasReadStoragePermissionSimple(context))
                return;
        }
        RemoteAudios.clear();
        File audios = new File(Settings.get().other().getMusicDir(), "local_server_audio_list.json");
        if (!audios.exists()) {
            return;
        }
        JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(audios), StandardCharsets.UTF_8));
        reader.beginArray();
        while (reader.hasNext()) {
            RemoteAudios.insert(reader.nextString().toLowerCase(Locale.getDefault()));
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
        return CachedPhotos.contains(transform_owner(photo.getOwnerId()) + "_" + photo.getId());
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
                CachedPhotos.insert(u.getName());
            else if (u.isDirectory()) {
                loadDownloadPath(u.getAbsolutePath());
            }
        }
    }

    @Override
    public Completable findLocalImages(@Nullable List<SelectablePhotoWrapper> photos) {
        return Completable.create(t -> {
            File temp = new File(Settings.get().other().getPhotoDir());
            if (!temp.exists()) {
                t.onComplete();
                return;
            }
            File[] file_list = temp.listFiles();
            if (file_list == null || file_list.length <= 0) {
                t.onComplete();
                return;
            }
            CachedPhotos.clear();
            for (File u : file_list) {
                if (u.isFile())
                    CachedPhotos.insert(u.getName());
                else if (u.isDirectory()) {
                    loadDownloadPath(u.getAbsolutePath());
                }
            }
            if (nonNull(photos)) {
                for (SelectablePhotoWrapper i : photos) {
                    i.setDownloaded(existPhoto(i.getPhoto()));
                }
            }
            t.onComplete();
        });
    }

    @Override
    public void addAudio(@NonNull String file) {
        CachedAudios.insert(file.toLowerCase(Locale.getDefault()));
    }

    @Override
    public Completable findAllAudios(Context context) {
        if (!AppPerms.hasReadStoragePermissionSimple(context))
            return Completable.complete();
        return Completable.create(t -> {
            findRemoteAudios(context, false);
            File temp = new File(Settings.get().other().getMusicDir());
            if (!temp.exists()) {
                t.onComplete();
                return;
            }
            File[] file_list = temp.listFiles();
            if (file_list == null || file_list.length <= 0) {
                t.onComplete();
                return;
            }
            CachedAudios.clear();
            for (File u : file_list) {
                if (u.isFile())
                    CachedAudios.insert(u.getName().toLowerCase(Locale.getDefault()));
            }
        });
    }

    @Override
    public void markExistPhotos(@NonNull List<SelectablePhotoWrapper> photos) {
        if (Utils.isEmpty(photos)) {
            return;
        }
        for (SelectablePhotoWrapper i : photos) {
            i.setDownloaded(existPhoto(i.getPhoto()));
        }
    }

    @Override
    public boolean isExistRemoteAudio(@NonNull String file) {
        return RemoteAudios.has(file.toLowerCase(Locale.getDefault()));
    }

    @Override
    public int isExistAllAudio(@NonNull String file) {
        String res = file.toLowerCase(Locale.getDefault());
        if (CachedAudios.has(res)) {
            return 1;
        }
        if (RemoteAudios.has(res)) {
            return 2;
        }
        return 0;
    }
}
