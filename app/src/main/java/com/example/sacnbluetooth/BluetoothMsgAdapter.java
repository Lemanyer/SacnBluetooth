package com.example.sacnbluetooth;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Amarao on 2017/10/31.
 */

public class BluetoothMsgAdapter extends RecyclerView.Adapter<BluetoothMsgAdapter.ViewHolder> {

    private List<BluetoothMsg> msgList;

    public BluetoothMsgAdapter(List<BluetoothMsg> msg){
        msgList = msg;
    }
    public class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout sendLayout;
        LinearLayout receiveLayout;
        TextView sendMsg;
        TextView receiveMsg;

        public ViewHolder(View itemView) {
            super(itemView);
            sendLayout = (LinearLayout) itemView.findViewById(R.id.send_layout);
            receiveLayout = (LinearLayout) itemView.findViewById(R.id.receive_layout);
            sendMsg = (TextView) itemView.findViewById(R.id.send_msg);
            receiveMsg = (TextView) itemView.findViewById(R.id.receive_msg);
        }
    }

    //创建ViewHolder,动态加载布局
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.msg_layout,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //对RecycleView子项数据赋值，子项滚到屏幕内执行
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BluetoothMsg msg = msgList.get(position);//得到当前进入屏幕的RecycleView
        if (msg.GetType() == BluetoothMsg.TYPE_SENT){
            //发送消息时，隐藏接收消息框，显示发送消息框
            holder.receiveLayout.setVisibility(View.GONE);
            holder.sendLayout.setVisibility(View.VISIBLE);
            holder.sendMsg.setText(msg.GetContent());
        }else if (msg.GetType() == BluetoothMsg.TYPE_RECIVED){
            //接收消息时，隐藏发送消息框，显示接收消息框
            holder.receiveLayout.setVisibility(View.VISIBLE);
            holder.sendLayout.setVisibility(View.GONE);
            holder.receiveMsg.setText(msg.GetContent());
        }
    }

    //返回RecycleView长度
    @Override
    public int getItemCount() {
        return msgList.size();
    }
}
