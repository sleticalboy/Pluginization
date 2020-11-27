package com.binlee.pluginization.hook;

import android.content.Intent;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import com.binlee.pluginization.util.Constants;
import com.binlee.pluginization.util.Hooks;
import com.binlee.pluginization.util.Reflecter;

import java.lang.ref.WeakReference;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public class AMHookListener implements HookListener {

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
}
