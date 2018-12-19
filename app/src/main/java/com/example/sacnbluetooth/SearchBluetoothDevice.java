package com.example.sacnbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SearchBluetoothDevice extends AppCompatActivity implements AdapterView.OnItemClickListener{

    private static final String TAG = SearchBluetoothDevice.class.getSimpleName();

    private BluetoothAdapter bluetoothAdapter = null;
    private Set<BluetoothDevice> setBluetoothDevice;
    private BluetoothDevice bluetoothDevice;
    private DiscoveryBluetoothBroadcast discoveryBroadcast;
    private IntentFilter DiscoveryFilter;
    private ProgressBar startBar;

    private Button searchButton;
    private ArrayAdapter<String> arrayAdapter;
    private ListView deviceListView;
    private List<String> deviceList = new ArrayList<String>();
    private Boolean state = true;//新设配标志
    private String message = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_layout);
        initView();
        initBluetooth();
    }
    private void initView(){
        startBar = (ProgressBar) findViewById(R.id.start_discovery_bar);
        searchButton = (Button) findViewById(R.id.start_search_button);
        searchButton.setOnClickListener(new searchButtonClick());
        arrayAdapter = new ArrayAdapter<String>(SearchBluetoothDevice.this,
                android.R.layout.simple_expandable_list_item_1,deviceList);
        deviceListView = (ListView) findViewById(R.id.device_ListView);
        deviceListView.setOnItemClickListener(this);
        deviceListView.setAdapter(arrayAdapter);
    }

    private void initBluetooth(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//蓝牙适配器
        setBluetoothDevice = bluetoothAdapter.getBondedDevices();//得到本地蓝牙集合
        discoveryBroadcast = new DiscoveryBluetoothBroadcast();

        DiscoveryFilter = new IntentFilter();
        DiscoveryFilter.addAction(BluetoothDevice.ACTION_FOUND);//添加发现设备广播
        DiscoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始搜索
        DiscoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);//结束搜索广播
        this.registerReceiver(discoveryBroadcast,DiscoveryFilter);
        if (bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
    }


    public class searchButtonClick implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: 开始搜索");
            if (bluetoothAdapter.isEnabled()){
                if (bluetoothAdapter.isDiscovering()){
                    Toast.makeText(SearchBluetoothDevice.this,"正在搜索，请不要重复点击",Toast.LENGTH_SHORT).show();
                }else {
                    deviceList.clear();
                    arrayAdapter.notifyDataSetChanged();
                    bluetoothAdapter.startDiscovery();//开始搜索
                }
            }else {
                Toast.makeText(SearchBluetoothDevice.this,"请打开蓝牙",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        message = deviceList.get(position);//得到设备信息
        String DialogTitle = "确认连接";
        String DialogOk = "连接";
        if (message.indexOf("已配对") > -1){//message中查找到"已配对"
            DialogTitle = "确认连接";
            DialogOk = "连接";
            state = false;
        }else if (message.indexOf("新设备") > -1){//message中查找到"新设备"
            DialogTitle = "确认配对";
            DialogOk = "配对";
            state = true;
        }
        message = message.substring(4);//截取第四个字符后面字符串
        //创建一个对话框，并设置标题内容
        AlertDialog.Builder dialog = new AlertDialog.Builder(SearchBluetoothDevice.this);
        dialog.setTitle(DialogTitle);
        dialog.setMessage(message);
        //dialog.setCancelable(true);//进制返回键
        dialog.setPositiveButton(DialogOk, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothMsg.BlueToothAddress = message.substring(message.length()-17);//得到硬件地址
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(BluetoothMsg.BlueToothAddress);//得到远程设备
                if (bluetoothAdapter.isDiscovering()){
                    bluetoothAdapter.cancelDiscovery();//正在搜索就关闭搜索
                    Toast.makeText(SearchBluetoothDevice.this,"搜索结束",Toast.LENGTH_SHORT).show();
                }
                if (state == true){//新设备要配对
                    try{
                        Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                        createBondMethod.invoke(bluetoothDevice);
                    }catch (Exception e){
                        Log.d(TAG, "onClick: 绑定失败");
                    }
                }else if (state == false){//旧设备要连接
                    if(BluetoothMsg.lastblueToothAddress!=BluetoothMsg.BlueToothAddress){
                        BluetoothMsg.lastblueToothAddress=BluetoothMsg.BlueToothAddress;
                    }
                    Intent intent = new Intent(SearchBluetoothDevice.this,BluetoothChat.class);
                    startActivity(intent);
                }
            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                BluetoothMsg.BlueToothAddress = null;
            }
        });
        dialog.show();//显示
    }

    //关于发现的广播
    public class DiscoveryBluetoothBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction().toString();
            int bondState;
            switch (action){
                case BluetoothDevice.ACTION_FOUND://发现设备
                    bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);//得到该设备
                    bondState = bluetoothDevice.getBondState();//获得状态
                    if (bondState == BluetoothDevice.BOND_NONE){
                        Log.d(TAG, "onReceive: "+bluetoothDevice.getName()+"被发现");
                        deviceList.add("新设备\n"+"名字:"+bluetoothDevice.getName()+"\n地址:"+bluetoothDevice.getAddress());
                        arrayAdapter.notifyDataSetChanged();
                    }else if(bondState == BluetoothDevice.BOND_BONDING){
                        Log.d(TAG, "onReceive: "+bluetoothDevice.getName()+"正在绑定");
                    }else if(bondState == BluetoothDevice.BOND_BONDED){
                        Log.d(TAG, "onReceive: "+bluetoothDevice.getName()+"已绑定");
                        deviceList.add("已配对\n"+"名字:"+bluetoothDevice.getName()+"\n地址:"+bluetoothDevice.getAddress());
                        arrayAdapter.notifyDataSetChanged();
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED://搜索结束
                    startBar.setVisibility(View.GONE);
                    searchButton.setText("开始搜索");
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED://搜索开始
                    startBar.setVisibility(View.VISIBLE);
                    searchButton.setText("正在搜索...");
                    break;
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(discoveryBroadcast);
    }
}
