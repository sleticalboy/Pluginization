package com.sleticalboy.pluginization;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

/**
 * Created on 20-10-24.
 *
 * @author binlee sleticalboy@gmail.com
 */
public final class NoNameActivity extends BaseActivity {

    private static final String TAG = "NoNameActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
    }
}
