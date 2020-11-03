package com.sleticalboy.pluginization.util;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

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

    public static void hookHandlerCallback(Handler.Callback callback) {
        if (callback == null) {
            throw new NullPointerException("hookHandlerCallback() callback == null");
        }
        Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
        Object mH = Reflecter.on("android.app.ActivityThread", sCat).get("mH");
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

        InvocationHandler handler = (proxy, method, args) -> {
            if (listener == null) {
                return method.invoke(rawAm, args);
            }
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

    public static void hookPackageManager(Context context, InvokeListener listener) {
        Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
        // 获取原 sPackageManager 字段
        Object rawPm = Reflecter.on(sCat).get("sPackageManager");
        if (Proxy.isProxyClass(rawPm.getClass())) {
            return;
        }
        InvocationHandler handler = (proxy, method, args) -> {
            if (null == listener) {
                return method.invoke(rawPm, args);
            }
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
        Reflecter.on("android.app.ActivityThread", sCat).set("sPackageManager", proxyPm);
        Log.d(TAG, "hookPackageManager proxyPm: " + proxyPm);
    }

    public static void hookActivityTaskManager(Context context, InvokeListener listener) {
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
        sAtmHooked = true;

        InvocationHandler handler = (proxy, method, args) -> {
            if (listener == null) {
                return method.invoke(rawAtm, args);
            }
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
    }

    public static void init(Application app) {
        if (app == null) {
            return;
        }
        hookHandlerCallback(new Handler.Callback() {
            private static final String TAG = Hooks.TAG + "-mCallback";

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Log.d(TAG, "handleMessage() action: " + Constants.codeToString(msg.what));
                switch (msg.what) {
                    default:
                    case Constants.LAUNCH_ACTIVITY:
                    case Constants.PAUSE_ACTIVITY:
                    case Constants.RESUME_ACTIVITY:
                        break;
                    case Constants.BIND_SERVICE:
                    case Constants.UNBIND_SERVICE:
                    case Constants.STOP_SERVICE:
                    case Constants.SERVICE_ARGS:
                        // Service#onStartCommand()
                        break;
                    case Constants.CREATE_SERVICE:
                        // start & bind Service 均会执行到这里
                        handleCreateService(TAG, msg.obj);
                        break;
                    case Constants.RECEIVER:
                        // the original logic is to initialize BroadcastReceiver and call its
                        // onReceive() method
                        break;
                    case Constants.EXECUTE_TRANSACTION:
                        Log.d(TAG, "handleMessage() msg: " + /*JSON.toJSONString(msg)*/msg);
                        break;
                }
                return false;
            }
        });

        hookActivityManager(app, new InvokeListener() {

            public static final String TAG = Hooks.TAG + "-Am";
            // supported before android P
            private boolean mStartActivity;
            private boolean mStartService, mStopService;
            private boolean mBindService, mUnbindService;
            private boolean mRegister, mUnregister;

            @Override
            public void before(final Object rawCaller, final String method, final Object... args) {
                Log.d(TAG, "method --------> " + method);
                mStartActivity = "startActivity".equals(method);
                // start & stop service
                mStartService = "startService".equals(method);
                mStopService = "stopService".equals(method);
                // bind & unbind service
                mBindService = "bindIsolatedService".equals(method);
                mUnbindService = "unbindService".equals(method);
                // register & unregister receiver
                mRegister = "registerReceiver".equals(method);
                mUnregister = "unregisterReceiver".equals(method);

                if (mStartActivity || mStartService || mStopService
                        || mBindService || mUnbindService
                        || mRegister || mUnregister) {
                    int index = -1;
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Intent) {
                            // 找到第一个 Intent 参数进行加工
                            index = i;
                            break;
                        } else if (mRegister || mUnregister) {
                            if ("android.app.LoadedApk$ReceiverDispatcher$InnerReceiver".equals(
                                    args[i].getClass().getName())) {
                                index = i;
                                break;
                            }
                        }
                    }
                    if (0 > index) {
                        return;
                    }
                    final Object raw = args[index];
                    Log.d(TAG, "before index: " + index + ", raw intent: " + raw);
                    if (!mRegister && !mUnregister) {
                        // 启动未在 AndroidManifest.xml 文件中注册的 Activity/Service
                        ((Intent) raw).putExtra(Constants.REAL_COMPONENT, ((Intent) raw).getComponent());
                        // 替换 component 为 ProxyActivity/Service, 此 Activity/Service 已在
                    }
                    // AndroidManifest 中声明
                    if (mStartActivity) {
                        ((Intent) raw).setComponent(Constants.PROXY_ACTIVITY);
                    } else if (mRegister || mUnregister) {
                        handleReceiver(raw);
                    } else {
                        // stopService 时，内存中 ServiceInfo#name 字段已经是实际的 service 了
                        ((Intent) raw).setComponent(Constants.PROXY_SERVICE);
                    }
                    Log.d(TAG, "after index: " + index + ", raw intent: " + raw);
                }
            }

            @Override
            public Object after(final Object rawResult) {
                if (mStartService || mStartActivity || mStopService
                        || mBindService || mUnbindService
                        || mRegister || mUnregister) {
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

    private static void handleCreateService(String tag, Object obj) {
        if (obj != null) {
            ServiceInfo info = (ServiceInfo) Reflecter.on(obj).get("info");
            info.name = getRealService(info.name);
            Log.d(tag, "handleStartService() service info: " + info);
        }
    }

    private static String getRealService(String name) {
        // 通过事先配置好的映射关系来启动真正的 Service
        // ProxyService -> AnotherService
        return name.replace("Proxy", "Another");
    }

    public static void hookInstrumentation() {
        Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
        Object rawInst = Reflecter.on(sCat).get("mInstrumentation");
        HookedInstrumentation hooked = new HookedInstrumentation((Instrumentation) rawInst);
        Reflecter.on(sCat).set("mInstrumentation", hooked);
    }

    private static void parseReceivers(Context context, String plugin) {
        File file;
        try {
            file = new File(plugin);
        } catch (Throwable e) {
            throw new IllegalArgumentException();
        }
        Object sCat = Reflecter.on("android.app.ActivityThread").get("sCurrentActivityThread");
        // 获取原 sPackageManager 字段
        Object sPm = Reflecter.on(sCat).get("sPackageManager");
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
            Log.d(TAG, "handleReceiver() receiver: " + receiver);
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

    private static void handleReceiver(Object raw) {
        // 以下均为测试代码，实际的项目中不会用到

        // android.app.LoadedApk$ReceiverDispatcher$InnerReceiver#mDispatcher
        // -> android.app.LoadedApk$ReceiverDispatcher
        // android.app.LoadedApk$ReceiverDispatcher#mReceiver
        // -> ProxyReceiver
        // 此处通过反射获取 dispatcher 是会失败的，原因是 raw 对象是一个 binder 的 proxy
        // Accessing hidden field Landroid/app/LoadedApk$ReceiverDispatcher$InnerReceiver;
        // ->mDispatcher:Ljava/lang/ref/WeakReference;
        // (greylist-max-o, reflection, denied)
        if (raw instanceof IInterface) {
            final IBinder binder = ((IInterface) raw).asBinder();
            Log.d(TAG, "handleReceiver() as binder: " + binder);
        }
        // dispatcher is a WeakReference<android.app.LoadedApk$ReceiverDispatcher>
        Object weakRef;
        try {
            weakRef = Reflecter.on(raw).get("mDispatcher");
        } catch (Exception e) {
            e.printStackTrace();
            // fallback to mOwner field
            try {
                weakRef = Reflecter.on(raw).get("mOwner");
            } catch (Exception ex) {
                ex.printStackTrace();
                weakRef = null;
            }
        }
        Log.d(TAG, "handleReceiver() weakRef: " + weakRef);

        Object dispatcher = null;
        if (weakRef instanceof WeakReference) {
            dispatcher = ((WeakReference<?>) weakRef).get();
        }
        if (dispatcher == null) {
            return;
        }
        Object receiver = Reflecter.on(dispatcher).get("mReceiver");
        Log.d(TAG, "register or unregister receiver, dispatcher: " + dispatcher
                + ", receiver: " + receiver);
    }

    public static void parseProviders(Context context, String plugin) {
        // 可以在宿主 apk 的 Application#attachBaseContent() 方法中执行此方法
        File file;
        try {
            file = new File(plugin);
        } catch (Throwable e) {
            throw new IllegalArgumentException();
        }

        // android.content.pm.PackageParser
        // public Package parsePackage(File packageFile, int flags)
        Object parser = Reflecter.on("android.content.pm.PackageParser").create();
        // parse plugin package info
        Class<?>[] parameters = {File.class, int.class};
        Object[] args = {file, PackageManager.GET_PROVIDERS};
        Object packageInfo = Reflecter.on(parser).call("parsePackage", parameters, args);

        // get providers form Package#providers -> ArrayList<Provider>
        List<?> providers = (List<?>) Reflecter.on(packageInfo).get("providers");

        // prepare params for android.content.pm.PackageParser
        // static generateProviderInfo(Provider p, int flags, PackageUserState state, int userId)
        Object state = Reflecter.on("android.content.pm.PackageUserState").create();
        int userId = (int) Reflecter.on("android.os.UserHandle").call("getCallingUserId");
        parameters = new Class[]{providers.get(0).getClass(), int.class, state.getClass(), int.class};
        args = new Object[]{null/*provider*/, 0, state, userId};

        List<ProviderInfo> infoList = new ArrayList<>(providers.size());
        for (Object p : providers) {
            args[0] = p;
            ProviderInfo info = (ProviderInfo) Reflecter.on(parser.getClass())
                    .call("generateProviderInfo", parameters, args);
            // 将插件的 packageName 替换为宿主的 packageName
            info.applicationInfo.packageName = context.getPackageName();
            infoList.add(info);
        }
        Log.d(TAG, "infoList: " + infoList);

        // install providers
        // ActivityThread -> private void installContentProviders(
        //         Context context, List<ProviderInfo> providers)
        parameters = new Class[]{Context.class, List.class};
        args = new Object[]{context, infoList};
        // Reflecter.on(sCat).call("installContentProviders", parameters, args);
    }

    abstract public static class InvokeListener {

        public void before(Object rawCaller, String method, Object... args) {
        }

        public Object after(Object rawResult) {
            return rawResult;
        }
    }

    public static class HookedInstrumentation extends Instrumentation {

        private static final String TAG = Hooks.TAG + "-Inst";

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
            final ComponentName component = intent.getParcelableExtra(Constants.REAL_COMPONENT);
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
