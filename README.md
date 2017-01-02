# runtimepermission
Android 运行时权限的学习代码。

 - 课程目标：对运行时权限进行合理的封装。
 - 封装原因：简化代码，降低工作量。

**难点：运行时权限和activity耦合性非常高**
 

 - 弹出运行时权限申请对话框需要基于activity。
 - 比较好处理的是，requestPermissions需要传进activity实例作为参数。
 因为把它封装起来，独立到一个方法中，可以在独立的方法中设置一个activity参数，然后让调用者把activity参数传进来即可。
 - 	最难处理的是onRequestPermissionResult()方法。
	它是override了Activity类里的一个方法，如果把该方法独立到一个新类里，新类不继承Activity，则此方法无法重写。

####**郭老师提供了三种封装方案：**
###1. 建立一个PermissionActivity
因为其和Activity高度耦合，则建立一个PermissionActivity，去封装运行时权限的所有操作，	然后把该activity设置为一个透明的activity，即不做其他任何操作，专门用于运行时权限处理。
	当用户处理完运行时权限弹出框后，再finish掉该透明activity。
###2.参考[RxPermissions开源库](https://github.com/tbruyelle/RxPermissions)
 实际上是模拟了RxJava的语法进行了运行时权限的封装。
####i>用法比较简单：
首先，new一个RxPermission的实例，并传参，参数必须是activity。
然后，调用其request方法申请权限，再调用subscribe方法执行订阅，
	之后就可以进行判断了。
