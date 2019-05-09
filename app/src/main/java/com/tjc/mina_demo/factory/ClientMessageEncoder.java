package com.tjc.mina_demo.factory;

import com.tjc.mina_demo.ByteTools;
import com.tjc.mina_demo.entity.SendServiceMessageBean;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.apache.mina.filter.codec.demux.MessageEncoder;

import java.nio.charset.Charset;
import java.util.logging.Logger;

/*
 *  编码客户端发送数据
 *  编码格式 S.001.256.8888.C
 *        类型.ID码.数据大小.数据.CRC检查码.结尾
 */
public class ClientMessageEncoder implements MessageEncoder<SendServiceMessageBean> {
    private final static Logger log = Logger.getLogger(ClientMessageEncoder.class.getSimpleName());

    @Override
    public void encode(IoSession session, SendServiceMessageBean msg, ProtocolEncoderOutput out) throws Exception {

        byte[] type_arr = ByteTools.getBytes(SendServiceMessageBean.dataType);
        byte[] id_arr = ByteTools.intToButeArray(msg.getDataId());
        byte[] code_arr = ByteTools.longToBytes(msg.getCodeId());
        byte[] finality_arr = ByteTools.getBytes(SendServiceMessageBean.finality);
        byte[] content_arr = msg.getContent().getBytes(Charset.defaultCharset());
        int count = type_arr.length + id_arr.length + code_arr.length + finality_arr.length;
        byte[] length_arr = ByteTools.intToButeArray(msg.getDataLength() + count);

        IoBuffer buffer = IoBuffer.allocate(count + msg.getDataLength() + length_arr.length).setAutoExpand(true);

//        	log.warning("编码 内容头部字节长度:" + count);
//			log.warning("编码 type_arr长度:" + type_arr.length);
//			log.warning("编码 ID码:" + id_arr.length);
//			log.warning("编码 数据大小:" + length_arr.length);
//			log.warning("编码 数据长度:" + content_arr.length);
//			log.warning("编码 code长度:" + code_arr.length);
//			log.warning("编码 结尾长度:" + finality_arr.length);

        buffer.put(type_arr);
        buffer.put(id_arr);
        buffer.put(length_arr);
        buffer.put(content_arr);
        buffer.put(code_arr);
        buffer.put(finality_arr);
        buffer.flip();

//        StringMessageBean temp = new StringMessageBean();
//        temp.setDataType(ByteTools.getChar(ByteTools.getDataLength(2,buffer)));
//        temp.setDataId(ByteTools.byteArrayToInt(ByteTools.getDataLength(4,buffer)));
//        temp.setDataLength(ByteTools.byteArrayToInt(ByteTools.getDataLength(4,buffer)));
//        temp.setContent(new String(ByteTools.getDataLength(temp.getDataLength() - 16,buffer),Charset.defaultCharset()));
//        temp.setCodeId(ByteTools.bytesToLong(ByteTools.getDataLength(8,buffer)));
//        char tail = ByteTools.getChar(ByteTools.getDataLength(2,buffer));
//        log.warning(temp.toString());
//        log.warning("结尾："+tail);
        out.write(buffer);
        out.flush();

        buffer.free();

    }
}
