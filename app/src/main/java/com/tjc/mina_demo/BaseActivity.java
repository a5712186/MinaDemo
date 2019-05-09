package com.tjc.mina_demo;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tjc.mina_demo.entity.JsonMessage;

import java.util.List;

public class BaseActivity extends AppCompatActivity implements ServiceConnection,ServiceMsgListener.clientListener {

    public ServiceMsgListener.servicelListener mServiceListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, MinaService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        //绑定
        MinaService.MyIBinder myBinder = (MinaService.MyIBinder) service;
//        Log.d(,"绑定");
        myBinder.setListener(this.getClass().getSimpleName(),this);
        this.mServiceListener = myBinder.getListener();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        //解除绑定
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(null != mServiceListener){
            //当前正在运行activity
            mServiceListener.currentActivity(this.getClass().getSimpleName());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(null != mServiceListener){
            //销毁当前可返回请求列表
            mServiceListener.closedMessage(this.getClass().getSimpleName());
        }
        unbindService(this);
    }

    @Override
    public void connectException(Throwable cause) {
         //连接异常
    }

    @Override
    public void connectClosed() {
//         连接关闭
    }

    @Override
    public void connectOpened() {
//        连接成功
    }

    @Override
    public void connectError() {
//        连接错误
    }

    @Override
    public void messageReceived(JsonMessage msg) {
//         接收服务消息
    }

    @Override
    public void messageList(List<JsonMessage> msg) {
//         接收服务记录
    }

    @Override
    public void sendMessageSuccess() {
//         发送成功
    }

    @Override
    public void sendMessageFail() {
//         发送失败
    }

    @Override
    public void jsonError(Throwable cause) {
//          转换JSON失败
    }
}
