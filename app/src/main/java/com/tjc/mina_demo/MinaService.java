package com.tjc.mina_demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.tjc.mina_demo.entity.JsonMessage;
import com.tjc.mina_demo.entity.SendClientMessageBean;
import com.tjc.mina_demo.entity.SendServiceMessageBean;

import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/*
 *  与服务器通信
 *
 */
public class MinaService extends Service implements ServiceMsgListener.servicelListener {

    //连接管理
    private MinaSocketManage manage;
//    绑定服务集合
    public HashMap<String, ServiceMsgListener.clientListener> clientListener;
//    标志位
    private boolean isStartConnect = false;
    //当前绑定activity
    public String currentActivity;

    /*
     * 获取当前活动的activity接口
     */
    public ServiceMsgListener.clientListener getClientListener() {
        if (clientListener.containsKey(currentActivity)) {
            return clientListener.get(currentActivity);
        }
        return null;
    }


    //用消息队列发送消息到服务器
    private HandlerThread sendThread2 = new HandlerThread("send-thread");
    private Handler mHandler;
    private void setSendQueue(){
        if(null != mHandler){
            return;
        }
        sendThread2.start();
        mHandler = new Handler(sendThread2.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                //发送
                if(null != msg.obj){

                    manage.sendMessage(msg.obj);

                }
            }
        };
    }


    @Override
    public void onCreate() {
        super.onCreate();
        manage = MinaSocketManage.getInstance();
        clientListener = new HashMap<>();

    }

    @Override
    public void onDestroy() {
        //释放资源
        sendThread2.quit() ;
        clientListener.clear();

        manage.coletSocket();
        super.onDestroy();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
         return new MyIBinder();
    }

    //收到服务消息
    public void backMessage(SendClientMessageBean msg) {
        if (null == getClientListener()) {
            return;
        }
        //返回消息记录
        if (null != msg && msg.getMsgTypeId() == 666) {
            List<JsonMessage> listMsg = new ArrayList<>();
            if(msg.getContent().contains("^")){
                String[] contes = msg.getContent().split("\\^");
                for (int i = 0;i<contes.length;i++){
                    Log.d("KKKKK","记录："+ contes[i]);
                    JsonMessage objMsg = manage.getGosn().fromJson(contes[i], JsonMessage.class);
                    listMsg.add(objMsg);
                }
            }else{
                JsonMessage objMsg = manage.getGosn().fromJson(msg.getContent(), JsonMessage.class);
                listMsg.add(objMsg);
            }
            getClientListener().messageList(listMsg);
            return;
        }
        //服务返回消息
        if (null != msg && !TextUtils.isEmpty(msg.getContent())) {
            try {
                getClientListener().messageReceived(manage.getGosn().fromJson(msg.getContent(), JsonMessage.class));
            } catch (Exception ee) {
                getClientListener().jsonError(ee);

            }

        }
    }

    //发送消息
    @Override
    public void sendMessage(Object msg) {
        //发消息到服务器
        if (null == msg) {
            return;
        }
        try {
            String content = manage.getGosn().toJson(msg);
            SendServiceMessageBean bean = new SendServiceMessageBean();
            bean.setDataId(100);
            bean.setCodeId(manage.getCodeValue(content));
            bean.setContent(content);
            mHandler.sendMessage(mHandler.obtainMessage(1,bean));

        } catch (Exception ee) {
            ee.printStackTrace();
            if (getClientListener() != null) {
                getClientListener().jsonError(ee);
            }
        }

    }



    @Override
    public void closedMessage(String tag) {
        //activity销毁时，调用，关闭请求返回
        if (null != clientListener) {
            clientListener.remove(tag);
        }
    }

    @Override
    public void currentActivity(String activity) {
        //更新当前活动activity
        this.currentActivity = activity;
    }

    @Override
    public boolean isConnect() {
        //连接是否成功
        return manage.isSocket();
    }

    //连接服务
    @Override
    public void restConnect() {
        if (isStartConnect) {
            return;
        }
        if (!manage.isSocket()) {
            isStartConnect = true;
            Observable.create(manage.getConnectServer(this)).subscribeOn(Schedulers.io())//被观察者在子线程中处理
                    .observeOn(AndroidSchedulers.mainThread())//将结果发送到主线程中处理
                    .subscribe(new Observer<IoSession>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                        }

                        @Override
                        public void onNext(IoSession s) {
                            setSendQueue();
                        }

                        @Override
                        public void onError(Throwable e) {
                            isStartConnect = false;
                            e.printStackTrace();
                            if (null != getClientListener()) {
                                getClientListener().connectError();
                            }

                        }

                        @Override
                        public void onComplete() {
                            isStartConnect = false;
                        }
                    });
        }
    }


    //绑定后，交换接口
    public class MyIBinder extends Binder {
        ServiceMsgListener.servicelListener getListener() {
            return MinaService.this;
        }

        void setListener(String tag, ServiceMsgListener.clientListener listener) {
//            clientListener = listener;
            currentActivity = tag;
            clientListener.put(tag, listener);
            restConnect();
        }
    }
}
