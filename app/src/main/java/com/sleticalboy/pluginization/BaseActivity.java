package com.sleticalboy.pluginization;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {
        // hook startActivityForResult()
        Log.d(TAG, "startActivityForResult() called with: intent = [" + intent
                + "], requestCode = [" + requestCode + "], options = [" + options + "]");
        super.startActivityForResult(intent, requestCode, options);
    }
}
