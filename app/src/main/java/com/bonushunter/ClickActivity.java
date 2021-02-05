package com.bonushunter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.bonushunter.utils.CommonUtils;
import com.bonushunter.utils.LogUtils;

public class ClickActivity extends Activity {

    private static final String TAG = ClickActivity.class.getSimpleName();

    private TextView mETX;
    private TextView mETY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_click);

        mETX = findViewById(R.id.x);
        mETY = findViewById(R.id.y);
        findViewById(R.id.btn_start_tap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!CommonUtils.isAccessibilityEnabled(ClickActivity.this) ||
                        !CommonUtils.isAccessibilitySettingsOn(ClickActivity.this,
                                BHAccessibilityService.class.getCanonicalName())) {
                    // Check if enabled accessibility service

                    AlertDialog alertDialog = new AlertDialog.Builder(ClickActivity.this)
                            .setTitle("设置")
                            .setMessage("启动赏金猎人需开启无障碍服务，请前往设置中打开赏金猎人的无障碍服务许可")
                            .setNegativeButton("稍等", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                                }
                            })
                            .create();
                    alertDialog.show();
                    alertDialog.setCanceledOnTouchOutside(false);
                } else if (!Settings.canDrawOverlays(ClickActivity.this)) {
                    // check float permission
                    AlertDialog alertDialog = new AlertDialog.Builder(ClickActivity.this)
                            .setTitle("设置")
                            .setMessage("启动赏金猎人需开启悬浮窗功能，请前往设置中打开赏金猎人的悬浮窗功能许可")
                            .setNegativeButton("稍等", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("前往设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                            })
                            .create();
                    alertDialog.show();
                    alertDialog.setCanceledOnTouchOutside(false);
                } else {
                    ClickFloatWindow clickFloatWindow = ClickFloatWindow.getInstance(ClickActivity.this,
                            Integer.parseInt(mETX.getText().toString()), Integer.parseInt(mETY.getText().toString()));
                    clickFloatWindow.show();
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        LogUtils.d(TAG, "getX:" + event.getX() + ", getY:" + event.getY()
            + "， getRawX:" + event.getRawX() + ", getRawY:" + event.getRawY());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mETX.setText(Integer.toString((int)event.getRawX()));
            mETY.setText(Integer.toString((int)event.getRawY()));
        }
        return super.onTouchEvent(event);
    }
}