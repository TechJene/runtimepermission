package com.techjene.RuntimePermissionDemo;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZHENXINGYI2015 on 2016/12/29.
 */

public class BaseActivity extends AppCompatActivity {

    //定义PermissionListener的全局变量。因为两个方法都需要。
    private static PermissionListener mPermissionListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeAtivity(this);
    }

    //1.new一个requestRuntimePermission方法，可以接收一个permissions数组
    //还需要PermissionListener通知权限申请结果,即注册一个申请权限的回调监听。
    public static void requestRuntimePermission(String[] permissions, PermissionListener listener) {

        Activity topStackActivity = ActivityCollector.getStackTopActivity();
        if (topStackActivity == null){
            return;
        }


        //requestCode可以封装在内部。

        mPermissionListener = listener;

        List<String> permissionList = new ArrayList<String>();

        //1.首先执行一个循环，将permissions数组中所有的值取出来。在其内部执行判断。
        for (String permission : permissions) {

            if (ContextCompat.checkSelfPermission(topStackActivity, permission) != PackageManager.PERMISSION_GRANTED) {
                //如果当前循环的权限未被授权，则add 进 permissionList.
                permissionList.add(permission);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(topStackActivity, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {
            //your own logical
            //3.授权，调用onGranted
            mPermissionListener.onGranted();
        }
    }

    //2.重写onRequestPermissionsResult方法。
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                //1>判断
                if (grantResults.length > 0){
                    List<String> deniedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        int grantResult = grantResults[i];
                        String permission = permissions[i];

                        if (grantResult != PackageManager.PERMISSION_GRANTED){
                            //此时至少有一个权限未被授权
                            deniedPermissions.add(permission);
                        }
                    }

                    //判断是否为空
                    if (deniedPermissions.isEmpty()){
                        mPermissionListener.onGranted();
                    } else {
                        mPermissionListener.onDenied(deniedPermissions);
                    }
                    /*
                    如果for循环成功执行完，即没有在中间被return掉，就说明所有权限都被同意了。
                    此时就可以执行自己的逻辑了。
                     */
                }
                break;
            default:
                break;
        }
    }
}
