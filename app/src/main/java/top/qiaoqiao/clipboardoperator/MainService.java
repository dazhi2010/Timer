package top.qiaoqiao.clipboardoperator;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by dazhi on 2018-10-15.
 */
public class MainService extends Service{
    private ClipboardManager mClipboardManager;
    private static final String TAG = "MainService";
    List<String> stringList;
    ConstraintLayout toucherLayout;
    WindowManager.LayoutParams params;
    WindowManager windowManager;
    Configuration mConfiguration;
    Button imageButton1;
    Button imageButton2;
    Button countText;
    Button dropButton;
    ListView listView;
    //状态栏高度.
    int statusBarHeight = -1;
    boolean timerRunning = false;
    static final long periodMs = 33250;//计时周期
    int periodCount = 1;
    long startTime;
    private Timer timer;
    private TimerTask task;
    //不与Activity进行绑定.
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mConfiguration = this.getResources().getConfiguration();
        Log.i(TAG,"MainService Created");
        mClipboardManager = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        SharedPreferences myData = getSharedPreferences("myData",0);
        String wordsStr = myData.getString("words","");

        stringList =  new ArrayList<>(Arrays.asList(wordsStr.split("##")));
        createToucher();
    }

    @TargetApi(26)
    @SuppressLint("ClickableViewAccessibility")
    private void createToucher()
    {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        //Android8.0行为变更，对8.0进行适配https://developer.android.google.cn/about/versions/oreo/android-8.0-changes#o-apps
        Log.i(TAG,"(Build.VERSION.SDK_INT::::::" + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 23)
        {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        int ori = mConfiguration.orientation;
        SharedPreferences myData = getSharedPreferences("myData",0);
        String[] positionStr = myData.getString("position_"+ori,"0,0").split(",");
        params.x = Integer.parseInt(positionStr[0]);
        params.y = Integer.parseInt(positionStr[1]);

        //设置悬浮窗口长宽数据.
        params.width = 420;
        params.height = 80;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (ConstraintLayout) inflater.inflate(R.layout.activity_main,null);
        //添加toucherlayout
        windowManager.addView(toucherLayout,params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        imageButton1 = toucherLayout.findViewById(R.id.imageButton1);
        imageButton2 = toucherLayout.findViewById(R.id.imageButton2);
        countText =  toucherLayout.findViewById(R.id.countText);
        Button dropButton = toucherLayout.findViewById(R.id.dropButton);
        listView = toucherLayout.findViewById(R.id.listView);
        countText.setText("00");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item, stringList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                ClipData mClipData = ClipData.newPlainText("text",stringList.get(position));
                mClipboardManager.setPrimaryClip(mClipData);
                listView.setVisibility(View.INVISIBLE);
                params.height = 80;
                Toast.makeText(getApplicationContext(), "已复制到剪贴板："+stringList.get(position),Toast.LENGTH_SHORT).show();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                Toast.makeText(getApplicationContext(), stringList.get(position),Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        countText.setOnClickListener(new View.OnClickListener() {
            long[] hints = new long[2];
            @Override
            public void onClick(View v) {
                System.arraycopy(hints,1,hints,0,hints.length -1);
                hints[hints.length -1] = SystemClock.uptimeMillis();
                if (SystemClock.uptimeMillis() - hints[0] >= 200)
                {
                    if(imageButton2.getVisibility()==View.VISIBLE){
                        imageButton2.setVisibility(View.INVISIBLE);
                    }else{
                        imageButton2.setVisibility(View.VISIBLE);
                    }
                }else{
                    Log.i(TAG,"即将关闭");
                    stopSelf();
                }
            }
        });
        dropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listView.getVisibility()==View.VISIBLE){
                    listView.setVisibility(View.INVISIBLE);
                    params.height = 80;
                }else{
                    listView.setVisibility(View.VISIBLE);
                    params.height = 500;
                }
                windowManager.updateViewLayout(toucherLayout,params);
            }
        });
        imageButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                params.x = (int) event.getRawX() - 260;
                params.y = (int) event.getRawY() - 40 - statusBarHeight;
                windowManager.updateViewLayout(toucherLayout,params);
                SharedPreferences myData = getSharedPreferences("myData",0);
                SharedPreferences.Editor codeEditor = myData.edit();
                int ori = mConfiguration.orientation;
                codeEditor.putString("position_"+ori,params.x+","+params.y);
                codeEditor.commit();
                return false;
            }
        });
        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(timerRunning){
                    timer.cancel();
                    timer = null;
                    task.cancel();
                    task = null;
                }
                timerRunning = true;
                startTime = new Date().getTime();
                periodCount = 1;
                imageButton1.setText(periodCount+"");
                timer = new Timer();
                task = new TimerTask() {
                    @Override
                    public void run() {
                        long nowTime = new Date().getTime();
                        long remainMs = periodMs - ((nowTime-startTime)%periodMs);
                        if(remainMs<=250){
//                            startTime = nowTime;
                            periodCount ++;
                            imageButton1.setText(periodCount+"");
                        }
                        countText.setText((remainMs/1000+1)+"");
                        if(remainMs>=20000){
                            countText.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }else if(remainMs>=10000){
                            countText.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                        }else {
                            countText.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                    }
                };
                timer.schedule(task, 0, 250);
                imageButton2.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onDestroy()
    {
        if (imageButton1 != null)
        {
            windowManager.removeView(toucherLayout);
        }
        super.onDestroy();
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SharedPreferences myData = getSharedPreferences("myData",0);
        String[] positionStr = myData.getString("position_"+newConfig.orientation,"0,0").split(",");
        params.x = Integer.parseInt(positionStr[0]);
        params.y = Integer.parseInt(positionStr[1]);
        windowManager.updateViewLayout(toucherLayout,params);
    }
}
