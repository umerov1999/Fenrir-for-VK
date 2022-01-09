package dev.ragnarok.fenrir.model;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class FunctionSource {
    private final Text title;
    private final FunctionSourceCall call;
    private final @DrawableRes
    int icon;

    public FunctionSource(String title, @DrawableRes int icon, @NonNull FunctionSourceCall call) {
        this.title = new Text(title);
        this.call = call;
        this.icon = icon;
    }

    public FunctionSource(@StringRes int title, @DrawableRes int icon, @NonNull FunctionSourceCall call) {
        this.title = new Text(title);
        this.call = call;
        this.icon = icon;
    }

    public String getTitle(Context context) {
        return title.getText(context);
    }

    public void Do() {
        call.call();
    }

    public @DrawableRes
    int getIcon() {
        return icon;
    }

    public interface FunctionSourceCall {
        void call();
    }
}
