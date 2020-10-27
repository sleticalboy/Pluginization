package com.sleticalboy.pluginization.util;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created on 19-7-9.
 *
 * @author leebin
 */
public final class Hooks {

    private static final String TAG = "Hooks";

    private Hooks() {
        throw new AssertionError();
    }

    public static void hookHandlerCallback(MessageInterceptor interceptor) {
        if (interceptor == null) {
            throw new NullPointerException("hookHandlerCallback() listener == null");
        }
        final Object sCat = Reflections.getField("android.app.ActivityThread",
                "sCurrentActivityThread", null);
        final Object mH = Reflections.getField(sCat, "mH", sCat);
        Reflections.setField(Handler.class, mH, "mCallback",
                // 此处返回值很重要，如果返回 true，有可能 app 不能正常运行
                (Handler.Callback) interceptor::onMessage
        );
    }

    public static void hookActivityManager(final Context context, final InvokeListener listener) {
        final Object singleton = Reflections.getField(
                "android.app.ActivityManager", "IActivityManagerSingleton", null);
        final Object rawAm = Reflections.getField("android.util.Singleton", "mInstance", singleton);
        Log.d(TAG, "hookActivityManager singleton: " + singleton + ", rawAm: " + rawAm);

        final Object proxyAm = Proxies.newAmProxy(context, (o, method, args) -> {
            if (listener == null) {
                return method.invoke(rawAm, args);
            }
            listener.before(rawAm, method, args);
            return listener.after(method.invoke(rawAm, args));
        });
        Log.d(TAG, "hookActivityManager proxyAm: " + proxyAm);
        if (proxyAm != null) {
            Reflections.setField("android.util.Singleton", singleton, "mInstance", proxyAm);
        }
    }

    public static void hookPackageManager(Context context) {
        // dynamic proxy
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

        Hooks.hookActivityManager(app, new InvokeListener() {

            String mName;

            @Override
            public void before(final Object rawCaller, final Method method, final Object... args) {
                mName = method.getName();
                Log.d(TAG, "method --------> " + mName);
                if ("startActivity".equals(mName)) {
                    // Log.d(TAG, "args: " + JSON.toJSONString(args));
                    // 启动未在 AndroidManifest.xml 文件中注册的 Activity
                    int index = -1;
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Intent) {
                            // 找到第一个 Intent 参数进行加工
                            index = i;
                            break;
                        }
                    }
                    if (index >= 0) {
                        final Intent raw = (Intent) args[index];
                        Log.d(TAG, "index: " + index + ", raw intent: " + raw);
                        // 替换 component 为空壳 Activity
                        final ComponentName component = new ComponentName("com.sleticalboy.pluginization",
                                "com.sleticalboy.pluginization.NoNameActivity");
                        raw.putExtra("real_activity", component);
                    }
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

        hookInstrumentation();
    }

    public static void hookInstrumentation() {
        final Object sCat = Reflections.getField("android.app.ActivityThread",
                "sCurrentActivityThread", null);
        final Object rawInst = Reflections.getField(sCat, "mInstrumentation", sCat);
        final HookedInstrumentation hooked = new HookedInstrumentation((Instrumentation) rawInst);
        Reflections.setField(sCat, sCat, "mInstrumentation", hooked);
    }

    abstract public static class InvokeListener {

        public void before(Object rawCaller, Method method, Object... args) {
        }

        public Object after(Object rawResult) {
            return rawResult;
        }
    }

    public interface MessageInterceptor {

        boolean onMessage(Message msg);
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
            final ComponentName component = intent.getParcelableExtra("real_activity");
            Log.d(TAG, "newActivity() called with: cl = [" + cl + "], className = [" + className
                    + "], intent = [" + intent + "], real activity: " + component);
            return mBase.newActivity(cl, className, intent);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle icicle) {
            Log.d(TAG, "callActivityOnCreate() act: " + activity + ", icicle: " + icicle);
            mBase.callActivityOnCreate(activity, icicle);
        }

        @Override
        public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
            Log.d(TAG, "callActivityOnCreate() act: " + activity + ", icicle: " + icicle
                    + ", persistentState: " + persistentState);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mBase.callActivityOnCreate(activity, icicle, persistentState);
            } else {
                super.callActivityOnCreate(activity, icicle, persistentState);
            }
        }
    }
}
