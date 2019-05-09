package com.tjc.mina_demo;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tjc.mina_demo.entity.JsonMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements View.OnClickListener, View.OnLayoutChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    //聊天信息容器
    private LinearLayout mContent;
    //输入框
    private EditText editBody;
    //滑动
    private ScrollView mScrollView;
    //时间格式化
    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.CHINA);

    //使用Handler 防异线程修改UI
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.arg1) {
                case 1:
                    if (msg.obj instanceof JsonMessage) {
                        addViewText((JsonMessage) msg.obj);
                    }
                    break;
                case 2:
                    Toast.makeText(MainActivity.this,String.valueOf(msg.obj),Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    mServiceListener.restConnect();
                    break;
            }
            return true;
        }
    });

    //不想写listView暂时动态添加就好
    private void addViewText(JsonMessage body) {
        if (null != mContent) {

            View view = LayoutInflater.from(this).inflate(R.layout.item_im_layout, null, false);
            if(mContent.getChildCount()%2 == 0){
                view.setBackgroundColor(getResources().getColor(R.color.nb_read_bg_1));
            }else{
                view.setBackgroundColor(getResources().getColor(R.color.nb_read_bg_2));
            }
            ((TextView) view.findViewById(R.id.user_content)).setText(body.getContent());
            String datetime = sdf.format(new Date(body.getDate()));
            ((TextView) view.findViewById(R.id.user_name)).setText(body.getUserName() + " " + datetime + "发布:");
            mContent.addView(view);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //添加监听器，监听根布局
        findViewById(R.id.main_root_view).addOnLayoutChangeListener(this);

        findViewById(R.id.main_but_send).setOnClickListener(this);
        mContent = findViewById(R.id.main_content);
        editBody = findViewById(R.id.main_edit_body);
        mScrollView = findViewById(R.id.main_scroll);

        //判断网络，没网就退出
        if(!isNetworkConnected()){
            Toast.makeText(this,"无网络",Toast.LENGTH_LONG).show();
            finish();
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_but_send://发送消息
                String body = editBody.getText().toString();
                if (TextUtils.isEmpty(body)) {
                    return;
                }

                JsonMessage msg = new JsonMessage();
                msg.setType(1);
                msg.setContent(body);
                msg.setDate(System.currentTimeMillis());
                if(!TextUtils.isEmpty(android.os.Build.BRAND)){
                    msg.setUserName(android.os.Build.BRAND + " 手机");
                }else{
                    msg.setUserName("未知 手机");
                }


                mServiceListener.sendMessage(msg);
                editBody.getText().clear();
                break;
//            case R.id.main_but_connect:
//                mServiceListener.restConnect();
//                break;
        }

    }

    //判断网络连通
    private boolean isNetworkConnected() {

        ConnectivityManager mConnectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }

        return false;
    }


    @Override
    public void connectError() {
        //连接失败
        super.connectError();
        if(!isNetworkConnected()){
            finish();
            return;
        }
        Message msg1 = new Message();
        msg1.arg1 = 2;
        msg1.obj = "连接失败";
        mHandler.sendMessage(msg1);


        //重联
        mHandler.sendEmptyMessageDelayed(3,3000);


    }

    @Override
    public void messageReceived(JsonMessage msg) {
        super.messageReceived(msg);
        //收到消息
        Message msg1 = new Message();
        msg1.arg1 = 1;
        msg1.obj = msg;
        mHandler.sendMessage(msg1);

    }

    @Override
    public void messageList(List<JsonMessage> msg) {
        super.messageList(msg);
        //收到消息记录
        for (JsonMessage ee : msg) {
            Message msg1 = new Message();
            msg1.arg1 = 1;
            msg1.obj = ee;
            mHandler.sendMessage(msg1);
//            mHandler.sendMessageAtTime(msg1, 200);
        }
    }


    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        //根布局变化，将滑动到最底到
        mContent.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }
}
