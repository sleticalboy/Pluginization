package com.binlee.pluginization;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created on 20-11-1.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ProxyReceiver extends BroadcastReceiver {

    private static final String TAG = "ProxyReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() context = [" + context + "], intent = [" + intent + "]");
        // 接收到广播后将 action 分发给插件中的广播
        Intent pluginAction = getIntent(intent.getAction());
        context.sendBroadcast(pluginAction);
    }

    private Intent getIntent(String action) {
        // action -> plugin action
        return new Intent(action + "_");
    }
}
