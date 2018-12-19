package com.example.sacnbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothInit extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemClickListener{

    private static final String TAG = BluetoothInit.class.getSimpleName();
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice bluetoothDevice = null;
    private Button openButton;
    private Button closeButton;
    private Button pairButton;
    private Button discoverableButton;
    private Button serviceButton;
    private Button clientButton;
    Timer timer;
    private ArrayAdapter<String> arrayAdapter;
    private ListView deviceListview;
    private List<String> deviceList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.init_layout);
        initView();
        initBluetooth();
    }
    //初始化控件
    private void initView(){
        openButton = (Button) findViewById(R.id.open_bluetooth);
        closeButton = (Button) findViewById(R.id.close_bluetooth);
        pairButton = (Button) findViewById(R.id.pair_bluetooth);
        discoverableButton = (Button) findViewById(R.id.discoverable_bluetooth);
        serviceButton = (Button) findViewById(R.id.service_bluetooth);
        clientButton = (Button) findViewById(R.id.client_bluetooth);
        //设置Button监听事件
        openButton.setOnClickListener(this);
        closeButton.setOnClickListener(this);
        pairButton.setOnClickListener(this);
        discoverableButton.setOnClickListener(this);
        serviceButton.setOnClickListener(this);
        clientButton.setOnClickListener(this);


        //ListView 布局
        arrayAdapter = new ArrayAdapter<String>(BluetoothInit.this,
                android.R.layout.simple_expandable_list_item_1,deviceList);
        deviceListview = (ListView) findViewById(R.id.pair_devices_listview);
        deviceListview.setAdapter(arrayAdapter);
        deviceListview.setOnItemClickListener(this);//设置ListView监听事件
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    //检测蓝牙
    private void initBluetooth(){
        if (bluetoothAdapter != null){
            Log.d(TAG, "onClick: 设备支持蓝牙");
        }else {
            Log.d(TAG, "onClick: 无蓝牙设备");
            return;
        }
    }
    @Override//Button点击事
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.open_bluetooth:
                if (bluetoothAdapter.isEnabled()){
                    Log.d(TAG, "onClick: 已打开蓝牙");
                    Toast.makeText(BluetoothInit.this,"蓝牙已打开，请不要重复点击",Toast.LENGTH_SHORT).show();
                    deviceList.clear();//清空设备列表
                    arrayAdapter.notifyDataSetChanged();//更新
                }else {
                    Log.d(TAG, "onClick: 正在打开蓝牙");
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);//需要BLUETOOTH权限
                    startActivityForResult(intent,RESULT_OK);
                    deviceList.clear();//清空设备列表
                    arrayAdapter.notifyDataSetChanged();//更新
                }
                break;
            case R.id.close_bluetooth:
                if (bluetoothAdapter.isEnabled()){
                    Log.d(TAG, "onClick: 正在关闭蓝牙");
                    Toast.makeText(BluetoothInit.this,"蓝牙已关闭",Toast.LENGTH_SHORT).show();
                    bluetoothAdapter.disable();//需要BLUETOOTH_ADMIN权限
                    Log.d(TAG, "onClick: 蓝牙已关闭");
                }else {
                    Log.d(TAG, "onClick: 蓝牙已关闭");
                    Toast.makeText(BluetoothInit.this,"蓝牙已关闭，请不要重复点击",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.pair_bluetooth:
                if (bluetoothAdapter.isEnabled()){
                    Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices();//得到本地蓝牙集合
                    deviceList.clear();
                    for (BluetoothDevice device:devices){
                        Log.d(TAG, "MainActivity: "+"名字:"+device.getName()+"\n地址:"+device.getAddress());//得到设备名字和地址
                        deviceList.add("名字:"+device.getName()+"\n地址:"+device.getAddress());
                        arrayAdapter.notifyDataSetChanged();//更新数据
                    }
                }else {
                    Toast.makeText(BluetoothInit.this,"蓝牙未打开",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.discoverable_bluetooth:
                Log.d(TAG, "onClick: 可被搜索");
                isDiscoveriableDialog();
                break;
            case R.id.service_bluetooth:
                Log.d(TAG, "onClick: 服务端");
                BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.SERVICE;//决定是server还是client
                Intent service = new Intent(BluetoothInit.this,SearchBluetoothDevice.class);
                startActivity(service);
                break;
            case R.id.client_bluetooth:
                Log.d(TAG, "onClick: 客户端");
                BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.CILENT;//决定是server还是client
                Intent client = new Intent(BluetoothInit.this,SearchBluetoothDevice.class);
                startActivity(client);
                break;
        }
    }
    @Override//ListView点击事件
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final String message = deviceList.get(position);//得到标签信息
        //创建一个对话框，并设置标题内容
        AlertDialog.Builder dialog = new AlertDialog.Builder(BluetoothInit.this);
        dialog.setTitle("确认连接");
        dialog.setMessage(message);
        //dialog.setCancelable(true);//进制返回键
        dialog.setPositiveButton("连接", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.CILENT;
                BluetoothMsg.BlueToothAddress = message.substring(message.length()-17);
                if(BluetoothMsg.lastblueToothAddress!=BluetoothMsg.BlueToothAddress){
                    BluetoothMsg.lastblueToothAddress=BluetoothMsg.BlueToothAddress;
                }
                Intent intent = new Intent(BluetoothInit.this,BluetoothChat.class);
                startActivity(intent);
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        dialog.show();//显示
    }
    //可被搜索提示数据
    private void isDiscoveriableDialog(){
        //创建一个对话框，并设置标题内容
        AlertDialog.Builder dialog = new AlertDialog.Builder(BluetoothInit.this);
        dialog.setTitle("蓝牙权限请求");
        dialog.setMessage("此应用请求让其他蓝牙设备在120秒内搜索到您的手机。允许该程序执行相应操作吗？");
        dialog.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent discoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);//请求搜索
                discoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120);//可被搜索120s
                startActivity(discoverable);//开始
                Log.d(TAG, "onClick: 123");
                timer = new Timer();
                TimerTask task = new TimerTask() {
                    int discoverableSecond = 120;
                    @Override
                    public void run() {
                        discoverableSecond--;
                        Message message = new Message();
                        if (discoverableSecond <= 0){
                            message.what = 0;
                        }else {
                            message.what = 1;
                            message.obj = discoverableSecond;
                        }
                        handler.sendMessage(message);
                    }
                };
                timer.schedule(task,0,1000);//0s延时后，每隔一秒执行一次
                Log.d(TAG, "onClick: 1234");

            }
        });
        dialog.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(BluetoothInit.this,"拒绝搜索", Toast.LENGTH_SHORT).show();
                return;
            }
        });
        dialog.show();//显示
    }
    //开放检测的秒数，进行ui操作
    final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    discoverableButton.setText("开放检测"+msg.obj+"s");
                    break;
                case 0:
                    discoverableButton.setText("开放检测120s");
                    if (timer != null){
                        timer.cancel();
                        timer = null;
                    }
                    Toast.makeText(BluetoothInit.this,"开放检测结束", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
