package com.techjene.RuntimePermissionDemo;

import java.util.List;

/**
 * Created by ZHENXINGYI2015 on 2016/12/29.
 */

public interface PermissionListener {

    void onGranted();

    //需要告知用户那些权限被拒绝了，添加参数。
    void onDenied(List<String>  deniedPermissions);
}
