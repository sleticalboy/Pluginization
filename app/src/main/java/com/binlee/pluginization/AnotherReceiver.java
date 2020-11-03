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
public final class AnotherReceiver extends BroadcastReceiver {

    private static final String TAG = "AnotherReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive() context = [" + context + "], intent = [" + intent + "]");
    }
}
