package ai.guiji.duix.sdk.client.pro.demo.ui.activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseActivity extends AppCompatActivity implements Handler.Callback {

    public final String TAG = getClass().getName();
    protected BaseActivity mContext;
    protected Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        HandlerThread mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper(), this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
        if (mHandler != null && mHandler.getLooper() != null) {
            mHandler.getLooper().quit();
        }
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        onMessage(msg);
        return false;
    }

    // try abstract
    protected void onMessage(@NonNull Message msg) {

    }

    protected void fullscreen() {
        // 全屏显示，隐藏状态栏和导航栏，拉出状态栏和导航栏显示一会儿后消失。
        WindowInsetsControllerCompat windowInsetsController = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars()); // 隐藏状态栏
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars()); // 隐藏导航栏
            // 与 hide() 结合 后, 从隐藏栏的屏幕边缘滑动，系统栏会再次显示且会在一段时间后再次自动隐藏
            windowInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            // 与 hide() 结合 后, 从隐藏栏的屏幕边缘滑动后，会固定显示；isAppearanceLightStatusBars 设置为 false，状态栏才是浅色
            // windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_SWIPE
            // 与 hide() 结合 后, 从隐藏栏的屏幕边缘滑动后，会固定显示；isAppearanceLightStatusBars 设置为 false，状态栏才是浅色
            // windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

    }

    // 在onWindowFocusChanged回到中调用该函数
    protected void focusChangeFullScreen(boolean focus) {
        if (focus) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    protected void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private String[] mRequestPermissions;
    private int mRequestPermissionCode;
    ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            result -> {
                boolean hasDeny = false;
                for (String permission : mRequestPermissions) {
                    if (null == permission) {
                        continue;
                    }
                    if (ContextCompat.checkSelfPermission(mContext, permission) !=
                            PackageManager.PERMISSION_GRANTED) {
                        hasDeny = true;
                    }
                }
                if (hasDeny) {
                    permissionsGet(false, mRequestPermissionCode);
                } else {
                    permissionsGet(true, mRequestPermissionCode);
                }
            });

    //申请权限
    public void requestPermission(String[] permissions, int code) {
        if (null == permissions) {
            permissionsGet(true, code);
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            permissionsGet(true, code);
            return;
        }
        mRequestPermissions = permissions;
        mRequestPermissionCode = code;
        List<String> requestPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(mContext, permission) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions.add(permission);
            }
        }
        if (0 != requestPermissions.size()) {
            String[] permissionArray = new String[requestPermissions.size()];
            for (int i = 0; i < requestPermissions.size(); i++) {
                permissionArray[i] = requestPermissions.get(i);
            }
            permissionLauncher.launch(permissionArray);
        } else {
            permissionsGet(true, mRequestPermissionCode);
        }
    }

    //申请权限回调
    public void permissionsGet(boolean get, int code) {

    }

    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
