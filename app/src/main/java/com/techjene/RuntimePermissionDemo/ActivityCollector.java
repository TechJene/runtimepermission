package com.techjene.RuntimePermissionDemo;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZHENXINGYI2015 on 2016/12/29.
 */

public class ActivityCollector {

    //封装一个list用于管理当前所有activity
    private static List<Activity> activityList = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public static void removeAtivity(Activity activity) {
        activityList.remove(activity);
    }

    //提供一个获取当前栈顶activity的方法
    public static Activity getStackTopActivity() {
        if (activityList.isEmpty()) {
            return null;
        } else {
            return activityList.get(activityList.size() - 1);//栈顶
        }
    }
}
