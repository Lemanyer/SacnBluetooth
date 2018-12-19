package com.example.sacnbluetooth;

/**
 * Created by Amarao on 2017/10/23.
 */

public class BluetoothMsg {

    /*****消息界面*****/
    public static final int TYPE_RECIVED = 1;
    public static final int TYPE_SENT = 0;
    private String content;
    private int type;
    /*****蓝牙消息*****/
    //蓝牙连接类型
    public enum ServerOrCilent{
        NONE,
        SERVICE,
        CILENT
    }
    //蓝牙连接身份
    public static ServerOrCilent serviceOrCilent = ServerOrCilent.NONE;
    //连接蓝牙地址
    public static String BlueToothAddress = null,lastblueToothAddress=null;
    //通信线程是否开启
    public static boolean isOpen = false;

    public BluetoothMsg(String content,int type){
        this.content = content;
        this.type = type;
    }
    public String GetContent(){
        return content;
    }
    public int GetType(){
        return type;
    }
}