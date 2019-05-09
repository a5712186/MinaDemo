package com.tjc.mina_demo.factory;

import com.tjc.mina_demo.ByteTools;
import com.tjc.mina_demo.entity.SendClientMessageBean;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.apache.mina.filter.codec.demux.MessageDecoder;
import org.apache.mina.filter.codec.demux.MessageDecoderResult;

import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.zip.CRC32;

public class ServiceMessageDecoder implements MessageDecoder {
    private final static Logger log = Logger.getLogger(ServiceMessageDecoder.class.getSimpleName());
    //数据包头部有多少字节
    private final int packHeadLength = 42;

    @Override
    public MessageDecoderResult decodable(IoSession session, IoBuffer buf) {
        if (buf.remaining() < packHeadLength) {
            return MessageDecoderResult.NEED_DATA;
        }else{
            String packhead = new String(ByteTools.getDataLength(11, buf), Charset.defaultCharset());
//            log.info("包头：" + packhead);
            if (SendClientMessageBean.packHandler.equals(packhead)) {
                int length = ByteTools.byteArrayToInt(ByteTools.getDataLength(4, buf));
//                log.info("数据长度：" + length);
                if (length >= packHeadLength && length - 22 <= buf.remaining()) {
                    return MessageDecoderResult.OK;
                }
            }
        }
        return MessageDecoderResult.NOT_OK;
    }

    @Override
    public MessageDecoderResult decode(IoSession session, IoBuffer buf, ProtocolDecoderOutput out) throws Exception {

        if (buf.remaining() >= packHeadLength) {
            buf.mark();//标记位置，以便数据有问题时还原
            String packhead = new String(ByteTools.getDataLength(11, buf), Charset.defaultCharset());
            log.info("包头：" + packhead);
            if (SendClientMessageBean.packHandler.equals(packhead)) {
                int length = ByteTools.byteArrayToInt(ByteTools.getDataLength(4, buf));
                log.info("数据长度：" + length);
                if (length >= packHeadLength && length - 22 <= buf.remaining()) {
                    int funcid = ByteTools.byteArrayToInt(ByteTools.getDataLength(4, buf));
                    log.info("协议类型：" + funcid);
                    long packetIdCode = ByteTools.bytesToLong(ByteTools.getDataLength(8, buf));
                    log.info("数据包标识码：" + packetIdCode);
                    //读取报文正文内容
                    int oldLimit = buf.limit();
                    //当前读取的位置 + 总长度  - 前面读取的字节长度 - 校验码
                    buf.limit(buf.position() + length - packHeadLength);
                    String content = buf.getString(Charset.defaultCharset().newDecoder());
                    buf.limit(oldLimit);
                    log.info("报文正文内容：" + content);
                    CRC32 crc = new CRC32();
                    crc.update(content.getBytes(Charset.defaultCharset()));
                    //读取校验码 8个字节
                    long checkcode = ByteTools.bytesToLong(ByteTools.getDataLength(8, buf));
                    log.info("校验码：" + checkcode);
                    //验证校验码
                    if (checkcode != crc.getValue()) {
                        // 如果消息包不完整,将指针重新移动消息头的起始位置
                        buf.reset();
                        return MessageDecoderResult.NEED_DATA;
                    }
                    //读取包尾 2个字节
                    String packtail = new String(ByteTools.getDataLength(11, buf), Charset.defaultCharset());
                    log.info("包尾：" + packtail);
                    if (!SendClientMessageBean.packTail.equals(packtail)) {
                        // 如果消息包不完整,将指针重新移动消息头的起始位置
                        buf.reset();
                        return MessageDecoderResult.NEED_DATA;
                    }
                    SendClientMessageBean message = new SendClientMessageBean();
                    message.setDataLength(length);
                    message.setCrcCode(checkcode);
                    message.setMsgTypeId(funcid);
                    message.setCrcCodeId(packetIdCode);
                    message.setContent(content);
                    out.write(message);

                    return MessageDecoderResult.OK;

                }
            }
        }

        //数据不完整，或者有问题，还原，继续累积数据
        buf.reset();
        return MessageDecoderResult.NEED_DATA;
    }

    @Override
    public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {

    }
}
