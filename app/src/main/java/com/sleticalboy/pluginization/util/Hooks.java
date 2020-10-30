package com.sleticalboy.pluginization.util;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Proxy;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public final class Hooks {

    public static final String TAG = "Hooks";
    public static final ComponentName PROXY_COMPONENT = new ComponentName(
            "com.sleticalboy.pluginization", "com.sleticalboy.pluginization.ProxyActivity");
    public static final String REAL_COMPONENT = "real_component";

    private static boolean sAtmHooked = false;

    private Hooks() {
        throw new AssertionError();
    }

    public static void hookHandlerCallback(Handler.Callback callback) {
        if (callback == null) {
            throw new NullPointerException("hookHandlerCallback() callback == null");
        }
        Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
        Object mH = Reflecter.on("android.app.ActivityThread", sCat).get("mH");
        // Object rawCallback = Reflecter.on(Handler.class, mH).get("mCallback");
        // if (rawCallback != null && Proxy.isProxyClass(rawCallback.getClass())) {
        //     return;
        // }
        // 此处 callback 返回值很重要，如果返回 true，有可能 app 不能正常运行
        Reflecter.on(Handler.class, mH).set("mCallback", callback);
    }

    public static void hookActivityManager(Context context, InvokeListener listener) {
        Object singleton = Reflecter.on("android.app.ActivityManager")
                .get("IActivityManagerSingleton");
        Object rawAm = Reflecter.on("android.util.Singleton", singleton).get("mInstance");
        Log.d(TAG, "hookActivityManager singleton: " + singleton + ", rawAm: " + rawAm);
        if (Proxy.isProxyClass(rawAm.getClass())) {
            return;
        }

        Object proxyAm = Proxies.newAmProxy(context, (proxy, method, args) -> {
            if (listener == null) {
                return method.invoke(rawAm, args);
            }
            listener.before(rawAm, method.getName(), args);
            return listener.after(method.invoke(rawAm, args));
        });
        Log.d(TAG, "hookActivityManager proxyAm: " + proxyAm);
        if (proxyAm != null) {
            Reflecter.on("android.util.Singleton", singleton).set("mInstance", proxyAm);
        }
    }

    public static void hookPackageManager(Context context, InvokeListener listener) {
        Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
        // 获取原 sPackageManager 字段
        Object rawPm = Reflecter.on(sCat).get("sPackageManager");
        if (Proxy.isProxyClass(rawPm.getClass())) {
            return;
        }
        // 生成 PackageManager 代理对象
        Object proxyPm = Proxies.newPmProxy(context, (proxy, method, args) -> {
            if (null == listener) {
                return method.invoke(rawPm, args);
            }
            listener.before(rawPm, method.getName(), args);
            return listener.after(method.invoke(rawPm, args));
        });
        Log.d(TAG, "hookActivityTaskManager proxyPm: " + proxyPm);
        if (null != proxyPm) {
            Reflecter.on("android.app.ActivityThread", sCat).set("sPackageManager", proxyPm);
        }
    }

    public static void hookActivityTaskManager(Context context, InvokeListener listener) {
        if (sAtmHooked) {
            return;
        }
        Object singleton = Reflecter.on("android.app.ActivityTaskManager")
                .get("IActivityTaskManagerSingleton");
        Object rawAtm = Reflecter.on("android.util.Singleton", singleton).get("mInstance");
        Log.d(TAG, "hookActivityTaskManager singleton: " + singleton + ", rawAm: " + rawAtm);
        if (null == rawAtm || Proxy.isProxyClass(rawAtm.getClass())) {
            return;
        }
        sAtmHooked = true;

        Object proxyAtm = Proxies.newAtmProxy(context, (proxy, method, args) -> {
            if (listener == null) {
                return method.invoke(rawAtm, args);
            }
            listener.before(rawAtm, method.getName(), args);
            return listener.after(method.invoke(rawAtm, args));
        });
        Log.d(TAG, "hookActivityTaskManager proxyAtm: " + proxyAtm);
        if (proxyAtm != null) {
            Reflecter.on("android.util.Singleton", singleton).set("mInstance", proxyAtm);
        }
    }

    public static void init(Application app) {
        if (app == null) {
            return;
        }
        Hooks.hookHandlerCallback(msg -> {
            switch (msg.what) {
                default:
                case Messages.LAUNCH_ACTIVITY:
                case Messages.PAUSE_ACTIVITY:
                case Messages.RESUME_ACTIVITY:
                    break;
                case Messages.EXECUTE_TRANSACTION:
                    Log.d(TAG, "onMessage() action: " + Messages.codeToString(msg.what)
                            + ", msg: " + /*JSON.toJSONString(msg)*/msg);
                    break;
            }
            return false;
        });

        hookActivityManager(app, new InvokeListener() {

            public static final String TAG = Hooks.TAG + "-Am";
            private String mName;

            @Override
            public void before(final Object rawCaller, final String method, final Object... args) {
                mName = method;
                Log.d(TAG, "method --------> " + mName);
                if ("startActivity".equals(mName)) {
                    int index = -1;
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Intent) {
                            // 找到第一个 Intent 参数进行加工
                            index = i;
                            break;
                        }
                    }
                    if (0 > index) {
                        return;
                    }
                    final Intent raw = (Intent) args[index];
                    Log.d(TAG, "index: " + index + ", raw intent: " + raw);
                    // 启动未在 AndroidManifest.xml 文件中注册的 Activity
                    raw.putExtra(REAL_COMPONENT, raw.getComponent());
                    // 替换 component 为 ProxyActivity, 此 Activity 已在 AndroidManifest 中声明
                    raw.setComponent(PROXY_COMPONENT);
                }
            }

            @Override
            public Object after(final Object rawResult) {
                if ("startActivity".equals(mName)) {
                    Log.d(TAG, "afterInvoke() result: " + rawResult);
                }
                return rawResult;
            }
        });

        hookPackageManager(app, new InvokeListener() {

            private static final String TAG = Hooks.TAG + "-Pm";
            private String mName;

            @Override
            public void before(Object rawCaller, String method, Object... args) {
                mName = method;
                Log.d(TAG, "method --------> " + mName);
                if ("getPackageInfo".equals(mName)) {
                    //
                }
            }

            @Override
            public Object after(Object rawResult) {
                if ("getPackageInfo".equals(mName)) {
                    Log.d(TAG, "afterInvoke() result: " + rawResult);
                }
                return rawResult;
            }
        });

        hookInstrumentation();
    }

    public static void hookInstrumentation() {
        Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
        Object rawInst = Reflecter.on(sCat).get("mInstrumentation");
        HookedInstrumentation hooked = new HookedInstrumentation((Instrumentation) rawInst);
        Reflecter.on(sCat).set("mInstrumentation", hooked);
    }

    abstract public static class InvokeListener {

        public void before(Object rawCaller, String method, Object... args) {
        }

        public Object after(Object rawResult) {
            return rawResult;
        }
    }

    public static class HookedInstrumentation extends Instrumentation {

        private static final String TAG = "HookedInst";

        private final Instrumentation mBase;

        protected HookedInstrumentation(Instrumentation base) {
            mBase = base;
            Log.d(TAG, "HookedInstrumentation() base: " + base);
        }

        @Override
        public Activity newActivity(ClassLoader cl, String className, Intent intent)
                throws ClassNotFoundException, IllegalAccessException, InstantiationException {
            Log.d(TAG, "newActivity() called with: className = [" + className
                    + "], intent = [" + intent + "]");
            // 取出真正要启动的 Activity 并实例化
            final ComponentName component = intent.getParcelableExtra(REAL_COMPONENT);
            final String realClass = null != component ? component.getClassName() : className;
            return mBase.newActivity(cl, realClass, intent);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle icicle) {
            Log.d(TAG, "callActivityOnCreate() act: " + activity + ", icicle: " + icicle);
            // newActivity() 之后会调用到这里
            mBase.callActivityOnCreate(activity, icicle);
        }
    }
}
