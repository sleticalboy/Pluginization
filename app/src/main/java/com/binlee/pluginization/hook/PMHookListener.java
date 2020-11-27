package com.binlee.pluginization.hook;

import android.util.Log;

import com.binlee.pluginization.util.Hooks;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public class PMHookListener implements HookListener {

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
}
