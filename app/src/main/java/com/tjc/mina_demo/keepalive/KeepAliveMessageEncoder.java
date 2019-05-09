package com.tjc.mina_demo.keepalive;

import com.tjc.mina_demo.ByteTools;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import java.util.logging.Logger;

/*
 *   心跳编码包
 *
 *   K.99.8888.C
 *   标识码.ID.时间.结尾
 *
 */
public class KeepAliveMessageEncoder implements MessageEncoder<KeepAliveMessageBean> {
    private final static Logger log = Logger.getLogger(KeepAliveMessageEncoder.class.getSimpleName());

    @Override
    public void encode(IoSession session, KeepAliveMessageBean message, ProtocolEncoderOutput out) throws Exception {

        byte[] type_arr = ByteTools.getBytes(KeepAliveMessageBean.typeData);
        byte[] id_arr = ByteTools.intToButeArray(message.getTypeId());
        byte[] date_arr = ByteTools.longToBytes(message.getDateHs());
        byte[] finality_arr = ByteTools.getBytes(KeepAliveMessageBean.finality);

        IoBuffer buffer = IoBuffer.allocate( 4 + id_arr.length + date_arr.length).setAutoExpand(true);

        buffer.put(type_arr);
        buffer.put(id_arr);
        buffer.put(date_arr);
        buffer.put(finality_arr);
        buffer.flip();
        out.write(buffer);
        out.flush();

        buffer.free();
    }
}
