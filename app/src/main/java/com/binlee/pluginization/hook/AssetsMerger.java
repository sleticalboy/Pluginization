package com.binlee.pluginization.hook;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.binlee.pluginization.util.Reflecter;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public final class AssetsMerger {

    private static final String TAG = "AssetsMerger";

    private AssetsMerger() {
    }

    // Assets
    public static void addAssets(Context context, String plugin) {
        // 获取宿主 apk 的 assets
        AssetManager assets = context.getAssets();
        // 通过反射，将插件 apk 的资源与宿主的资源合并
        Object code = Reflecter.on(assets).call("addAssetPath", new Class[]{String.class}, plugin);
        Log.d(TAG, "addAssets() plugin = [" + plugin + "]， ret code: " + code);
    }
}
