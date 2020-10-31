package com.sleticalboy.pluginization;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Created on 20-10-31.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class ProxyService extends Service {

    private static final String TAG = "ProxyService";

    private LocalBinder mBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() called with: intent = [" + intent + "]");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called with: intent = [" + intent + "]");
        if (mBinder == null) {
            mBinder = new LocalBinder(this);
        }
        return mBinder;
    }

    public static class LocalBinder extends Binder {

        private final ProxyService mService;

        public LocalBinder(ProxyService service) {
            mService = service;
        }

        public ProxyService getService() {
            return mService;
        }
    }
}
