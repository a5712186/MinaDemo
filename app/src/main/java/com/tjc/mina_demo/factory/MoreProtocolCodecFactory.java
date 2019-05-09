package com.tjc.mina_demo.factory;

import com.tjc.mina_demo.entity.SendClientMessageBean;
import com.tjc.mina_demo.entity.SendServiceMessageBean;
import com.tjc.mina_demo.keepalive.KeepAliveMessageBean;
import com.tjc.mina_demo.keepalive.KeepAliveMessageDecoder;
import com.tjc.mina_demo.keepalive.KeepAliveMessageEncoder;

import org.apache.mina.filter.codec.demux.DemuxingProtocolCodecFactory;
/*
 * 多个编与解 码器添加
 */
public class MoreProtocolCodecFactory extends DemuxingProtocolCodecFactory{

    public MoreProtocolCodecFactory(){
        //心跳
        super.addMessageEncoder(KeepAliveMessageBean.class, KeepAliveMessageEncoder.class);
        super.addMessageDecoder(KeepAliveMessageDecoder.class);
        //客户发送给服务的
        super.addMessageEncoder(SendServiceMessageBean.class, ClientMessageEncoder.class);
        super.addMessageDecoder(ClientMessageDecoder.class);
        //服务发送给客户
        super.addMessageEncoder(SendClientMessageBean.class, ServiceMessageEncoder.class);
        super.addMessageDecoder(ServiceMessageDecoder.class);
    }

}
