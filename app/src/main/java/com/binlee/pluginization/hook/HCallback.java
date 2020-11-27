package com.binlee.pluginization.hook;

import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.binlee.pluginization.util.Constants;
import com.binlee.pluginization.util.Hooks;
import com.binlee.pluginization.util.Reflecter;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public class HCallback implements Handler.Callback {

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
}
