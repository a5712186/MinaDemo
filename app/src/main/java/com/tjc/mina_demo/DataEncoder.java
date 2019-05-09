package com.tjc.mina_demo;

import com.tjc.mina_demo.entity.SendClientMessageBean;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import java.nio.charset.Charset;
import java.util.logging.Logger;

public class DataEncoder extends ProtocolEncoderAdapter {
    private final static Logger log = Logger.getLogger(CharsetDataEncoder.class.getSimpleName());
    private Charset charset;

    public DataEncoder(Charset charset) {
        this.charset = charset;
    }

    @Override
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        if (message instanceof SendClientMessageBean) {
            // 协议内容
            SendClientMessageBean msg = (SendClientMessageBean) message;
            //将内容转成byte 获取长度
            byte[] packhead_arr = SendClientMessageBean.packHandler.getBytes(charset);
            byte[] funcid_arr = ByteTools.intToButeArray(msg.getMsgTypeId());
            byte[] packetIdCode_arr = ByteTools.longToBytes(msg.getCrcCodeId());
            byte[] content_arr = msg.getContent().getBytes(charset);
            byte[] checkcode_arr = ByteTools.longToBytes(msg.getCrcCode());
            byte[] packtail_arr = SendClientMessageBean.packTail.getBytes(charset);
            int count = packhead_arr.length + packtail_arr.length + funcid_arr.length + packetIdCode_arr.length + checkcode_arr.length;
            byte[] length_arr = ByteTools.intToButeArray(msg.getDataLength() + count);
//			log.warning("编码 内容头部字节长度:" + count);
//			log.warning("编码 packhead长度:" + packhead_arr.length);
//			log.warning("编码 length长度:" + length_arr.length);
//			log.warning("编码 funcid长度:" + funcid_arr.length);
//			log.warning("编码 packetIdCode长度:" + packetIdCode_arr.length);
//			log.warning("编码 checkcode长度:" + checkcode_arr.length);
//			log.warning("编码 content长度:" + content_arr.length);
//			log.warning("编码 packtail长度:" + packtail_arr.length);

//			long packetIdCode = ByteTools.bytesToLong(packetIdCode_arr);
//			log.warning(msg.getCrcCodeId() + "编码再转回->" + packetIdCode);

            IoBuffer buffer = IoBuffer.allocate(packhead_arr.length + length_arr.length + funcid_arr.length + packetIdCode_arr.length + content_arr.length + checkcode_arr.length + packtail_arr.length).setAutoExpand(true);

//			buffer.putString(BaseMessageClient.packHandler,charset.newEncoder());
//			buffer.putInt(msg.getDataLength() + count);
//			buffer.putInt(msg.getMsgTypeId());
//			buffer.putLong(msg.getCrcCodeId());
//			buffer.putString(msg.getContent(),charset.newEncoder());
//			buffer.putLong(msg.getCrcCode());
//			buffer.putString(BaseMessageClient.packTail,charset.newEncoder());

            buffer.put(packhead_arr);
            buffer.put(length_arr);
            buffer.put(funcid_arr);
            buffer.put(packetIdCode_arr);
            buffer.put(content_arr);
            buffer.put(checkcode_arr);
            buffer.put(packtail_arr);
            buffer.flip();
//			log.warning("编码 总长度:" + buffer.remaining());

//			String temp1 = buffer.getString(11,charset.newDecoder());
//			log.warning(temp1);
//			int temp2 = buffer.getInt();
//			log.warning(""+temp2);
//			int temp3 = buffer.getInt();
//			log.warning(""+temp3);
//			long temp4 = buffer.getLong();
//			log.warning(""+temp4);
//			String temp5 = buffer.getString(charset.newDecoder());
//			log.warning(temp5);
//			long temp6 = buffer.getLong
//			log.warning(""+temp6);
//			String temp7 = buffer.getString(charset.newDecoder());
//			log.warning(temp7);


            out.write(buffer);
            out.flush();

            buffer.free();

        }
    }
}
