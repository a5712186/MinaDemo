package com.tjc.mina_demo;


import com.tjc.mina_demo.entity.SendClientMessageBean;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ClientHandle extends IoHandlerAdapter {
    private final static Logger log = Logger.getLogger(ClientHandle.class.getSimpleName());
    private MinaService mServicfe;
    private List<SendClientMessageBean> cechaObj;

    public ClientHandle(MinaService context) {
        this.mServicfe = context;
        cechaObj = new ArrayList<>();
    }

    private boolean isListener(){
        return (null != mServicfe && null != mServicfe.getClientListener());
    }


    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        log.info("连接成功！");
        if(isListener()){
            mServicfe.getClientListener().connectOpened();
        }

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        log.info("关闭连接！");
        if(isListener()){
            mServicfe.getClientListener().connectClosed();
        }

    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        super.exceptionCaught(session, cause);
        log.info("连接异常！");
        if(isListener()){
            mServicfe.getClientListener().connectException(cause);
        }

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
//        log.debug("客户端收到消息" + message.toString());
        super.messageReceived(session, message);
//        if(message instanceof BaseMessageClient){
//            Message msg = new Message();
//            msg.arg1 = 1;
//            msg.obj = ((BaseMessageClient) message).getContent();
//            mHandler.sendMessage(msg);
//        }
        if(message instanceof SendClientMessageBean){
            SendClientMessageBean bean = (SendClientMessageBean) message;
            log.info("客户端收到消息" + bean.getContent());
            if(isListener()){
                if(cechaObj.size() > 0){
                    for (SendClientMessageBean objs : cechaObj)mServicfe.backMessage(objs);
                    cechaObj.clear();
                }
                mServicfe.backMessage(bean);
            }else{
                cechaObj.add(bean);
            }
        }


    }

//    @Override
//    public void messageSent(IoSession session, Object message) throws Exception {
////        log.debug("客户端发送消息" + message.toString());
//        super.messageSent(session, message);
//    }

}
