package com.techjene.RuntimePermissionDemo;

import java.util.List;

/**
 * Created by ZHENXINGYI2015 on 2016/12/29.
 */

public class Util {

    public void test(){
        BaseActivity.requestRuntimePermission(new String[]{}, new PermissionListener() {
            @Override
            public void onGranted() {

            }

            @Override
            public void onDenied(List<String> deniedPermissions) {

            }
        });
    }

}
