package com.binlee.pluginization;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created on 20-11-3.
 *
 * @author binli
 */
public final class ProxyProvider extends ContentProvider {

    private static final String TAG = "ProxyProvider";

    @Override
    public boolean onCreate() {
        Log.d(TAG, "onCreate() called");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        uri = dispatch(uri);
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        uri = dispatch(uri);
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        uri = dispatch(uri);
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        uri = dispatch(uri);
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        uri = dispatch(uri);
        return 0;
    }

    private static Uri dispatch(Uri original) {
        // content://host/plugin_xxx/path/query -> content://plugin_xxx/path/query
        String uri = original.toString();
        if (uri.contains("host/plugin_xxx")) {
            uri = uri.replace("host/", "");
        }
        Log.d(TAG, "dispatch() original :" + original + ", real: " + uri);
        return Uri.parse(uri);
    }
}
