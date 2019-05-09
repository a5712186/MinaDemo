package com.tjc.mina_demo;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tjc.mina_demo.entity.SendClientMessageBean;
import com.tjc.mina_demo.factory.MoreProtocolCodecFactory;
import com.tjc.mina_demo.keepalive.KeepAliveFactory;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.compression.CompressionFilter;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.zip.CRC32;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/*
 *    Mina连接控制
 */
public class MinaSocketManage {

    private static class SingletonClassInstance{
        private static final MinaSocketManage manage = new MinaSocketManage();
    }

    public static MinaSocketManage getInstance(){
        return SingletonClassInstance.manage;
    }

    private IoSession mSession;

    private Gson gson;

    private CRC32 crc;

    //gson
    public Gson getGosn(){
        if(null == gson){
            gson = new GsonBuilder().create();
        }
        return gson;
    }

    public boolean isSocket(){
        if(null != mSession && mSession.isConnected()){
            return true;
        }
        return false;
    }

    public void coletSocket(){
        if(isSocket()){
            mSession.closeOnFlush();
        }
    }

    public void sendMessage(Object msg){
        if(isSocket()){
            WriteFuture futur = mSession.write(msg);
        }
    }

    //异步发送，需要线程，
//    public synchronized boolean sendAsynchronousMessage(Object msg) throws InterruptedException {
////        mSession.getConfig().setUseReadOperation(true);
//         WriteFuture futur = mSession.write(msg);
////        futur.await(3000);
//        futur.awaitUninterruptibly(); // 等待发送数据操作完成
////        if(futur.isWritten()){
////            ReadFuture readFuture = mSession.read();
////            //等待消息响应
////            readFuture.awaitUninterruptibly();
////            //是否响应成功
////            if(readFuture.isRead()){
////                //获取消息
////                Object message = readFuture.getMessage();
////                if(message instanceof SendClientMessageBean){
////                    Log.d("sendAsynchronousMessage","客户端xxx收到消息" + ((SendClientMessageBean) message).getContent());
////
////                }
////            }
////            mSession.getConfig().setUseReadOperation(false);
////        }
//        return futur.isWritten();
//    }



    //获取CRC码
    public long getCodeValue(String content){
        if(null == crc){
            crc = new CRC32();
        }
        crc.reset();
        crc.update(content.getBytes(Charset.defaultCharset()));
        return crc.getValue();
    }


    public ObservableOnSubscribe<IoSession> getConnectServer(final MinaService mHandler){
        return new ObservableOnSubscribe<IoSession>() {
            @Override
            public void subscribe(ObservableEmitter<IoSession> e) throws Exception {
                try {
                    NioSocketConnector mSocketConnector = new NioSocketConnector();


                    //新建线程池处理逻辑
                    mSocketConnector.getFilterChain().addLast("threadPool", new ExecutorFilter(Executors.newCachedThreadPool()));


                    //压缩消息
                    mSocketConnector.getFilterChain().addLast("zip", new CompressionFilter());

                    //设置协议封装解析处理
//                    mSocketConnector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new SocketCodeFactory()));
                    mSocketConnector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(new MoreProtocolCodecFactory()));


                    //设置心跳包
                    KeepAliveFilter heartFilter = new KeepAliveFilter(new KeepAliveFactory());
                    //每 5 分钟发送一个心跳包
                    heartFilter.setRequestInterval(90);
                    //心跳包超时时间 10s
                    heartFilter.setRequestTimeout(30);

                    mSocketConnector.getFilterChain().addLast("heartbeat", heartFilter);


                    // 获取过滤器链
//                    DefaultIoFilterChainBuilder filterChain = mSocketConnector.getFilterChain();
//                    filterChain.addLast("encoder", new ProtocolCodecFilter(new SocketCodeFactory()));
//                    // 添加编码过滤器 处理乱码、编码问题
//                    filterChain.addLast("decoder", new ProtocolCodecFilter(new SocketCodeFactory()));

                    //设置 handler 处理业务逻辑
                    mSocketConnector.setHandler(new ClientHandle(mHandler));
//                    mSocketConnector.addListener(new HeartBeatListener(mSocketConnector));
                    //配置服务器地址
                    InetSocketAddress mSocketAddress = new InetSocketAddress("www.mtmatjc.cn", 8888);
                    //连接超时
                    mSocketConnector.setConnectTimeoutMillis(3000);
                    //发起连接
                    ConnectFuture mFuture = mSocketConnector.connect(mSocketAddress);
                    // 等待是否连接成功，相当于是转异步执行为同步执行。
                    mFuture.awaitUninterruptibly();
                    if(mFuture.isConnected()){
                        mSession = mFuture.getSession();
                        Log.d("", "======连接成功" + mSession.toString());
                        e.onNext(mSession);
                        mSession.getCloseFuture().awaitUninterruptibly();
                    }else{
                        e.onError(new Throwable("连接失败"));
                    }
                    mSocketConnector.dispose();
                } catch (Exception es) {
                    e.onError(es);
                }
                e.onComplete();
            }
        };
    }

}
