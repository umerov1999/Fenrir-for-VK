package dev.ragnarok.fenrir.util.hls;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.io.ByteStreams;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.core.Single;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class M3U8 {

    private final String url;
    private final OutputStream output;

    public M3U8(@NonNull String url, @NonNull String filename) {
        try {
            this.url = url;
            File file = new File(filename).getAbsoluteFile();
            file.getParentFile().mkdirs();
            output = new FileOutputStream(file);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public M3U8(@NonNull String url) {
        this.url = url;
        output = null;
    }

    private static @Nullable
    InputStream getStream(@NonNull OkHttpClient client, URL url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                return null;
            }
            return Objects.requireNonNull(response.body()).byteStream();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static @Nullable
    Property checkProperty(String line) {
        Property property = parseLine(line);
        if (property.type.equals("FILE")) return property;
        if (property.type.equals("EXT-X-STREAM-INF") && property.properties != null) {
            // #EXT-X-STREAM-INF:BANDWIDTH=1280000,CODECS="...",AUDIO="aac"
            if (property.properties.get("BANDWIDTH") != null) {
                try {
                    Long.parseLong(property.properties.get("BANDWIDTH"));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return property;
        }
        if (property.type.equals("EXTINF") && property.values != null) {
            // #EXTINF:10.23,
            try {
                Double.parseDouble(property.values[0]);
            } catch (NumberFormatException e) {
                return null;
            }
            return property;
        }
        if (property.type.equals("EXT-X-KEY") && property.properties != null) {
            // #EXT-X-KEY:METHOD=AES-128,URI=\"(.*)\",IV=0[xX](.{32})
            boolean aes = "AES-128".equals(property.properties.get("METHOD"));
            if (!aes && !"NONE".equals(property.properties.get("METHOD"))) {
                return null;
            }
            if (!property.properties.containsKey("URI") && aes) {
                return null;
            }
            if (property.properties.containsKey("IV") && !property.properties.get("IV").matches("0[xX](.{32})")) {
                return null;
            }
            return property;
        }

        return null;
    }

    private static Property parseLine(String line) {
        Property ret = new Property();
        if (!line.startsWith("#")) {
            ret.type = "FILE";
            ret.value = line;
            return ret;
        }
        String[] types = line.split(":", 2);
        ret.type = types[0].substring(1);
        if (types.length == 1) {
            return ret;
        }
        ret.value = types[1];
        ret.values = splitProperty(types[1]);
        ret.properties = new HashMap<>();
        for (String p : ret.values) {
            if (p.length() > 0) {
                String[] ps = p.split("=", 2);
                if (ps.length == 1) {
                    ret.properties.put(p, null);
                } else {
                    String k = ps[0];
                    String v = ps[1];
                    if (v.charAt(0) == '"' && v.charAt(v.length() - 1) == '"') {
                        v = v.substring(1, v.length() - 1);
                    }
                    ret.properties.put(k, v);
                }
            }
        }
        return ret;
    }

    private static String[] splitProperty(String line) {
        ArrayList<String> list = new ArrayList<>();
        boolean escape = false;
        boolean quote = false;
        StringBuilder sb = new StringBuilder();

        for (char c : line.toCharArray()) {
            if (escape) {
                escape = false;
            } else if (quote) {
                if (c == '"') quote = false;
            } else {
                if (c == ',') {
                    list.add(sb.toString());
                    sb.delete(0, sb.length());
                    continue;
                }
                if (c == '\\') escape = true;
                if (c == '"') quote = true;
            }
            sb.append(c);
        }
        list.add(sb.toString());
        return list.toArray(new String[]{});
    }

    public Single<Long> getLength() {
        return Single.create(ss -> {
            long ret = 0L;
            OkHttpClient client = Utils.createOkHttp(60).build();
            try {
                URL mediaURL;
                URL m3u8Url = new URL(url);
                try (InputStream is = getStream(client, m3u8Url)) {
                    if (is == null) {
                        ss.onSuccess(0L);
                        return;
                    }
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        String line;
                        ArrayList<Map.Entry<Long, URL>> urls = new ArrayList<>();
                        long newurl = 0;
                        while ((line = br.readLine()) != null) {
                            line = line.trim();
                            Property property = checkProperty(line);
                            if (property == null) {
                                newurl = 0;
                            } else if (property.type.equals("EXT-X-STREAM-INF")) {
                                if (property.properties.get("BANDWIDTH") != null)
                                    newurl = Long.parseLong(property.properties.get("BANDWIDTH"));
                                else newurl = 1;
                            } else if (property.type.equals("FILE") && newurl > 0) {
                                urls.add(new AbstractMap.SimpleEntry<>(newurl, new URL(m3u8Url, line)));
                                newurl = 0;
                            } else {
                                newurl = 0;
                            }
                        }
                        Collections.sort(urls, (o1, o2) -> -Long.compare(o1.getKey(), o2.getKey()));
                        if (urls.size() > 0) {
                            m3u8Url = urls.get(0).getValue();
                        }
                    }
                }
                mediaURL = m3u8Url;
                ArrayList<TSDownload> list = new ArrayList<>();
                InputStream iis = getStream(client, mediaURL);
                if (iis == null) {
                    ss.onSuccess(0L);
                    return;
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(iis))) {
                    KeyType type = KeyType.NONE;
                    byte[] key = new byte[16];
                    byte[] iv = new byte[16];
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        Property property = checkProperty(line);
                        if (property == null) {
                            continue;
                        } else if (property.type.equals("FILE")) {
                            URL tsUrl = new URL(mediaURL, line);
                            list.add(new TSDownload(tsUrl, type, key, iv));
                            for (int i = iv.length; i > 0; i--) {
                                iv[i - 1] = (byte) (iv[i - 1] + 1);
                                if (iv[i - 1] != 0) break;
                            }
                        }
                    }
                }

                for (int i = 0; i < list.size(); i++) {
                    for (int j = i; j < list.size() && j < i + 1; j++) {
                        Response response = client.newCall(new Request.Builder()
                                .url(list.get(j).url)
                                .build()).execute();
                        if (response.isSuccessful()) {
                            String v = response.header("Content-Length");
                            response.body().close();
                            if (Utils.isEmpty(v)) {
                                ss.onSuccess(0L);
                                return;
                            }
                            ret += Long.parseLong(v);
                        } else {
                            ss.onSuccess(0L);
                            return;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                ss.onSuccess(0L);
                return;
            }
            long vtt = (ret / 188L);
            ss.onSuccess(ret - vtt * 4);
        });
    }

    public boolean run() {
        OkHttpClient client = Utils.createOkHttp(60).build();
        try {
            URL mediaURL;
            URL m3u8Url = new URL(url);
            try (InputStream is = getStream(client, m3u8Url)) {
                if (is == null) {
                    return false;
                }
                try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                    String line;
                    ArrayList<Map.Entry<Long, URL>> urls = new ArrayList<>();
                    long newurl = 0;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        Property property = checkProperty(line);
                        if (property == null) {
                            newurl = 0;
                        } else if (property.type.equals("EXT-X-STREAM-INF")) {
                            if (property.properties.get("BANDWIDTH") != null)
                                newurl = Long.parseLong(property.properties.get("BANDWIDTH"));
                            else newurl = 1;
                        } else if (property.type.equals("FILE") && newurl > 0) {
                            urls.add(new AbstractMap.SimpleEntry<>(newurl, new URL(m3u8Url, line)));
                            newurl = 0;
                        } else {
                            newurl = 0;
                        }
                    }
                    Collections.sort(urls, (o1, o2) -> -Long.compare(o1.getKey(), o2.getKey()));
                    if (urls.size() > 0) {
                        m3u8Url = urls.get(0).getValue();
                    }
                }
            }
            mediaURL = m3u8Url;
            ArrayList<TSDownload> list = new ArrayList<>();
            InputStream iis = getStream(client, mediaURL);
            if (iis == null) {
                return false;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(iis))) {
                KeyType type = KeyType.NONE;
                byte[] key = new byte[16];
                byte[] iv = new byte[16];
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    Property property = checkProperty(line);
                    if (property == null) {
                        continue;
                    } else if (property.type.equals("EXT-X-KEY")) {
                        if ("NONE".equals(property.properties.get("METHOD"))) {
                            type = KeyType.NONE;
                            continue;
                        }
                        type = KeyType.AES128;
                        URL keyUrl = new URL(mediaURL, property.properties.get("URI"));
                        InputStream ooo = getStream(client, keyUrl);
                        if (ooo == null) {
                            return false;
                        }
                        try (InputStream ks = ooo) {
                            int keyLen = ks.read(key);
                            if (keyLen != key.length) {
                                throw new RuntimeException("key error");
                            }
                        }
                        if (property.properties.get("IV") != null) {
                            String ivstr = property.properties.get("IV");
                            ivstr = ivstr.substring(2);
                            for (int i = 0; i < iv.length; i++) {
                                iv[i] = (byte) Integer.parseInt(ivstr.substring(i * 2, (i + 1) * 2), 16);
                            }
                        } else {
                            Arrays.fill(iv, (byte) 0);
                        }
                    } else if (property.type.equals("FILE")) {
                        URL tsUrl = new URL(mediaURL, line);
                        list.add(new TSDownload(tsUrl, type, key, iv));
                        for (int i = iv.length; i > 0; i--) {
                            iv[i - 1] = (byte) (iv[i - 1] + 1);
                            if (iv[i - 1] != 0) break;
                        }
                    }
                }
            }

            try (OutputStream fileStream = output) {
                for (int i = 0; i < list.size(); i++) {
                    for (int j = i; j < list.size() && j < i + 1; j++) {
                        if (list.get(j).start(client)) {
                            fileStream.write(list.get(j).getData());
                        } else {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private enum KeyType {
        NONE,
        AES128
    }

    private static class TSDownload {
        private static final int TS_PACKET_SIZE = 188;

        private final URL url;
        private final KeyType type;
        private final byte[] key;
        private final byte[] iv;
        private byte[] bytes;


        public TSDownload(URL url, KeyType type, byte[] key, byte[] iv) {
            this.url = url;
            this.type = type;
            this.key = key.clone();
            this.iv = iv.clone();
        }

        private static void closeQuietly(@Nullable Closeable closeable) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (IOException e) {
                // Ignore.
            }
        }

        private static Cipher getAesCp(byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            Key skey = new SecretKeySpec(key, "AES");
            IvParameterSpec param = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, skey, param);
            return cipher;
        }

        public URL getUrl() {
            return url;
        }

        public KeyType getType() {
            return type;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getIv() {
            return iv;
        }

        public boolean start(@NonNull OkHttpClient client) {
            try {
                InputStream iis = getStream(client, getUrl());
                if (iis == null) {
                    return false;
                }
                KeyType type = getType();
                byte[] bytes;
                if (type == KeyType.AES128) {
                    CipherInputStream stream = new CipherInputStream(iis, getAesCp(key, iv));
                    bytes = ByteStreams.toByteArray(stream);
                    closeQuietly(stream);
                } else if (type == KeyType.NONE) {
                    bytes = ByteStreams.toByteArray(iis);
                } else {
                    throw new IllegalStateException();
                }
                if (bytes.length % TS_PACKET_SIZE != 0) {
                    throw new IllegalStateException("MPEG2 TS Files that are not");
                }
                for (int i = 0; i < bytes.length; i = i + TS_PACKET_SIZE) {
                    if (bytes[i] != 0x47) {
                        throw new IllegalStateException("MPEG2 TS Files that are not");
                    }
                }

                this.bytes = bytes;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public byte[] getData() {
            return bytes;
        }
    }

    private static class Property {

        private String type;
        private Map<String, String> properties;
        private String[] values;
        private String value;

        @NonNull
        public String toString() {
            StringBuilder sb = new StringBuilder("Property type = ");
            sb.append(type);
            if (properties != null) {
                sb.append(", properties = { ");
                for (Map.Entry<String, String> i : properties.entrySet()) {
                    sb.append(i.getKey());
                    sb.append(" = ");
                    sb.append(i.getValue());
                    sb.append(", ");
                }
                sb.append("}");
            }
            if (values != null) {
                sb.append(", values = [ ");
                for (String i : values) {
                    sb.append(i);
                    sb.append(", ");
                }
                sb.append("]");
            }
            return sb.toString();
        }
    }

}
