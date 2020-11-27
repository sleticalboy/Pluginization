package com.binlee.pluginization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.binlee.pluginization.util.Hooks;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        // hook startActivityForResult()
        Log.d(TAG, "startActivityForResult() called with: intent = [" + intent
                + "], requestCode = [" + requestCode + "], options = [" + options + "]");
        super.startActivityForResult(intent, requestCode, options);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        Hooks.hookActivityTaskManager(this);
    }
}
