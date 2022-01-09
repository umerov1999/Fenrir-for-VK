package ealvatag.logging;

import android.util.Log;

import com.ealvatag.BuildConfig;

public class EalvaTagLog {
    public static final String MARKER = "eAlvaTag";

    public static final class LogLevel {

        /**
         * Priority constant for the println method; use Log.v.
         */
        public static final int TRACE = 2;

        /**
         * Priority constant for the println method; use Log.d.
         */
        public static final int DEBUG = 3;

        /**
         * Priority constant for the println method; use Log.i.
         */
        public static final int INFO = 4;

        /**
         * Priority constant for the println method; use Log.w.
         */
        public static final int WARN = 5;

        /**
         * Priority constant for the println method; use Log.e.
         */
        public static final int ERROR = 6;
    }

    public static class JLogger {
        private final String tag;

        public JLogger(Class<?> c, String tag) {
            this.tag = tag + ": " + c.getSimpleName();
        }

        public boolean isLoggable(int type, String tag) {
            if (type == LogLevel.DEBUG) {
                return BuildConfig.DEBUG;
            }
            return true;
        }

        public void log(int level, String format, Object... formatArgs) {
            Log.println(level, tag, String.format(format, formatArgs));
        }

        public void log(int level, Exception exception, String format, Object... formatArgs) {
            Log.println(level, tag, String.format(format, formatArgs) + " exception: " + exception.getLocalizedMessage());
        }
    }

    public static class JLoggers {
        public static JLogger get(Class<?> c, String tag) {
            return new JLogger(c, tag);
        }
    }
}