####ii>源码简析
#####1>创建一个RxPermissionsFragment方法继承自Fragment，因为在Fragment也可以申请运行时权限。
参考官网说明:[Fragment](https://developer.android.com/reference/android/support/v4/app/Fragment.html)
有一个requestPermissions方法
![这里写图片描述](http://img.blog.csdn.net/20170102090002304?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvWWFveXVhbmRlbWVpbGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
还有一个onRequestPermissionsResult
![这里写图片描述](http://img.blog.csdn.net/20170102090029342?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvWWFveXVhbmRlbWVpbGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
也就是说，Fragment既可以申请运行时权限，也可以处理。

接着看RxPermissions代码：

![这里写图片描述](http://img.blog.csdn.net/20170102090522609?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvWWFveXVhbmRlbWVpbGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

RxPermissionsFragment方法中提供了一个resultPermissions方法，调用父类Fragment的requestPermissions方法，去请求权限。

```
void requestPermissions(@NonNull String[] permissions) {
	requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
}
```
然后重写了onRequestPermissionsResult方法，去处理结果的回调，并进行下一次的回调处理。

```
public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
	super.onRequestPermissionsResult(requestCode, permissions, grantResults);

	if (requestCode != PERMISSIONS_REQUEST_CODE) return;

	boolean[] shouldShowRequestPermissionRationale = new boolean[permissions.length];

	for (int i = 0; i < permissions.length; i++) {
		shouldShowRequestPermissionRationale[i] = shouldShowRequestPermissionRationale(permissions[i]);
	}

	onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale);
}
```
说明该RxPermissionsFragment是没有界面的，没有大小，不占用界面空间。

#####2>RxPermissions类分析
在RxPermissions方法中，传一个activity进来，并调用了getRxPermissionsFragment()方法。

```
public RxPermissions(@NonNull Activity activity) {
	mRxPermissionsFragment = getRxPermissionsFragment(activity);
}
```

getRxPermissionsFragment()方法中：
new出了rxPermissionsFragment并将其添加到activity这个类中。

```
private RxPermissionsFragment getRxPermissionsFragment(Activity activity) {
	RxPermissionsFragment rxPermissionsFragment = findRxPermissionsFragment(activity);
	boolean isNewInstance = rxPermissionsFragment == null;
	if (isNewInstance) {
		rxPermissionsFragment = new RxPermissionsFragment();
		FragmentManager fragmentManager = activity.getFragmentManager();
		fragmentManager
				.beginTransaction()
				.add(rxPermissionsFragment, TAG)
				.commit();
		fragmentManager.executePendingTransactions();
	}
	return rxPermissionsFragment;
}
```
一切的运行时权限处理都是在RxPermissionsFragment中进行的。

###3.建立一个BaseActivity继承自AppCompatActivity
这也是郭老师重点讲的方案，并且实现简单。
让需要申请权限的activity继承BaseActivity即可。
郭老师一句话说得很不错：

> 几乎所有的项目都会有自己的BaseActivity，因为直接继承Android的Activity，则代码就很难扩展。

这是整体的思路：

![这里写图片描述](http://img.blog.csdn.net/20170102173137520?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvWWFveXVhbmRlbWVpbGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

接下来说实现过程
####第一步：把运行时权限的代码封装进BaseActivity中。

1.new一个requestRuntimePermission方法，可以接收一个permissions数组。

 	1> new 一个permissionList 
 	2>执行一个循环，将permissions数组中所有的值取出来。在其内部执行判断。

```
public static void requestRuntimePermission(String[] permissions) {
	//requestCode可以封装在内部。

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
	}
}
```

2.重写onRequestPermissionsResult方法。用于回调用户授权行为。

```
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
	switch (requestCode){
		case 1:
			//1>判断
			if (grantResults.length > 0){
				for (int grantResult : grantResults) {
					if (grantResult != PackageManager.PERMISSION_GRANTED){
						//此时至少有一个权限未被授权
						Toast.makeText(MainActivity.this, "某个权限被拒绝了", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				/*
				如果for循环成功执行完，即没有在中间被return掉，就说明所有权限都被同意了。
				此时就可以执行自己的逻辑了。
				 */
				defineLogical();
			}
			break;
		default:
			break;
	}
}
```

####第二步：新建一个接口用于通知申请的activity。
执行到所有权限都被授权时，需要写一个回调机制。
1.新建一个PermissionListener接口
![这里写图片描述](http://img.blog.csdn.net/20170102162743997?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvWWFveXVhbmRlbWVpbGk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

```
public interface PermissionListener {

    void onGranted();

    //需要告知用户那些权限被拒绝了，添加参数。
    void onDenied(List<String>  deniedPermissions);
}
```
因为结果只有两种，所以只需定义两个方法。授权&拒绝
由于用户拒绝的权限数量是不确定的，所以需要添加参数，用于把被拒绝的权限情况回调给调用方。

####第三步：再次修改BaseActivity
1.在requestRuntimePermission方法参数中传入监听接口
因为需要PermissionListener通知权限申请结果，即注册一个申请权限的回调监听。

```
//还需要PermissionListener通知权限申请结果,即注册一个申请权限的回调监听。
public static void requestRuntimePermission(String[] permissions, PermissionListener listener) {...}
```

2.定义PermissionListener的全局变量。

```
//定义PermissionListener的全局变量。因为两个方法都需要。
private static PermissionListener mPermissionListener;
```
3.调用PermissionListener接口。
1>在requestRuntimePermission方法中，授权了，就在else中granted

```
//3.授权，调用onGranted
mPermissionListener.onGranted();
```
2>在onRequestPermissionResult中，在授权拒绝后，还需要把情况回调给调用方。
new一个数组，用于存放被拒绝的权限。

```
List<String> deniedPermissions = new ArrayList<>();
```
通过方法中的参数permissions回调权限授权情况。
因为需要数组的下标i，而foreach无法完成，故改为fori循环，并在循环内部进行判断。

```
for (int i = 0; i < grantResults.length; i++) {
	int grantResult = grantResults[i];
	String permission = permissions[i];

	if (grantResult != PackageManager.PERMISSION_GRANTED){
		//此时至少有一个权限未被授权
		deniedPermissions.add(permission);
	}
}
```
循环执行完之后，再判断一下deniedPermissions是否为空。

```
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
```
####第四步：调用封装
在MainActivity的onClick方法中，调用requestRuntimePermission。
传入需要申请的权限，并新建一个PermissionListener的回调接口。

```
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
```
####第五步：进一步优化
目前为止还有一些限制，只有在Activity方法中才能调用。	
进行下一步优化，使得能在任何地方调用。这样就不能实例化了，需改为静态方法。

改为static之后，需要解决BaseActivity中的ContextCompat和ActivityCompat的this，Activity实例问题。
**1.建一个Activity的管理类ActivityCollector。**
1>封装一个List，管理所有Activity。
2>提供addActivity和removeActivity方法。
3>提供一个获取当前栈顶activity的方法。

```
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
```
**2.修改BaseActivity**
1>添加OnCreate方法用于add活动

```
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityCollector.addActivity(this);
}
```
2>添加OnDestroy方法用于remove活动

```
@Override
protected void onDestroy() {
    super.onDestroy();
    ActivityCollector.removeAtivity(this);
}
```
3>在requestRuntimePermission方法中获取当前栈顶activity。

```
public static void requestRuntimePermission(String[] permissions, PermissionListener listener) {

	Activity topStackActivity = ActivityCollector.getStackTopActivity();
	if (topStackActivity == null){
		return;
	}
	...
}
```
将获取的topActivity传入需要this的方法。
**注意：申请的权限务必在AndroidManifest中声明。**

这样，到目前为止在任何一个类中申请运行时权限，
只需调用BaseActivity的静态方法requestRuntimePermission()，并传入需要申请的权限即可。


