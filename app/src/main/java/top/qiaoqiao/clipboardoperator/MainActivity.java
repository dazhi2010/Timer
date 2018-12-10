package top.qiaoqiao.clipboardoperator;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> stringList;
    ArrayAdapter<String> adapter;
    ListView listView;
    Button addBtn;
    Button startButton;
    final MainActivity thisActivity = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startFloatWindow();
        setContentView(R.layout.word_edit_activity);
        listView = (ListView) findViewById(R.id.listViewEdit);
        addBtn = (Button) findViewById(R.id.addButton);
        startButton =  (Button) findViewById(R.id.startButton);
        SharedPreferences myData = getSharedPreferences("myData",0);
        String wordsStr = myData.getString("words","");
        stringList =  new ArrayList<>(Arrays.asList(wordsStr.split("##")));
        adapter= new ArrayAdapter<String>(this, R.layout.list_item, stringList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
                alert_edit(position,stringList.get(position));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
                Toast.makeText(getApplicationContext(), "长按置顶",Toast.LENGTH_SHORT).show();
                String str = stringList.remove(position);
                stringList.add(0,str);
                saveWords();
                return true;
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MainService.class);
                stopService(intent);
                startFloatWindow();
            }
        });
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alert_edit(-1,"");
            }
        });
    }
    public void alert_edit(final int index, String str){
        final EditText et = new EditText(this);
        et.setText(str);
        new AlertDialog.Builder(this).setTitle("请输入")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(index<0){
                            stringList.add(0,et.getText().toString());
                        }else{
                            stringList.set(index,et.getText().toString());
                        }
                        saveWords();
                    }
                }).setNeutralButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AlertDialog.Builder(thisActivity).setTitle("确认删除吗？--"+stringList.get(index))
                                .setIcon(android.R.drawable.ic_dialog_info)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        stringList.remove(index);
                                        saveWords();
                                    }
                                })
                                .setNegativeButton("取消",null).show();
                    }
        }).setNegativeButton("取消",null).show();
    }
    private void saveWords(){
        adapter.notifyDataSetChanged();
        String resultStr = "";
        for (String s:stringList) {
            resultStr = resultStr+s+"##";
        }
        SharedPreferences myData = getSharedPreferences("myData",0);
        SharedPreferences.Editor codeEditor = myData.edit();
        codeEditor.putString("words",resultStr);
        Toast.makeText(getApplicationContext(), "已保存，重启悬浮窗生效",Toast.LENGTH_SHORT).show();
        codeEditor.commit();
    }
    private void startFloatWindow(){
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(MainActivity.this, MainService.class);
                Toast.makeText(MainActivity.this,"已开启Toucher",Toast.LENGTH_SHORT).show();
                startService(intent);
    //                finish();
            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Toast.makeText(MainActivity.this,"需要取得权限以使用悬浮窗",Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }
        } else {
            //SDK在23以下，不用管.
            Intent intent = new Intent(MainActivity.this, MainService.class);
            startService(intent);
    //            finish();
        }
    }
}
