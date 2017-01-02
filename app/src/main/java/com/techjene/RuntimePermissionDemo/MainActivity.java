package com.techjene.RuntimePermissionDemo;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.techjene.permissiondemo.R;

import java.util.List;

public class MainActivity extends BaseActivity {

    private Button makeCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        File file = getExternalFilesDir("");
//        //如果参数为空，则为根目录。如果指定的目录不存在，则新建。
//        file.getPath();

        makeCall = (Button) findViewById(R.id.make_call);
        makeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestRuntimePermission(new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, new PermissionListener() {
                    @Override
                    public void onGranted() {
                        Toast.makeText(MainActivity.this, "所有权限申请通过", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenied(List<String> deniedPermissions) {
                        for (String permission : deniedPermissions) {
                            Toast.makeText(MainActivity.this, permission + " 权限被拒绝", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void call() {
        /*添加trycatch的原因：
        在上面的onClick方法中，在if else语句中进行了完整的运行时权限验证，所以代码是安全的。
         但是单独的call()方法，有可能在不安全的位置被调用。
         */
        try {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:10086"));
            startActivity(intent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void defineLogical() {
        Toast.makeText(MainActivity.this, "所有权限申请通过", Toast.LENGTH_SHORT).show();
    }
}
