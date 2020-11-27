package com.binlee.pluginization.hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.binlee.pluginization.util.Constants;
import com.binlee.pluginization.util.Hooks;
import com.binlee.pluginization.util.Reflecter;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public class HookedInstrumentation extends Instrumentation {

    private static final String TAG = Hooks.TAG + "-Inst";

    private final Instrumentation mBase;

    public HookedInstrumentation() {
        Object rawInst = Reflecter.on(Hooks.getActivityThread()).get("mInstrumentation");
        Log.d(TAG, "HookedInstrumentation() base: " + rawInst);
        mBase = ((Instrumentation) rawInst);
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
