package com.tjc.mina_demo.keepalive;

import com.tjc.mina_demo.ByteTools;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

/*
 *  心跳解码
 *
 */
public class KeepAliveMessageDecoder implements MessageDecoder {

    //数据包头部有多少字节
    private final int packHeadLength = 16;

    @Override
    public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
        if (in.remaining() > packHeadLength) {
            return MessageDecoderResult.NOT_OK;
        }
        if (in.remaining() < packHeadLength) {
            return MessageDecoderResult.NEED_DATA;
        }
        if (ByteTools.getChar(ByteTools.getDataLength(2, in)) == KeepAliveMessageBean.typeData) {
            int typeId = ByteTools.byteArrayToInt(ByteTools.getDataLength(4, in));
            if (typeId == KeepAliveFactory.client_alive_message || typeId == KeepAliveFactory.server_alive_message) {
                return MessageDecoderResult.OK;
            }
        }

        return MessageDecoderResult.NOT_OK;
    }

    @Override
    public MessageDecoderResult decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out) throws Exception {
        buffer.mark();//标记位置，以便数据有问题时还原

        char head = ByteTools.getChar(ByteTools.getDataLength(2, buffer));
        int dataId = ByteTools.byteArrayToInt(ByteTools.getDataLength(4, buffer));
        long date = ByteTools.bytesToLong(ByteTools.getDataLength(8, buffer));
        char finality = ByteTools.getChar(ByteTools.getDataLength(2, buffer));

        if (head == 'K' && finality == 'C') {
            KeepAliveMessageBean bean = new KeepAliveMessageBean();
            bean.setTypeId(dataId);
            bean.setDateHs(date);
            out.write(bean);
            return MessageDecoderResult.OK;
        }

        // 如果消息包不完整,将指针重新移动消息头的起始位置
        buffer.reset();
        return MessageDecoderResult.NEED_DATA;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {

    }
}
