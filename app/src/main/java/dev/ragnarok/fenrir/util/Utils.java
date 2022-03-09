package dev.ragnarok.fenrir.util;

import static dev.ragnarok.fenrir.util.Objects.isNull;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.qrcode.CustomQRCodeWriter;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ragnarok.fenrir.AccountType;
import dev.ragnarok.fenrir.App;
import dev.ragnarok.fenrir.BuildConfig;
import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.Includes;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.activity.MainActivity;
import dev.ragnarok.fenrir.activity.SwipebleActivity;
import dev.ragnarok.fenrir.api.ProxyUtil;
import dev.ragnarok.fenrir.api.model.Identificable;
import dev.ragnarok.fenrir.link.internal.LinkActionAdapter;
import dev.ragnarok.fenrir.link.internal.OwnerLinkSpanFactory;
import dev.ragnarok.fenrir.media.exo.OkHttpDataSource;
import dev.ragnarok.fenrir.model.ISelectable;
import dev.ragnarok.fenrir.model.ISomeones;
import dev.ragnarok.fenrir.model.Lang;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.ProxyConfig;
import dev.ragnarok.fenrir.model.Sticker;
import dev.ragnarok.fenrir.module.rlottie.RLottieDrawable;
import dev.ragnarok.fenrir.place.Place;
import dev.ragnarok.fenrir.place.PlaceFactory;
import dev.ragnarok.fenrir.service.ErrorLocalizer;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.view.emoji.EmojiconTextView;
import dev.ragnarok.fenrir.view.natives.rlottie.RLottieImageView;
import dev.ragnarok.fenrir.view.pager.BackgroundToForegroundTransformer;
import dev.ragnarok.fenrir.view.pager.ClockSpinTransformer;
import dev.ragnarok.fenrir.view.pager.CubeInDepthTransformer;
import dev.ragnarok.fenrir.view.pager.DepthTransformer;
import dev.ragnarok.fenrir.view.pager.FanTransformer;
import dev.ragnarok.fenrir.view.pager.GateTransformer;
import dev.ragnarok.fenrir.view.pager.SliderTransformer;
import dev.ragnarok.fenrir.view.pager.Transformers_Types;
import dev.ragnarok.fenrir.view.pager.ZoomOutTransformer;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Utils {
    private static final List<Integer> reload_news = new LinkedList<>();
    private static final List<Integer> reload_dialogs = new LinkedList<>();
    private static final List<Integer> reload_stickers = new LinkedList<>();
    private static final List<Sticker.LocalSticker> CachedMyStickers = new ArrayList<>();
    private static final Point displaySize = new Point();
    private static String device_id;
    private static float density = 1;
    private static int screenRefreshRate = 60;
    private static DisplayMetrics metrics;
    private static boolean compressTraffic;

    public static List<Sticker.LocalSticker> getCachedMyStickers() {
        return CachedMyStickers;
    }

    public static boolean isCompressTraffic() {
        return compressTraffic;
    }

    public static void setCompressTraffic(boolean enable) {
        compressTraffic = enable;
    }

    public static int getScreenRefreshRate() {
        return screenRefreshRate;
    }

    public static Point getDisplaySize() {
        return displaySize;
    }

    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (App.getApplicationHandler() == null) {
            return;
        }
        if (delay == 0) {
            App.getApplicationHandler().post(runnable);
        } else {
            App.getApplicationHandler().postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        if (App.getApplicationHandler() == null) {
            return;
        }
        App.getApplicationHandler().removeCallbacks(runnable);
    }


    public static boolean needReloadNews(int account_id) {
        if (!reload_news.contains(account_id)) {
            reload_news.add(account_id);
            return true;
        }
        return false;
    }

    public static boolean needReloadDialogs(int account_id) {
        if (!reload_dialogs.contains(account_id)) {
            reload_dialogs.add(account_id);
            return true;
        }
        return false;
    }

    public static boolean needReloadStickers(int account_id) {
        if (!reload_stickers.contains(account_id)) {
            reload_stickers.add(account_id);
            return true;
        }
        return false;
    }

    public static DisplayMetrics getDisplayMetrics() {
        return metrics;
    }

    public static <T> T lastOf(@NonNull List<T> data) {
        return data.get(data.size() - 1);
    }

    public static String stringEmptyIfNull(String orig) {
        return orig == null ? "" : orig;
    }

    public static <T> List<T> listEmptyIfNull(List<T> orig) {
        return orig == null ? Collections.emptyList() : orig;
    }

    public static <T> ArrayList<T> singletonArrayList(T data) {
        ArrayList<T> list = new ArrayList<>(1);
        list.add(data);
        return list;
    }

    public static <T> int findIndexByPredicate(List<T> data, Predicate<T> predicate) {
        for (int i = 0; i < data.size(); i++) {
            if (predicate.test(data.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public static <T> Pair<Integer, T> findInfoByPredicate(List<T> data, Predicate<T> predicate) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (predicate.test(t)) {
                return Pair.Companion.create(i, t);
            }
        }

        return null;
    }

    public static <T extends Identificable> Pair<Integer, T> findInfoById(List<T> data, int id) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (t.getId() == id) {
                return Pair.Companion.create(i, t);
            }
        }

        return null;
    }

    public static <T extends Identificable> List<Integer> collectIds(Collection<T> data, Predicate<T> predicate) {
        int count = countOf(data, predicate);
        if (count == 0) {
            return Collections.emptyList();
        }

        List<Integer> ids = new ArrayList<>(count);
        for (T t : data) {
            if (predicate.test(t)) {
                ids.add(t.getId());
            }
        }

        return ids;
    }

    public static <T extends Identificable> int countOf(Collection<T> data, Predicate<T> predicate) {
        int count = 0;
        for (T t : data) {
            if (predicate.test(t)) {
                count++;
            }
        }

        return count;
    }

    public static boolean nonEmpty(Collection<?> data) {
        return data != null && !data.isEmpty();
    }

    public static Throwable getCauseIfRuntime(Throwable throwable) {
        Throwable target = throwable;
        while (target instanceof RuntimeException) {
            if (isNull(target.getCause())) {
                break;
            }

            target = target.getCause();
        }

        return target;
    }

    public static <T> ArrayList<T> cloneListAsArrayList(List<T> original) {
        if (original == null) {
            return null;
        }

        ArrayList<T> clone = new ArrayList<>(original.size());
        clone.addAll(original);
        return clone;
    }

    public static int countOfPositive(Collection<Integer> values) {
        int count = 0;
        for (Integer value : values) {
            if (value > 0) {
                count++;
            }
        }

        return count;
    }

    public static int countOfNegative(Collection<Integer> values) {
        int count = 0;
        for (Integer value : values) {
            if (value < 0) {
                count++;
            }
        }

        return count;
    }

    public static void trimListToSize(List<?> data, int maxsize) {
        if (data.size() > maxsize) {
            data.remove(data.size() - 1);
            trimListToSize(data, maxsize);
        }
    }

    public static <T> ArrayList<T> copyToArrayListWithPredicate(List<T> orig, Predicate<T> predicate) {
        ArrayList<T> data = new ArrayList<>(orig.size());
        for (T t : orig) {
            if (predicate.test(t)) {
                data.add(t);
            }
        }

        return data;
    }

    public static <T> List<T> copyListWithPredicate(List<T> orig, Predicate<T> predicate) {
        List<T> data = new ArrayList<>(orig.size());
        for (T t : orig) {
            if (predicate.test(t)) {
                data.add(t);
            }
        }

        return data;
    }

    public static boolean isEmpty(CharSequence body) {
        return body == null || body.length() == 0;
    }

    public static boolean nonEmpty(CharSequence text) {
        return text != null && text.length() > 0;
    }

    public static boolean isEmpty(Collection<?> data) {
        return data == null || data.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> data) {
        return data == null || data.size() == 0;
    }

    public static <T> String join(T[] tokens, String delimiter, SimpleFunction<T, String> function) {
        if (isNull(tokens)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (T token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(function.apply(token));
        }

        return sb.toString();
    }

    public static String joinNonEmptyStrings(String delimiter, @NonNull String... tokens) {
        List<String> nonEmpty = new ArrayList<>();
        for (String token : tokens) {
            if (nonEmpty(token)) {
                nonEmpty.add(token);
            }
        }

        return join(nonEmpty, delimiter, orig -> orig);
    }

    public static <T> String join(Iterable<T> tokens, String delimiter, SimpleFunction<T, String> function) {
        if (isNull(tokens)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (T token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(function.apply(token));
        }

        return sb.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array objects to be joined. Strings will be formed from
     *               the objects by calling object.toString().
     */
    public static String join(CharSequence delimiter, Iterable<?> tokens) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> it = tokens.iterator();
        if (it.hasNext()) {
            sb.append(it.next());
            while (it.hasNext()) {
                sb.append(delimiter);
                sb.append(it.next());
            }
        }
        return sb.toString();
    }

    /**
     * Returns a string containing the tokens joined by delimiters.
     *
     * @param tokens an array strings to be joined
     */
    public static String stringJoin(CharSequence delimiter, String... tokens) {
        StringBuilder sb = new StringBuilder();

        boolean firstTime = true;
        for (String token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(token);
        }

        return sb.toString();
    }

    public static boolean safeIsEmpty(int[] mids) {
        return mids == null || mids.length == 0;
    }

    public static int safeLenghtOf(CharSequence text) {
        return isNull(text) ? 0 : text.length();
    }

    public static <T> int indexOf(@NonNull List<T> data, Predicate<T> predicate) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (predicate.test(t)) {
                return i;
            }
        }

        return -1;
    }

    public static <T> boolean removeIf(@NonNull Collection<T> data, @NonNull Predicate<T> predicate) {
        boolean hasChanges = false;
        Iterator<T> iterator = data.iterator();
        while (iterator.hasNext()) {
            if (predicate.test(iterator.next())) {
                iterator.remove();
                hasChanges = true;
            }
        }

        return hasChanges;
    }

    public static void safelyDispose(Disposable disposable) {
        if (Objects.nonNull(disposable) && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public static void safelyCloseCursor(Cursor cursor) {
        if (Objects.nonNull(cursor)) {
            cursor.close();
        }
    }

    public static void safelyRecycle(Bitmap bitmap) {
        if (Objects.nonNull(bitmap)) {
            try {
                if (!bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (Exception ignored) {

            }
        }
    }

    public static void safelyClose(Closeable closeable) {
        if (Objects.nonNull(closeable)) {
            try {
                closeable.close();
            } catch (IOException ignored) {

            }
        }
    }

    public static void showRedTopToast(@NonNull Context activity, String text) {
        View view = View.inflate(activity, R.layout.toast_error, null);
        ((TextView) view.findViewById(R.id.text)).setText(text);

        Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 15);
        toast.show();
    }

    public static void showRedTopToast(@NonNull Context activity, @StringRes int text, Object... params) {
        View view = View.inflate(activity, R.layout.toast_error, null);
        ((TextView) view.findViewById(R.id.text)).setText(activity.getString(text, params));

        Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 15);
        toast.show();
    }

    public static void showYellowTopToast(@NonNull Context activity, String text) {
        View view = View.inflate(activity, R.layout.toast_warrning, null);
        ((TextView) view.findViewById(R.id.text)).setText(text);

        Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 15);
        toast.show();
    }

    public static void showYellowTopToast(@NonNull Context activity, @StringRes int text, Object... params) {
        View view = View.inflate(activity, R.layout.toast_warrning, null);
        ((TextView) view.findViewById(R.id.text)).setText(activity.getString(text, params));

        Toast toast = Toast.makeText(activity, text, Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 15);
        toast.show();
    }

    public static int safeCountOf(SparseArray<?> sparseArray) {
        return sparseArray == null ? 0 : sparseArray.size();
    }

    public static int safeCountOf(Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

    public static int safeCountOf(Cursor cursor) {
        return cursor == null ? 0 : cursor.getCount();
    }

    public static long startOfTodayMillis() {
        return startOfToday().getTimeInMillis();
    }

    public static Calendar startOfToday() {
        Calendar current = Calendar.getInstance();
        current.set(current.get(Calendar.YEAR), current.get(Calendar.MONTH), current.get(Calendar.DATE), 0, 0, 0);
        return current;
    }

    @NonNull
    public static List<Integer> idsListOf(@NonNull Collection<? extends Identificable> data) {
        List<Integer> ids = new ArrayList<>(data.size());
        for (Identificable identifiable : data) {
            ids.add(identifiable.getId());
        }

        return ids;
    }

    @Nullable
    public static <T extends Identificable> T findById(@NonNull Collection<T> data, int id) {
        for (T element : data) {
            if (element.getId() == id) {
                return element;
            }
        }

        return null;
    }

    public static <T extends Identificable> int findIndexById(@NonNull List<T> data, int id) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public static <T extends ISomeones> int findIndexById(@NonNull List<T> data, int id, int ownerId) {
        for (int i = 0; i < data.size(); i++) {
            T t = data.get(i);
            if (t.getId() == id && t.getOwnerId() == ownerId) {
                return i;
            }
        }

        return -1;
    }

    @NonNull
    public static <T extends ISelectable> ArrayList<T> getSelected(@NonNull List<T> fullData) {
        return getSelected(fullData, false);
    }

    @NonNull
    public static <T extends ISelectable> ArrayList<T> getSelected(@NonNull List<T> fullData, boolean reverse) {
        ArrayList<T> result = new ArrayList<>();

        if (reverse) {
            for (int i = fullData.size() - 1; i >= 0; i--) {
                T m = fullData.get(i);
                if (m.isSelected()) {
                    result.add(m);
                }
            }
        } else {
            for (T item : fullData) {
                if (item.isSelected()) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    public static int countOfSelection(List<? extends ISelectable> data) {
        int count = 0;
        for (ISelectable selectable : data) {
            if (selectable.isSelected()) {
                count++;
            }
        }

        return count;
    }

    public static boolean hasFlag(int mask, int flag) {
        return (mask & flag) != 0;
    }

    public static int removeFlag(int mask, int flag) {
        mask &= ~flag;
        return mask;
    }

    public static int addFlagIf(int mask, int flag, boolean ifTrue) {
        if (ifTrue) {
            return mask + flag;
        }

        return mask;
    }

    /**
     * Проверка, содержит ли маска флаги
     *
     * @param mask  маска
     * @param flags флаги
     * @return если содержит - true
     */
    public static boolean hasFlags(int mask, int... flags) {
        for (int flag : flags) {
            if (!hasFlag(mask, flag)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Проверка, содержит ли маска какой нибудь из флагов
     *
     * @param mask  маска
     * @param flags флаги
     * @return если содержит - true
     */
    public static boolean hasSomeFlag(int mask, int... flags) {
        for (int flag : flags) {
            if (hasFlag(mask, flag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>Adds an object to the list. The object will be inserted in the correct
     * place so that the objects in the list are sorted. When the list already
     * contains objects that are equal according to the comparator, the new
     * object will be inserted immediately after these other objects.</p>
     *
     * @param o the object to be added
     */
    public static <T> int addElementToList(T o, List<T> data, Comparator<T> comparator) {
        int i = 0;
        boolean found = false;
        while (!found && (i < data.size())) {
            found = comparator.compare(o, data.get(i)) < 0;
            if (!found) {
                i++;
            }
        }

        data.add(i, o);
        return i;
    }

    public static boolean hasMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean hasScopedStorage() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && BuildConfig.MANAGE_SCOPED_STORAGE;
    }

    public static boolean hasNougat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N;
    }

    public static boolean hasOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    public static boolean hasPie() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
    }

    public static int indexOf(List<? extends Identificable> data, int id) {
        if (data == null) {
            return -1;
        }

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public static int indexOfOwner(List<? extends Owner> data, Owner in) {
        if (data == null || in == null) {
            return -1;
        }

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getOwnerId() == in.getOwnerId()) {
                return i;
            }
        }

        return -1;
    }

    public static int indexOfOwner(List<? extends Owner> data, int id) {
        if (data == null) {
            return -1;
        }

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getOwnerId() == id) {
                return i;
            }
        }

        return -1;
    }

    public static boolean safeIsEmpty(CharSequence text) {
        return isNull(text) || text.length() == 0;
    }

    public static boolean safeTrimmedIsEmpty(String value) {
        return value == null || TextUtils.getTrimmedLength(value) == 0;
    }

    public static String firstNonEmptyString(String... array) {
        for (String s : array) {
            if (!TextUtils.isEmpty(s)) {
                return s;
            }
        }

        return null;
    }

    @SafeVarargs
    public static <T> T firstNonNull(T... items) {
        for (T t : items) {
            if (t != null) {
                return t;
            }
        }

        return null;
    }

    /**
     * Округление числа
     *
     * @param value  число
     * @param digits количество знаков после запятой
     * @return округленное число
     */
    public static BigDecimal roundUp(double value, int digits) {
        return new BigDecimal("" + value).setScale(digits, BigDecimal.ROUND_HALF_UP);
    }

    public static <T> ArrayList<T> createSingleElementList(T element) {
        ArrayList<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }

    public static boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    public static boolean trimmedIsEmpty(String text) {
        return text == null || text.trim().length() == 0;
    }

    public static boolean trimmedNonEmpty(String text) {
        return text != null && text.trim().length() > 0;
    }

    public static boolean is600dp(Context context) {
        return context.getResources().getBoolean(R.bool.is_tablet);
    }

    public static boolean safeIsEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean safeIsEmpty(SparseArray<?> array) {
        return array == null || array.size() == 0;
    }

    public static boolean safeAllIsEmpty(Collection<?>... collections) {
        for (Collection<?> collection : collections) {
            if (!safeIsEmpty(collection)) {
                return false;
            }
        }

        return true;
    }

    public static boolean intValueNotIn(int value, int... variants) {
        for (int variant : variants) {
            if (value == variant) {
                return false;
            }
        }

        return true;
    }

    public static boolean intValueIn(int value, int... variants) {
        for (int variant : variants) {
            if (value == variant) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasOneElement(Collection<?> collection) {
        return safeCountOf(collection) == 1;
    }

    public static int safeCountOf(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    public static int safeCountOfMultiple(Collection<?>... collections) {
        if (collections == null) {
            return 0;
        }

        int count = 0;
        for (Collection<?> collection : collections) {
            count = count + safeCountOf(collection);
        }

        return count;
    }

    public static float getActionBarHeight(Activity activity) {
        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, activity.getResources().getDisplayMetrics());
        }
        return 0;
    }

    /**
     * Добавляет прозрачность к цвету
     *
     * @param color  цвет
     * @param factor степень прозрачности
     * @return прозрачный цвет
     */
    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static boolean isHiddenType(@AccountType int type) {
        return type == AccountType.VK_ANDROID_HIDDEN || type == AccountType.KATE_HIDDEN;
    }

    public static boolean isKateType(@AccountType int type) {
        return type == AccountType.KATE || type == AccountType.KATE_HIDDEN;
    }

    public static boolean isHiddenCurrent() {
        return isHiddenType(Settings.get().accounts().getType(Settings.get().accounts().getCurrent()));
    }

    public static boolean isHiddenAccount(int account_id) {
        return isHiddenType(Settings.get().accounts().getType(account_id));
    }

    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        if (isEmpty(device_id)) {
            device_id = android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            if (isEmpty(device_id))
                device_id = "0123456789A";
        }
        return device_id;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static String getAppVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static float dpToPx(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float spToPx(float dp, Context context) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     * <p>
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     * <p>
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     *
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    public static void shareLink(Activity activity, String link, String subject) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, link);
        activity.startActivity(Intent.createChooser(sharingIntent, activity.getResources().getString(R.string.share_using)));
    }

    public static void setTint(@Nullable ImageView view, @ColorInt int color) {
        if (isNull(view)) {
            return;
        }
        view.setImageTintList(ColorStateList.valueOf(color));
    }

    public static void setBackgroundTint(@Nullable ImageView view, @ColorInt int color) {
        if (isNull(view)) {
            return;
        }
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public static void setBackgroundTint(@Nullable View view, @ColorInt int color) {
        if (isNull(view)) {
            return;
        }
        view.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    public static void setColorFilter(@Nullable Drawable drawable, @ColorInt int color) {
        if (isNull(drawable)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.setColorFilter(new BlendModeColorFilter(color, BlendMode.MODULATE));
        } else {
            drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
    }

    public static void setColorFilter(@Nullable ImageView view, @ColorInt int color) {
        if (isNull(view)) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            view.setColorFilter(new BlendModeColorFilter(color, BlendMode.MODULATE));
        } else {
            view.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        }
    }

    @StringRes
    public static int declOfNum(int number_z, @StringRes int[] titles) {
        int number = Math.abs(number_z);
        int[] cases = {2, 0, 1, 1, 1, 2};
        return titles[(number % 100 > 4 && number % 100 < 20) ? 2 : cases[Math.min(number % 10, 5)]];
    }

    @StringRes
    public static int declOfNum(long number_z, @StringRes int[] titles) {
        long number = Math.abs(number_z);
        int[] cases = {2, 0, 1, 1, 1, 2};
        return titles[(number % 100 > 4 && number % 100 < 20) ? 2 : cases[(int) Math.min(number % 10, 5)]];
    }

    public static void doWavesLottie(RLottieImageView visual, boolean Play) {
        visual.clearAnimationDrawable();
        if (Play) {
            visual.setAutoRepeat(true);
            visual.fromRes(R.raw.waves, dp(28), dp(28));
        } else {
            visual.setAutoRepeat(false);
            visual.fromRes(R.raw.waves_end, dp(28), dp(28));
        }
        visual.playAnimation();
    }

    public static Bitmap createGradientChatImage(int width, int height, int owner_id) {
        int pp = owner_id % 10;
        String Color1 = "#D81B60";
        String Color2 = "#F48FB1";
        switch (pp) {
            case 0:
                Color1 = "#FF0061";
                Color2 = "#FF4200";
                break;
            case 1:
                Color1 = "#00ABD6";
                Color2 = "#8700D6";
                break;
            case 2:
                Color1 = "#FF7900";
                Color2 = "#FF9500";
                break;
            case 3:
                Color1 = "#55D600";
                Color2 = "#00D67A";
                break;
            case 4:
                Color1 = "#9400D6";
                Color2 = "#D6008E";
                break;
            case 5:
                Color1 = "#cd8fff";
                Color2 = "#9100ff";
                break;
            case 6:
                Color1 = "#ff7f69";
                Color2 = "#fe0bdb";
                break;
            case 7:
                Color1 = "#FE790B";
                Color2 = "#0BFEAB";
                break;
            case 8:
                Color1 = "#9D0BFE";
                Color2 = "#0BFEAB";
                break;
            case 9:
                Color1 = "#9D0BFE";
                Color2 = "#FEDF0B";
                break;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        LinearGradient gradient = new LinearGradient(0, 0, width, height, Color.parseColor(Color1), Color.parseColor(Color2), Shader.TileMode.CLAMP);
        Canvas canvas = new Canvas(bitmap);
        Paint paint2 = new Paint();
        paint2.setShader(gradient);
        canvas.drawRect(0F, 0F, (float) width, (float) height, paint2);
        return bitmap;
    }

    public static @NonNull
    OkHttpDataSource.Factory getExoPlayerFactory(String userAgent, ProxyConfig proxyConfig) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS);
        ProxyUtil.applyProxyConfig(builder, proxyConfig);
        return new OkHttpDataSource.Factory((Call.Factory) builder.build()).setUserAgent(userAgent);
    }

    public static boolean isColorDark(int color) {
        return ColorUtils.calculateLuminance(color) < 0.5;
    }

    public static Animator getAnimator(View view) {
        return ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
    }

    public static <K extends Parcelable, V extends Parcelable> void writeParcelableMap(
            Parcel parcel, int flags, Map<K, V> map) {
        if (isEmpty(map)) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(map.size());
        for (Map.Entry<K, V> e : map.entrySet()) {
            parcel.writeParcelable(e.getKey(), flags);
            parcel.writeParcelable(e.getValue(), flags);
        }
    }

    public static <K extends Parcelable, V extends Parcelable> Map<K, V> readParcelableMap(
            Parcel parcel, ClassLoader keyClassLoader, ClassLoader valueClassLoader) {
        int size = parcel.readInt();
        if (size == 0)
            return null;
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(parcel.readParcelable(keyClassLoader),
                    parcel.readParcelable(valueClassLoader));
        }
        return map;
    }

    public static <K extends Parcelable> void writeParcelableArray(
            Parcel parcel, int flags, List<K> list) {
        if (isEmpty(list)) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(list.size());
        for (K e : list) {
            parcel.writeParcelable(e, flags);
        }
    }

    public static <K extends Parcelable> List<K> readParcelableArray(
            Parcel parcel, ClassLoader classLoader) {
        int size = parcel.readInt();
        if (size == 0)
            return null;
        List<K> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(parcel.readParcelable(classLoader));
        }
        return list;
    }

    public static void writeStringMap(Parcel parcel, Map<String, String> map) {
        if (isEmpty(map)) {
            parcel.writeInt(0);
            return;
        }
        parcel.writeInt(map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            parcel.writeString(e.getKey());
            parcel.writeString(e.getValue());
        }
    }

    public static Map<String, String> readStringMap(Parcel parcel) {
        int size = parcel.readInt();
        if (size == 0)
            return null;
        Map<String, String> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(parcel.readString(), parcel.readString());
        }
        return map;
    }

    public static String[][] getArrayFromHash(Map<String, String> data) {
        String[][] str;

        Object[] keys = data.keySet().toArray();
        Object[] values = data.values().toArray();
        str = new String[2][values.length];
        for (int i = 0; i < keys.length; i++) {
            str[0][i] = (String) keys[i];
            str[1][i] = (String) values[i];
        }
        return str;
    }

    @NonNull
    public static Snackbar ThemedSnack(@NonNull View view, @StringRes int resId, @BaseTransientBottomBar.Duration int duration) {
        return ThemedSnack(view, view.getResources().getText(resId), duration);
    }

    @NonNull
    public static Snackbar ThemedSnack(@NonNull View view, @NonNull CharSequence text, @BaseTransientBottomBar.Duration int duration) {
        int color = CurrentTheme.getColorPrimary(view.getContext());
        int text_color = isColorDark(color)
                ? Color.parseColor("#ffffff") : Color.parseColor("#000000");

        return Snackbar.make(view, text, duration).setBackgroundTint(color).setActionTextColor(text_color).setTextColor(text_color);
    }

    @NonNull
    public static Snackbar ColoredSnack(@NonNull View view, @StringRes int resId, @BaseTransientBottomBar.Duration int duration, @ColorInt int color) {
        return ColoredSnack(view, view.getResources().getText(resId), duration, color);
    }

    @NonNull
    public static Snackbar ColoredSnack(@NonNull View view, @NonNull CharSequence text, @BaseTransientBottomBar.Duration int duration, @ColorInt int color) {
        int text_color = isColorDark(color)
                ? Color.parseColor("#ffffff") : Color.parseColor("#000000");

        return Snackbar.make(view, text, duration).setBackgroundTint(color).setActionTextColor(text_color).setTextColor(text_color);
    }

    public static int getVerifiedColor(Context context, boolean verified) {
        return !verified ? CurrentTheme.getPrimaryTextColorCode(context) : Color.parseColor("#009900");
    }

    public static float getDensity() {
        return density;
    }

    public static int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(density * value);
    }

    public static int dpr(float value) {
        if (value == 0) {
            return 0;
        }
        return Math.round(density * value);
    }

    public static int dp2(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.floor(density * value);
    }

    public static float dpf2(float value) {
        if (value == 0) {
            return 0;
        }
        return density * value;
    }

    public static void prepareDensity(Context context) {
        metrics = context.getResources().getDisplayMetrics();
        density = metrics.density;
        Display display = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display = context.getDisplay();
        } else {
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                display = manager.getDefaultDisplay();
            }
        }
        if (display != null) {
            RLottieDrawable.updateScreenRefreshRate((int) display.getRefreshRate());
            screenRefreshRate = (int) display.getRefreshRate();

            Configuration configuration = context.getResources().getConfiguration();
            if (configuration.screenWidthDp != Configuration.SCREEN_WIDTH_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenWidthDp * density);
                if (Math.abs(displaySize.x - newSize) > 3) {
                    displaySize.x = newSize;
                }
            }
            if (configuration.screenHeightDp != Configuration.SCREEN_HEIGHT_DP_UNDEFINED) {
                int newSize = (int) Math.ceil(configuration.screenHeightDp * density);
                if (Math.abs(displaySize.y - newSize) > 3) {
                    displaySize.y = newSize;
                }
            }
        }
    }


    public static <T> boolean isValueAssigned(@NonNull T value, @NonNull T[] args) {
        return Arrays.asList(args).contains(value);
    }

    public static <T> boolean isValueAssigned(@NonNull T value, @NonNull List<T> args) {
        return args.contains(value);
    }

    public static <T> boolean isOneElementAssigned(@NonNull List<T> array, @NonNull T[] args) {
        List<T> temp = Arrays.asList(args);
        for (T i : array) {
            if (temp.contains(i)) {
                return true;
            }
        }
        return false;
    }

    public static <T> boolean isOneElementAssigned(@NonNull List<T> array, @NonNull List<T> args) {
        for (T i : array) {
            if (args.contains(i)) {
                return true;
            }
        }
        return false;
    }

    public static void safeObjectCall(@Nullable Object object, @NonNull safeCallInt function) {
        if (object != null) {
            function.call();
        }
    }

    public static boolean safeCheck(@Nullable CharSequence object, @NonNull safeCallCheckInt function) {
        if (!isEmpty(object)) {
            return function.check();
        }
        return false;
    }

    public static void safeCall(@Nullable CharSequence object, @NonNull safeCallInt function) {
        if (!isEmpty(object)) {
            function.call();
        }
    }

    public static void safeCall(@Nullable Collection<?> object, @NonNull safeCallInt function) {
        if (!isEmpty(object)) {
            function.call();
        }
    }

    public static void safeCall(@Nullable Map<?, ?> data, @NonNull safeCallInt function) {
        if (!isEmpty(data)) {
            function.call();
        }
    }

    public static int clamp(int value, int min, int max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
    }

    public static float clamp(float value, float min, float max) {
        if (value > max) {
            return max;
        } else if (value < min) {
            return min;
        }
        return value;
    }

    @SuppressLint("CheckResult")
    public static void inMainThread(@NonNull safeCallInt function) {
        Completable.complete()
                .observeOn(Includes.provideMainThreadScheduler())
                .subscribe(function::call);
    }

    public static OkHttpClient.Builder createOkHttp(int timeouts) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(timeouts, TimeUnit.SECONDS)
                .readTimeout(timeouts, TimeUnit.SECONDS)
                .addInterceptor(chain -> chain.proceed(chain.request().newBuilder().addHeader("X-VK-Android-Client", "new").addHeader("User-Agent", Constants.USER_AGENT(AccountType.BY_TYPE)).build())).addInterceptor(BrotliInterceptor.INSTANCE);
        ProxyUtil.applyProxyConfig(builder, Includes.getProxySettings().getActiveProxy());
        return builder;
    }

    public static <T> T BY_DEFAULT_ACCOUNT_TYPE(T vk_official, T kate) {
        if (Constants.DEFAULT_ACCOUNT_TYPE == AccountType.VK_ANDROID) {
            return vk_official;
        }
        return kate;
    }

    public static boolean isKateDeault() {
        return Constants.DEFAULT_ACCOUNT_TYPE == AccountType.KATE;
    }

    public static String getErrorString(Activity context, Throwable throwable) {
        if (context == null || context.isFinishing() || context.isDestroyed()) {
            return null;
        }
        throwable = getCauseIfRuntime(throwable);
        if (Constants.IS_DEBUG) {
            throwable.printStackTrace();
        }
        return ErrorLocalizer.localizeThrowable(context.getApplicationContext(), throwable);
    }

    public static void showErrorInAdapter(Activity context, Throwable throwable) {
        if (context == null || context.isFinishing() || context.isDestroyed()) {
            return;
        }
        throwable = getCauseIfRuntime(throwable);
        if (Constants.IS_DEBUG) {
            throwable.printStackTrace();
        }
        showRedTopToast(context, ErrorLocalizer.localizeThrowable(context.getApplicationContext(), throwable));
    }

    /**
     * Returns the bitmap position inside an imageView.
     *
     * @param imageView source ImageView
     * @return 0: left, 1: top, 2: width, 3: height
     */
    public static int[] getBitmapPositionInsideImageView(@NonNull ImageView imageView) {
        int[] ret = new int[4];
        if (imageView.getDrawable() == null)
            return ret;
        // Get image dimensions
        // Get image matrix values and place them in an array
        float[] f = new float[9];
        imageView.getImageMatrix().getValues(f);
        // Extract the scale values using the constants (if aspect ratio maintained, scaleX == scaleY)
        float scaleX = f[Matrix.MSCALE_X];
        float scaleY = f[Matrix.MSCALE_Y];
        // Get the drawable (could also get the bitmap behind the drawable and getWidth/getHeight)
        Drawable d = imageView.getDrawable();
        int origW = d.getIntrinsicWidth();
        int origH = d.getIntrinsicHeight();
        // Calculate the actual dimensions
        int actW = Math.round(origW * scaleX);
        int actH = Math.round(origH * scaleY);
        ret[2] = actW;
        ret[3] = actH;
        // Get image position
        // We assume that the image is centered into ImageView
        int imgViewW = imageView.getWidth();
        int imgViewH = imageView.getHeight();
        int top = (int) ((float) imgViewH - actH) / 2;
        int left = (int) ((float) imgViewW - actW) / 2;
        ret[0] = left;
        ret[1] = top;
        return ret;
    }

    public static MediaItem makeMediaItem(String url) {
        return new MediaItem.Builder().setUri(url).build();
    }

    public static <T extends RecyclerView.ViewHolder> View createAlertRecycleFrame(@NonNull Context context, @NonNull RecyclerView.Adapter<T> adapter, @Nullable String message, int accountId) {
        View root = View.inflate(context, R.layout.alert_recycle_frame, null);
        RecyclerView recyclerView = root.findViewById(R.id.alert_recycle);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(adapter);

        EmojiconTextView mMessage = root.findViewById(R.id.alert_message);
        if (isEmpty(message)) {
            mMessage.setVisibility(View.GONE);
        } else {
            mMessage.setVisibility(View.VISIBLE);
            mMessage.setText(OwnerLinkSpanFactory.withSpans(message, true, false, new LinkActionAdapter() {
                @Override
                public void onOwnerClick(int ownerId) {
                    PlaceFactory.getOwnerWallPlace(accountId, ownerId, null).tryOpenWith(context);
                }
            }));
        }
        return root;
    }

    private static Locale getLocaleSettings(@Lang int lang) {
        switch (lang) {
            case Lang.ENGLISH:
                Constants.DEVICE_COUNTRY_CODE = "en";
                return Locale.ENGLISH;
            case Lang.RUSSIA:
                Constants.DEVICE_COUNTRY_CODE = "ru";
                return new Locale("ru", "RU");
            case Lang.DEFAULT:
                break;
        }
        Constants.DEVICE_COUNTRY_CODE = "ru";
        return Locale.getDefault();
    }

    public static @NonNull
    Locale getAppLocale() {
        return getLocaleSettings(Settings.get().other().getLanguage());
    }

    @SuppressWarnings("deprecation")
    private static Locale getSystemLocale(Configuration config) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return config.getLocales().get(0);
        } else {
            return config.locale;
        }
    }

    @SuppressWarnings("deprecation")
    private static void setSystemLocaleLegacy(Configuration config, Locale locale) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
    }

    public static Context updateActivityContext(Context base) {
        if (getSystemLocale(base.getResources().getConfiguration()) != null && !isEmpty(getSystemLocale(base.getResources().getConfiguration()).getLanguage())) {
            Constants.DEVICE_COUNTRY_CODE = getSystemLocale(base.getResources().getConfiguration()).getLanguage().toLowerCase();
        } else {
            Constants.DEVICE_COUNTRY_CODE = "ru";
        }
        int size = Settings.get().main().getFontSize();
        @Lang int lang = Settings.get().other().getLanguage();
        Locale locale = getLocaleSettings(lang);
        AppTextUtils.updateDateLang(locale);
        FileUtil.updateDateLang(locale);
        if (size == 0) {
            if (lang == Lang.DEFAULT) {
                return base;
            } else {
                Resources res = base.getResources();
                Configuration config = new Configuration(res.getConfiguration());
                setSystemLocaleLegacy(config, getLocaleSettings(lang));
                return base.createConfigurationContext(config);
            }
        } else {
            Resources res = base.getResources();
            Configuration config = new Configuration(res.getConfiguration());
            config.fontScale = res.getConfiguration().fontScale + 0.15f * size;
            if (lang != Lang.DEFAULT) {
                setSystemLocaleLegacy(config, getLocaleSettings(lang));
            }
            return base.createConfigurationContext(config);
        }
    }

    public static boolean checkValues(Collection<Boolean> values) {
        for (Boolean i : values) {
            if (!i) {
                return false;
            }
        }
        return true;
    }

    public static @Nullable
    String checkEditInfo(@Nullable String info, @Nullable String original) {
        if (isEmpty(info) || info.equals(original)) {
            return null;
        }
        return info;
    }

    public static @Nullable
    Integer checkEditInfo(@Nullable Integer info, @Nullable Integer original) {
        if (isNull(info) || info.equals(original)) {
            return null;
        }
        return info;
    }

    private static String parseResponse(String str, Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void getRegistrationDate(Context context, int owner_id) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder().addHeader("X-VK-Android-Client", "new").addHeader("User-Agent", Constants.USER_AGENT(AccountType.BY_TYPE)).build();
                    return chain.proceed(request);
                });
        ProxyUtil.applyProxyConfig(builder, Includes.getProxySettings().getActiveProxy());
        Request request = new Request.Builder()
                .url("https://vk.com/foaf.php?id=" + owner_id).build();

        builder.build().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call th, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call th, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String resp = response.body().string();
                    String result = context.getString(R.string.error);
                    Locale locale = getAppLocale();
                    try {
                        String registered = null, auth = null, changes = null;
                        String tmp = parseResponse(resp, Pattern.compile("ya:created dc:date=\"(.*?)\""));
                        if (!isEmpty(tmp)) {
                            registered = DateFormat.getDateInstance(1).format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", locale).parse(tmp));
                        }
                        tmp = parseResponse(resp, Pattern.compile("ya:lastLoggedIn dc:date=\"(.*?)\""));
                        if (!isEmpty(tmp)) {
                            auth = DateFormat.getDateInstance(1).format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", locale).parse(tmp));
                        }
                        tmp = parseResponse(resp, Pattern.compile("ya:modified dc:date=\"(.*?)\""));
                        if (!isEmpty(tmp)) {
                            changes = DateFormat.getDateInstance(1).format(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZ", locale).parse(tmp));
                        }
                        result = context.getString(R.string.registration_date_info, registered, auth, changes);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    Handler uiHandler = new Handler(context.getMainLooper());
                    String finalResult = result;
                    uiHandler.post(() -> new MaterialAlertDialogBuilder(context)
                            .setIcon(R.drawable.dir_person)
                            .setMessage(finalResult)
                            .setTitle(context.getString(R.string.registration_date))
                            .setCancelable(true)
                            .show());
                }
            }
        });
    }

    @Nullable
    public static ViewPager2.PageTransformer createPageTransform(@Transformers_Types int type) {
        switch (type) {
            case Transformers_Types.SLIDER_TRANSFORMER:
                return new SliderTransformer(1);
            case Transformers_Types.CLOCK_SPIN_TRANSFORMER:
                return new ClockSpinTransformer();
            case Transformers_Types.BACKGROUND_TO_FOREGROUND_TRANSFORMER:
                return new BackgroundToForegroundTransformer();
            case Transformers_Types.CUBE_IN_DEPTH_TRANSFORMER:
                return new CubeInDepthTransformer();
            case Transformers_Types.DEPTH_TRANSFORMER:
                return new DepthTransformer();
            case Transformers_Types.FAN_TRANSFORMER:
                return new FanTransformer();
            case Transformers_Types.GATE_TRANSFORMER:
                return new GateTransformer();
            case Transformers_Types.OFF:
                return null;
            case Transformers_Types.ZOOM_OUT_TRANSFORMER:
                return new ZoomOutTransformer();
        }
        return null;
    }

    @Nullable
    public static Bitmap generateQR(@NonNull String url, @NonNull Context context) {
        return new CustomQRCodeWriter().encode(url, 768, 768, ResourcesCompat.getDrawable(context.getResources(), R.drawable.for_qr, context.getTheme()));
    }

    public static int makeMutablePendingIntent(int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (flags == 0) {
                return PendingIntent.FLAG_MUTABLE;
            } else {
                return flags | PendingIntent.FLAG_MUTABLE;
            }
        }
        return flags;
    }

    public static int rnd(int min, int max) {
        max -= min;
        return (int) (Math.random() * ++max) + min;
    }

    static public <T> T getRandomFromList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            return list.get(new Random().nextInt(list.size()));
        }
    }

    static public <T> T getRandomFromArray(T[] list) {
        if (list == null || list.length <= 0) {
            return null;
        } else {
            return list[new Random().nextInt(list.length)];
        }
    }

    public static int nextIntInRangeButExclude(int start, int end, Integer[] excludes) {
        if (excludes == null || excludes.length <= 0) {
            return rnd(start, end);
        }
        List<Integer> ints = new ArrayList<>(end - start + 1);
        for (int i = start; i <= end; i++) {
            ints.add(i);
        }
        ints.removeAll(Arrays.asList(excludes));
        return getRandomFromList(ints);
    }

    public static void openPlaceWithSwipebleActivity(@NonNull Context context, @NonNull Place place) {
        Intent intent = new Intent(context, SwipebleActivity.class);
        intent.setAction(MainActivity.ACTION_OPEN_PLACE);
        intent.putExtra(Extra.PLACE, place);
        SwipebleActivity.start(context, intent);
    }

    public static String BytesToSize(long Bytes) {
        long tb = 1099511627776L;
        long gb = 1073741824;
        long mb = 1048576;
        long kb = 1024;

        String returnSize;
        if (Bytes >= tb)
            returnSize = String.format(Locale.getDefault(), "%.2f TB", (double) Bytes / tb);
        else if (Bytes >= gb)
            returnSize = String.format(Locale.getDefault(), "%.2f GB", (double) Bytes / gb);
        else if (Bytes >= mb)
            returnSize = String.format(Locale.getDefault(), "%.2f MB", (double) Bytes / mb);
        else if (Bytes >= kb)
            returnSize = String.format(Locale.getDefault(), "%.2f KB", (double) Bytes / kb);
        else returnSize = String.format(Locale.getDefault(), "%d Bytes", Bytes);
        return returnSize;
    }

    public interface safeCallInt {
        void call();
    }

    public interface safeCallCheckInt {
        boolean check();
    }

    public interface SimpleFunction<F, S> {
        S apply(F orig);
    }
}
