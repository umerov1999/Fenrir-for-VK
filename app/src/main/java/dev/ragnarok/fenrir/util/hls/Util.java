package dev.ragnarok.fenrir.util.hls;

/**
 * @author gamec
 */
public class Util {

    public static String filename(String str) {
        str = str.replace('\\', '-');
        str = str.replace('/', '-');
        str = str.replace('?', ' ');
        str = str.replace(':', ' ');
        str = str.replace('*', ' ');
        str = str.replace('"', ' ');
        str = str.replace('>', ' ');
        str = str.replace('<', ' ');
        str = str.replace('|', ' ');
        return str;
    }
}
