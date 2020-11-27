package com.binlee.pluginization.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.util.List;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public final class ReceiverParser {

    private static final String TAG = "ReceiverParser";

    private ReceiverParser() {
    }

    public static void parse(Context context, String[] files) {
        for (String file : files) {
            parseReceivers(context, file);
        }
    }

    private static void parseReceivers(Context context, String plugin) {
        File file;
        try {
            file = new File(plugin);
        } catch (Throwable e) {
            throw new IllegalArgumentException();
        }
        // 获取原 sPackageManager 字段
        Object sPm = Reflecter.on(Hooks.getActivityThread()).get("sPackageManager");
        // android.content.pm.PackageParser
        // public Package parsePackage(File packageFile, int flags)
        Class<?>[] parameters = {File.class, int.class};
        Object[] args = {file, PackageManager.GET_RECEIVERS};
        // plugin package info
        Object obj = Reflecter.on("android.content.pm.PackageParser", sPm/*pm instance*/)
                .call("parsePackage", parameters, args);
        // Package#receivers -> ArrayList<Activity>
        List<?> receivers = (List<?>) Reflecter.on(obj).get("receivers");
        for (Object receiver : receivers) {
            // handle like a dynamic receiver
            registerReceiver(context, receiver);
        }
    }

    private static void registerReceiver(Context context, Object receiver) {
        if (receiver != null) {
            Log.w(TAG, "handleReceiver() receiver: " + receiver);
        }
        // android.content.pm.PackageParser$Component#intents
        //                                      ^
        //                                    super
        //                                      |
        // android.content.pm.PackageParser$Activity#intents
        List<?> intents = (List<?>) Reflecter.on(receiver).get("intents");
        for (Object intent : intents) {
            // android.content.pm.PackageParser$Activity#info
            ActivityInfo info = (ActivityInfo) Reflecter.on(intent).get("info");
            BroadcastReceiver br = (BroadcastReceiver) Reflecter.on(info.name).create();
            // Context#registerReceiver()
            context.registerReceiver(br, ((IntentFilter) intent));
        }
    }
}
