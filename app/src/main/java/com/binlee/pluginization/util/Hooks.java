package com.binlee.pluginization.util;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.binlee.pluginization.hook.AMHookListener;
import com.binlee.pluginization.hook.ATMHookListener;
import com.binlee.pluginization.hook.HCallback;
import com.binlee.pluginization.hook.HookedInstrumentation;
import com.binlee.pluginization.hook.PMHookListener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public final class Hooks {

    public static final String TAG = "Hooks";

    private static boolean sAtmHooked = false;

    private Hooks() {
        throw new AssertionError();
    }

    public static void init(Application app) {
        if (app == null) {
            return;
        }
        hookHandlerCallback();

        hookActivityManager(app);

        hookPackageManager(app);

        hookInstrumentation();

        // copy plugin apk
        String[] files = copyPlugins(app);

        // Unable to get provider com.binlee.plugin.AnotherProvider:
        // java.lang.ClassNotFoundException:
        // Didn't find class "com.binlee.plugin.AnotherProvider" on path:
        // DexPathList[[zip file "/data/app/com.binlee.pluginization-KD7bSTMHikFhOr0D6ZiMvA==/base.apk"],
        // nativeLibraryDirectories=[/data/app/com.binlee.pluginization-KD7bSTMHikFhOr0D6ZiMvA==/lib/arm64, /system/lib64, /vendor/lib64]]

        // merge dex
        DexMerger.merge(app, files);

        // install providers
        ProviderParser.parse(app, files);

        // register receivers
        ReceiverParser.parse(app, files);
    }

    private static String[] copyPlugins(Application app) {
        try {
            File file = IOs.file(app.getApplicationInfo().dataDir + "/files/plugin/plugin-debug.apk");
            IOs.copy(app.getAssets().open("plugin-debug.apk"), file);
            Log.d(TAG, "plugin: " + file + ", exists: " + file.exists());
            return new String[]{file.getAbsolutePath()};
        } catch (IOException e) {
            Log.e(TAG, "copyPlugins() error", e);
            return new String[0];
        }
    }

    private static void hookHandlerCallback() {
        Object mH = Reflecter.on(getActivityThread()).get("mH");
        // 此处 callback 返回值很重要，如果返回 true，有可能 app 不能正常运行
        Reflecter.on(Handler.class, mH).set("mCallback", new HCallback());
    }

    private static void hookActivityManager(Context context) {
        Object singleton = Reflecter.on("android.app.ActivityManager")
                .get("IActivityManagerSingleton");
        Object rawAm = Reflecter.on("android.util.Singleton", singleton).get("mInstance");
        Log.d(TAG, "hookActivityManager singleton: " + singleton + ", rawAm: " + rawAm);
        if (Proxy.isProxyClass(rawAm.getClass())) {
            return;
        }

        final AMHookListener listener = new AMHookListener();
        InvocationHandler handler = (proxy, method, args) -> {
            listener.before(rawAm, method.getName(), args);
            return listener.after(method.invoke(rawAm, args));
        };
        Object proxyAm;
        try {
            proxyAm = Reflecter.with(context.getClassLoader())
                    .proxy("android.app.IActivityManager")
                    .handle(handler);
        } catch (Throwable e) {
            Log.e(TAG, "newAmProxy error", e);
            return;
        }
        Reflecter.on("android.util.Singleton", singleton).set("mInstance", proxyAm);
        Log.d(TAG, "hookActivityManager proxyAm: " + proxyAm);
    }

    private static void hookPackageManager(Context context) {
        // 获取原 sPackageManager 字段
        Object rawPm = Reflecter.on(getActivityThread()).get("sPackageManager");
        if (Proxy.isProxyClass(rawPm.getClass())) {
            return;
        }
        final PMHookListener listener = new PMHookListener();
        InvocationHandler handler = (proxy, method, args) -> {
            listener.before(rawPm, method.getName(), args);
            return listener.after(method.invoke(rawPm, args));
        };
        Object proxyPm;
        try {
            // 生成 PackageManager 代理对象
            proxyPm = Reflecter.with(context.getClassLoader())
                    .proxy("android.content.pm.IPackageManager")
                    .handle(handler);
        } catch (Throwable e) {
            Log.e(TAG, "newPmProxy() error", e);
            return;
        }
        Reflecter.on(getActivityThread()).set("sPackageManager", proxyPm);
        Log.d(TAG, "hookPackageManager proxyPm: " + proxyPm);
    }

    public static void hookActivityTaskManager(Context context) {
        // only supported after android Q
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || sAtmHooked) {
            return;
        }
        Object singleton = Reflecter.on("android.app.ActivityTaskManager")
                .get("IActivityTaskManagerSingleton");
        Object rawAtm = Reflecter.on("android.util.Singleton", singleton).get("mInstance");
        Log.d(TAG, "hookActivityTaskManager singleton: " + singleton + ", rawAm: " + rawAtm);
        if (null == rawAtm || Proxy.isProxyClass(rawAtm.getClass())) {
            return;
        }

        final ATMHookListener listener = new ATMHookListener();
        InvocationHandler handler = (proxy, method, args) -> {
            listener.before(rawAtm, method.getName(), args);
            return listener.after(method.invoke(rawAtm, args));
        };
        Object proxyAtm;
        try {
            proxyAtm = Reflecter.with(context.getClassLoader())
                    .proxy("android.app.IActivityTaskManager")
                    .handle(handler);
        } catch (Throwable e) {
            Log.e(TAG, "newAtmProxy() error", e);
            return;
        }
        Reflecter.on("android.util.Singleton", singleton).set("mInstance", proxyAtm);
        Log.d(TAG, "hookActivityTaskManager proxyAtm: " + proxyAtm);
        sAtmHooked = true;
    }

    public static void hookInstrumentation() {
        Reflecter.on(getActivityThread()).set("mInstrumentation", new HookedInstrumentation());
    }

    private static Object sActivityThread;

    public static Object getActivityThread() {
        if (sActivityThread == null) {
            Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
            sActivityThread = sCat;
            return sCat;
        }
        return sActivityThread;
    }
}
