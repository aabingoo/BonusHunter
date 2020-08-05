package com.bonushunter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.bonushunter.apps.AppRobotFactory;
import com.bonushunter.apps.IAppRobot;
import com.bonushunter.task.FindOneAndClickTask;
import com.bonushunter.utils.AppRobotUtils;
import com.bonushunter.utils.CommonUtils;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Map;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ImageView mImageView;

    private RadioGroup mAppsRG;
    private Button mStartBtn;
    private String mSelectedPkgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAppsRG = findViewById(R.id.rb_app_list);
        mStartBtn = findViewById(R.id.btn_start);

        // init APP list
        initAppList();
        mAppsRG.setOnCheckedChangeListener(this);
        mStartBtn.setOnClickListener(this);



//        Log.d(TAG, "isAccessibilityEnabled:" + Utils.isAccessibilityEnabled(this));
//        Log.d(TAG, "isAccessibilitySettingsOn:" + Utils.isAccessibilitySettingsOn(this,
//                BHAccessibilityService.class.getCanonicalName()));
//
////        if (!Utils.isAccessibilityEnabled(this) ||
//////                !Utils.isAccessibilitySettingsOn(this,
//////                        BHAccessibilityService.class.getCanonicalName())) {
//////            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
//////        }
//
//        FloatWindow.getInstance(this.getApplicationContext()).requestPermissionIfNeed();
//        FloatWindow.getInstance(this.getApplicationContext()).show();

//        staticLoadCVLibraries();
//
//        staticLoadCVLibraries();
//        mImageView = findViewById(R.id.img);
////        String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
////        Log.d(TAG, "sdcardPath:" + sdcardPath);
////
//        final Bitmap xigua = BitmapFactory.decodeResource(getResources(), R.drawable.xigua_home);
//        mImageView.setImageBitmap(xigua);
//        final Bitmap xigua_fudai = BitmapFactory.decodeResource(getResources(), R.drawable.xigua_fudai);
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Mat source = new Mat();
//                org.opencv.android.Utils.bitmapToMat(xigua, source);
//                Mat temp = new Mat();
//                org.opencv.android.Utils.bitmapToMat(xigua_fudai, temp);
//
//                Mat ret = Mat.zeros(source.rows() - temp.rows() + 1,
//                        source.cols() - temp.cols() + 1, CvType.CV_32FC1);
//                Imgproc.matchTemplate(source, temp, ret, Imgproc.TM_SQDIFF_NORMED);
//

//                Core.normalize(ret, ret, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//                Log.d(TAG, "r:" + ret.rows() + ", c:" + ret.cols());
//                double limit = 0.1;
//                int diff = 10;
//                List<Point> hitPoints = new ArrayList<>();
//                for (int i = 0; i < ret.rows(); i++) { //y
//                    for (int j = 0; j < ret.cols(); j++) { //x
//                        double matchValue = ret.get(i, j)[0];
////                        Log.d(TAG, "matchValue:" + matchValue);
//                        if (matchValue < limit) {
//                            boolean isAlreadyHit = false;
//                            for (Point hitPoint: hitPoints) {
////                                int r = temp.width() > temp.height() ? temp.width()/2 : temp.height()/2;
////                                r += 10;
////                                if (((hitPoint.x - j) * (hitPoint.x - j) + (hitPoint.y - i) * (hitPoint.y - i))
////                                        < r * r) {
////
////                                    isAlreadyHit = true;
////                                    continue;
////                                }
//
//                                if (hitPoint.x-diff  <= j && j <= hitPoint.x + temp.width()+diff
//                                        && hitPoint.y-diff <= i && i <= hitPoint.y + temp.height()+diff) {
////                                    j +=
//                                    isAlreadyHit = true;
//                                    continue;
//                                }
//                            }
//                            if (!isAlreadyHit) {
//                                hitPoints.add(new Point(j, i));
//                                Log.d(TAG, "find match: x:" + j + ", y:" + i + ", mat:" + matchValue);
//                                Imgproc.rectangle(source, new Rect((int)j, (int)i, temp.width(), temp.height()), new Scalar(0, 255, 0), 5);
//                            }
//                        }
//                    }
//                }
//
//
//                Core.normalize(ret, ret, 0, 1, Core.NORM_MINMAX, -1);
//                Core.MinMaxLocResult mlr = Core.minMaxLoc(ret);
//                Point matchLoc = mlr.minLoc;
//                NumberFormat nf = NumberFormat.getInstance();
//                nf.setMaximumFractionDigits(20);
//                nf.setGroupingUsed(false);
//                Log.d(TAG, "findView x:" + matchLoc.x + ", y:" + matchLoc.y + ", value:" + nf.format(mlr.minVal)
//                        + ", ma:" + (mlr.minVal < 0));
//                Imgproc.rectangle(source, new Rect((int)matchLoc.x, (int)matchLoc.y,
//                        temp.width(), temp.height()), new Scalar(100, 255, 0, 0), 5);
//                org.opencv.android.Utils.matToBitmap(source, xigua);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mImageView.setImageBitmap(xigua);
//                    }
//                });
//            }
//        }).start();






