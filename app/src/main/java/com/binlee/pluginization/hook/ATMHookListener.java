package com.binlee.pluginization.hook;

import android.content.Intent;
import android.util.Log;

import com.binlee.pluginization.util.Constants;
import com.binlee.pluginization.util.Hooks;

/**
 * Created on 20-11-27.
 *
 * @author binli
 */
public class ATMHookListener implements HookListener {

    public static final String TAG = Hooks.TAG + "-Atm";
    private String mName;

    @Override
    public void before(final Object rawCaller, final String method, final Object... args) {
        mName = method;
        Log.d(TAG, "method --------> " + mName);
        if ("startActivity".equals(mName)) {
            // 启动未在 AndroidManifest.xml 文件中注册的 Activity
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
            // 替换 component 为 ProxyActivity, 此 Activity 已在 AndroidManifest 中声明
            raw.putExtra(Constants.REAL_COMPONENT, raw.getComponent());
            raw.setComponent(Constants.PROXY_ACTIVITY);
        }
    }

    @Override
    public Object after(final Object rawResult) {
        if ("startActivity".equals(mName)) {
            Log.d(TAG, "afterInvoke() result: " + rawResult);
        }
        return rawResult;
    }
}
