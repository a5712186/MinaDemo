package com.tjc.mina_demo.keepalive;

import android.util.Log;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;

/*
 *   当空闲时，服务器主动发心跳，需要回应心跳否则会关闭连接
 */
public class KeepAliveFactory implements KeepAliveMessageFactory{
    private static final String TAG = KeepAliveFactory.class.getSimpleName();

    public static final int server_alive_message = 3;//客户发给服务
    public static final int client_alive_message = 4;//服务发给客户

    private long server_packetIdCode = 0;

    @Override
    public boolean isRequest(IoSession session, Object message) {
        // TODO Auto-generated method stub
        if (message instanceof KeepAliveMessageBean) {
            KeepAliveMessageBean basemessage = (KeepAliveMessageBean) message;
            // 心跳包方法协议类型
            if (basemessage.getTypeId() == client_alive_message) {
                // 为4，代表是一个心跳包，
                if(server_packetIdCode == basemessage.getDateHs()){
                    Log.d(TAG,"isRequest服务器返回，心跳数据正常,");
                }else{
                    Log.d(TAG,"isRequest服务器返回，心跳数据不正常,");
                }


                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isResponse(IoSession session, Object message) {
        if (message instanceof KeepAliveMessageBean) {
            KeepAliveMessageBean basemessage = (KeepAliveMessageBean) message;
            // 心跳包方法协议类型
            if (basemessage.getTypeId() == client_alive_message) {
                // 为4，代表是一个心跳包，
//                server_packetIdCode = basemessage.getDateHs();

//                Log.d(TAG,"isResponse服务器返回，表示收到,不回，否则死循环");

                return true;
            }
        }
        return false;
    }

    @Override
    public Object getRequest(IoSession session) {
        // TODO 服务器主动发心跳，回应
        return getMessage(server_alive_message);

    }

    @Override
    public Object getResponse(IoSession session, Object request) {
        // TODO 组装消息内容，返回给服务端
        return null;
    }

    private Object getMessage(int type) {

        KeepAliveMessageBean bean = new KeepAliveMessageBean();
        bean.setTypeId(type);
        server_packetIdCode = System.currentTimeMillis();
        bean.setDateHs(server_packetIdCode);
        return bean;

//        BaseMessageClient msge = new BaseMessageClient();
//        msge.setMsgTypeId(type);
//        msge.setContent("心跳");
//        // 校验码生成
//        CRC32 crc32 = new CRC32();
//        crc32.update(msge.getContent().getBytes());
//        // crc校验码
//        msge.setCrcCode(crc32.getValue());
//        // 数据包标识码
//        if(server_packetIdCode == 0) {
//            server_packetIdCode = 10003;
//        }
//        msge.setCrcCodeId(server_packetIdCode);
//        return msge;
    }
}