//        Core.normalize(ret, ret, 0, 1, Core.NORM_MINMAX, -1);
//        Core.MinMaxLocResult mlr = Core.minMaxLoc(ret);
//        Point matchLoc = mlr.minLoc;
//        Log.d(TAG, "x:" + matchLoc.x + ", y:" + matchLoc.y);
//
//
//
////        Imgproc.rectangle(source, new Point(100, 100), new Point(100 + 100, 100 + 100), );
//
//        Imgproc.rectangle(source, new Rect((int)matchLoc.x, (int)matchLoc.y, temp.width(), temp.height()), new Scalar(0, 255, 0), 5);

//        Imgproc.rectangle(source, matchLoc, new Point(matchLoc.x + temp.width(), matchLoc.y + temp.height()), new Scalar(111, 111, 111));




    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initAppList() {
        Iterator iterator = AppRobotUtils.sSupportApps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iterator.next();
            String name = entry.getKey();
            String pkgName = entry.getValue();
            Log.d(TAG, "name:" + name + ", pkgName:" + pkgName);

            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(name);
            radioButton.setTag(pkgName);
            mAppsRG.addView(radioButton);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.d(TAG, "onCheckedChanged - id:" + checkedId);
        RadioButton radioButton = group.findViewById(checkedId);
        if (radioButton.isPressed()) {
            Log.d(TAG, "name:" + radioButton.getText() + ", pkg:" + radioButton.getTag());
            mSelectedPkgName = (String) radioButton.getTag();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            if (TextUtils.isEmpty(mSelectedPkgName)) {
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("通知")
                        .setMessage("请先选择一款应用进行启动")
                        .setPositiveButton("知道了", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertDialog.show();
                alertDialog.setCanceledOnTouchOutside(false);
            } else
            if (!CommonUtils.isAccessibilityEnabled(this) ||
                    !CommonUtils.isAccessibilitySettingsOn(this,
                            BHAccessibilityService.class.getCanonicalName())) {
                // Check if enabled accessibility service

                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("设置")
                        .setMessage("启动赏金猎人需开启无障碍服务，请前往设置中打开赏金猎人的无障碍服务许可")
                        .setNegativeButton("再考虑下", new DialogInterface.OnClickListener() {
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
            } else
                if (!Settings.canDrawOverlays(this)) {
                // check float permission
                AlertDialog alertDialog = new AlertDialog.Builder(this)
                        .setTitle("设置")
                        .setMessage("启动赏金猎人需开启悬浮窗功能，请前往设置中打开赏金猎人的悬浮窗功能许可")
                        .setNegativeButton("再考虑下", new DialogInterface.OnClickListener() {
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
                FloatWindow floatWindow = FloatWindow.getInstance(this);
                floatWindow.setAppRobot(AppRobotFactory.getAppRobot(this, mSelectedPkgName));
                floatWindow.show();
            }
        }
    }

    private void staticLoadCVLibraries() {
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.d(TAG, "Open CV Libraries loaded...");
        }
    }

}