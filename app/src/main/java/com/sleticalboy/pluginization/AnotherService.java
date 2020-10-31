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
public final class AnotherService extends Service {

    private static final String TAG = "AnotherService";

    private LocalBinder mBinder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        if (mBinder == null) {
            mBinder = new LocalBinder(this);
        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

    public static class LocalBinder extends Binder {

        private final AnotherService mService;

        public LocalBinder(AnotherService service) {
            mService = service;
        }
    }
}
