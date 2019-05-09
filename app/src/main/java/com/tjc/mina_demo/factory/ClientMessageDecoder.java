package com.tjc.mina_demo.factory;

import com.tjc.mina_demo.ByteTools;
import com.tjc.mina_demo.entity.SendServiceMessageBean;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import java.nio.charset.Charset;
import java.util.zip.CRC32;

/*
 *  解码客户端发送数据
 */
public class ClientMessageDecoder implements MessageDecoder {
    //数据包头部有多少字节
    private final int packHeadLength = 16;
    @Override
    public MessageDecoderResult decodable(IoSession session, IoBuffer in) {
        if (in.remaining() < packHeadLength) {
            return MessageDecoderResult.NEED_DATA;
        }else{
            if(ByteTools.getChar(ByteTools.getDataLength(2,in)) == SendServiceMessageBean.dataType){
                if(ByteTools.byteArrayToInt(ByteTools.getDataLength(4,in)) == 100){
                    return MessageDecoderResult.OK;
                }
            }
        }
        return MessageDecoderResult.NOT_OK;
    }

    @Override
    public MessageDecoderResult decode(IoSession session, IoBuffer buffer, ProtocolDecoderOutput out) throws Exception {
        buffer.mark();//标记位置，以便数据有问题时还原

        char type = ByteTools.getChar(ByteTools.getDataLength(2,buffer));
        int id = ByteTools.byteArrayToInt(ByteTools.getDataLength(4,buffer));
        int length = ByteTools.byteArrayToInt(ByteTools.getDataLength(4,buffer));
        String content = new String(ByteTools.getDataLength(length - packHeadLength,buffer), Charset.defaultCharset());
        long code = ByteTools.bytesToLong(ByteTools.getDataLength(8,buffer));
        char tail = ByteTools.getChar(ByteTools.getDataLength(2,buffer));

        CRC32 crc = new CRC32();
        crc.update(content.getBytes(Charset.defaultCharset()));
        //验证校验码
        if (code == crc.getValue() && tail == SendServiceMessageBean.finality) {
            SendServiceMessageBean message = new SendServiceMessageBean();
            message.setDataId(id);
            message.setDataLength(length);
            message.setContent(content);
            message.setCodeId(code);
            out.write(message);
            return MessageDecoderResult.OK;
        }

//        if(ByteTools.getChar(ByteTools.getDataLength(2,buffer)) == StringMessageBean.dataType){
//
//
//            if (message.getDataLength() >= packHeadLength && message.getDataLength() - 4 <= buffer.remaining()){
//
//
//
//
//
//
//
//            }
//        }
        // 如果消息包不完整,将指针重新移动消息头的起始位置
        buffer.reset();
        return MessageDecoderResult.NEED_DATA;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {

    }
}
