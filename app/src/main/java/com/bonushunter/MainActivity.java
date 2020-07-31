package com.bonushunter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "isAccessibilityEnabled:" + Utils.isAccessibilityEnabled(this));
        Log.d(TAG, "isAccessibilitySettingsOn:" + Utils.isAccessibilitySettingsOn(this,
                BHAccessibilityService.class.getCanonicalName()));

//        if (!Utils.isAccessibilityEnabled(this) ||
////                !Utils.isAccessibilitySettingsOn(this,
////                        BHAccessibilityService.class.getCanonicalName())) {
////            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
////        }

        FloatWindow.getInstance(this.getApplicationContext()).requestPermissionIfNeed();
        FloatWindow.getInstance(this.getApplicationContext()).show();
    }
}