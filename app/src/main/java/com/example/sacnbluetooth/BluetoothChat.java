package com.example.sacnbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothChat extends AppCompatActivity {

    public static final String PROTOCOL_SCHEME_RFCOMM = "service";//服务器名称

    private RecyclerView msgRecycleView;
    private BluetoothMsgAdapter msgAdapter;
    private int MsgType;
    private List<BluetoothMsg> msgList = new ArrayList<>();
    private Button disConnectButton;
    private Button msgSendButton;
    private EditText msgEditText;
    Context MyContext;

    private BluetoothServerSocket bluetoothServerSocket = null;
    private BluetoothSocket socket= null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDevice device= null;
    private ReadThread readThread= null;
    private ServerThread startServerThread= null;
    private ClientThread clientConnectThread= null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_layout);
        MyContext = this;
        init();
    }

    private void init(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        msgRecycleView = (RecyclerView) findViewById(R.id.msg_RecycleView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        msgRecycleView.setLayoutManager(linearLayoutManager);
        msgRecycleView.setScrollbarFadingEnabled(true);//设置快速滚动
        msgAdapter = new BluetoothMsgAdapter(msgList);
        msgRecycleView.setAdapter(msgAdapter);

        msgEditText = (EditText) findViewById(R.id.msgEditText);
        msgEditText.clearFocus();//去掉焦点

        msgSendButton = (Button) findViewById(R.id.msgSendButton);
        disConnectButton = (Button) findViewById(R.id.disConnectButton);
        msgSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = msgEditText.getText().toString();
                if (!message.equals("")){
                    SendMessageHandle(message);//发送信息
                    msgEditText.setText("");//清空
                    msgEditText.clearFocus();
                    //关闭输入法
                    InputMethodManager inputMethodManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromInputMethod(msgEditText.getWindowToken(),0);
                }else {
                    Toast.makeText(MyContext,"发送内容不能为空",Toast.LENGTH_SHORT).show();
                }
            }
        });
        disConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.CILENT){
                    shutDownClient();//关闭客户端
                    Toast.makeText(MyContext,"客户端已关闭",Toast.LENGTH_SHORT).show();
                }
                else if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.SERVICE){
                    shutDownServer();
                    Toast.makeText(MyContext,"服务端已关闭",Toast.LENGTH_SHORT).show();
                }
                finish();
                /*
                Log.d("11111", "onClick: ???????????????????");
                BluetoothMsg.isOpen = false;
                //BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.NONE;
                BluetoothMsg.BlueToothAddress = BluetoothMsg.lastblueToothAddress = null;*/
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (BluetoothMsg.isOpen){
            Toast.makeText(BluetoothChat.this,"连接已打开",Toast.LENGTH_SHORT).show();
            return;
        }
        if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.CILENT){
            String address = BluetoothMsg.BlueToothAddress;
            if (!address.equals("null")){
                device = bluetoothAdapter.getRemoteDevice(address);//得到远程设备
                clientConnectThread = new ClientThread();
                clientConnectThread.start();
                BluetoothMsg.isOpen = true;
            }else {
                Toast.makeText(BluetoothChat.this,"空地址",Toast.LENGTH_SHORT).show();
            }
        }else if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.SERVICE){
            startServerThread = new ServerThread();
            startServerThread.start();
            BluetoothMsg.isOpen = true;
        }
    }
    private Handler messageHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            BluetoothMsg bluetoothMsg = new BluetoothMsg((String) msg.obj,BluetoothMsg.TYPE_RECIVED);
            if (msg.what == 1){
                msgList.add(bluetoothMsg);
            }else {
                msgList.add(bluetoothMsg);
            }
            msgAdapter.notifyItemInserted(msgList.size()-1);//插入新消息
            msgRecycleView.scrollToPosition(msgList.size()-1);//定位到最后一行
        }
    };

    //开启客户端
    private class ClientThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                //创建socket连接，只需要在服务器注册时的UUID号，如下是蓝牙串口服务uuid
                socket = device.createInsecureRfcommSocketToServiceRecord(
                        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                //连接
                Message msg2 = new Message();
                msg2.obj = "请稍等，正在连接服务器...."+BluetoothMsg.BlueToothAddress;
                msg2.what = 0;
                messageHandle.sendMessage(msg2);

                socket.connect();

                Message msg = new Message();
                msg.obj = "已经连接上服务端，可以发送信息。";
                msg.what = 0;
                messageHandle.sendMessage(msg);

                //启动接收数据
                readThread = new ReadThread();
                readThread.start();
            }catch (Exception e){

            }
        }
    }
    //开启服务器
    private class ServerThread extends Thread{
        @Override
        public void run() {
            super.run();
            try{
                /*创建一个蓝牙服务器
                 *参数：服务器名，UUID
                 */
                bluetoothServerSocket =
                        bluetoothAdapter.listenUsingRfcommWithServiceRecord(PROTOCOL_SCHEME_RFCOMM,
                                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                Log.d("服务器", "等客户端连接... ");
                Message msg = new Message();
                msg.obj = "请稍等，正在等待客户端连接..";
                msg.what = 0;
                messageHandle.sendMessage(msg);

                /*接收客户端请求*/
                socket = bluetoothServerSocket.accept();
                Log.d("服务器", "客户端连接成功");

                Message msg2 = new Message();
                String info = "客户端已经连接上！可以发送信息。";
                msg2.obj = info;
                msg.what = 0;
                messageHandle.sendMessage(msg2);

                //启动接收数据
                readThread = new ReadThread();
                readThread.start();
            }catch (Exception e){

            }
        }
    }
    //读取信息
    private class ReadThread extends Thread{
        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[1024];
            int bytes;
            InputStream inputStream = null;

            try{
                inputStream = socket.getInputStream();
            }catch (IOException e){
                Log.d("ReadThread", "run: 读取输入流错误");
                e.printStackTrace();
            }
            while(true){
                try{
                    //读取输入流
                    if ( (bytes = inputStream.read(buffer)) >0){
                        byte[] buf_data = new byte[bytes];
                        for (int i = 0; i < bytes; i++){
                            buf_data[i] = buffer[i];
                        }
                        String s = new String(buf_data);
                        Message msg = new Message();
                        msg.obj = s;
                        msg.what = 1;
                        messageHandle.sendMessage(msg);
                    }
                }catch (IOException e){
                    try{
                        inputStream.close();
                    }catch (IOException e1){
                        e1.printStackTrace();
                    }
                    break;
                }
            }//while
        }//run
    }
    //发送信息
    private void SendMessageHandle(String msg){
        BluetoothMsg bluetoothMsg = new BluetoothMsg(msg,BluetoothMsg.TYPE_SENT);
        if (socket == null){
            Toast.makeText(BluetoothChat.this,"没有连接",Toast.LENGTH_SHORT).show();
            return;
        }
        try{
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(msg.getBytes());
        }catch (IOException e){
            e.printStackTrace();
        }
        msgList.add(bluetoothMsg);
        msgAdapter.notifyItemInserted(msgList.size()-1);//插入新消息
        msgRecycleView.scrollToPosition(msgList.size()-1);//定位到最后一行
        msgAdapter.notifyDataSetChanged();
    }
    //关闭客户端
    private void shutDownClient(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                if (clientConnectThread != null){
                    clientConnectThread.interrupt();
                    clientConnectThread = null;
                }
                if (readThread != null){
                    readThread.interrupt();
                    readThread = null;
                }
                if (socket != null){
                    try{
                        socket.close();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    socket = null;
                }
            }
        }.start();
    }
    //关闭服务端
    private void shutDownServer(){
        new Thread() {
            @Override
            public void run() {
                if(startServerThread != null)
                {
                    startServerThread.interrupt();
                    startServerThread = null;
                }
                if(readThread != null)
                {
                    readThread.interrupt();
                    readThread = null;
                }
                try {
                    if(socket != null)
                    {
                        socket.close();
                        socket = null;
                    }
                    if (bluetoothServerSocket != null)
                    {
                        bluetoothServerSocket.close();/* 关闭服务器 */
                        bluetoothServerSocket = null;
                    }
                } catch (IOException e) {
                    Log.d("服务端", "run: 服务端关闭异常");
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.CILENT)
        {
            shutDownClient();
        }
        else if (BluetoothMsg.serviceOrCilent == BluetoothMsg.ServerOrCilent.SERVICE)
        {
            shutDownServer();
        }
        BluetoothMsg.isOpen = false;
       // BluetoothMsg.serviceOrCilent = BluetoothMsg.ServerOrCilent.NONE;
    }
}

