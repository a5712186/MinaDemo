package com.tjc.mina_demo;


import com.tjc.mina_demo.entity.SendClientMessageBean;

import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.zip.CRC32;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

/*
 * 解码数据
 * 
 */
public class CharsetDataDecoder implements ProtocolDecoder {
	private final static Logger log = Logger.getLogger(CharsetDataDecoder.class.getSimpleName());
	private Charset charset;
	// 可变的IoBuffer数据缓冲区
//	private IoBuffer buff = IoBuffer.allocate(100).setAutoExpand(true);

	public CharsetDataDecoder(Charset charset) {
		this.charset = charset;
	}

	private final AttributeKey context = new AttributeKey(getClass(), "context");

	private MinaContext getContext(IoSession session) {
		MinaContext ctx;
		ctx = (MinaContext) session.getAttribute(context);
		if (ctx == null) {
			ctx = new MinaContext(charset);
			session.setAttribute(context, ctx);
		}
		return ctx;
	}

	// 请求报文的最大长度 100k
	private int maxPackLength = 102400;

	public int getMaxPackLength() {
		return maxPackLength;
	}

	public void setMaxPackLength(int maxPackLength) {
		if (maxPackLength <= 0) {
			throw new IllegalArgumentException("请求报文最大长度：" + maxPackLength);
		}
		this.maxPackLength = maxPackLength;
	}

   //数据包头部有多少字节
    private final int packHeadLength = 42;

	@Override
	public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		log.info("#########decode#########");

		// 先获取上次的处理上下文，其中可能有未处理完的数据
		MinaContext ctx = getContext(session);
		// 先把当前buffer中的数据追加到Context的buffer当中
		ctx.append(in);
		// 把position指向0位置，把limit指向原来的position位置
		IoBuffer buf = ctx.getBuffer();
		buf.flip();
		// 然后按数据包的协议进行读取
		while (buf.remaining() >= packHeadLength) {
			log.info("test 长度1：" + buf.remaining());
			buf.mark();
			// 读取包头 2个字节
			String packhead = new String(ByteTools.getDataLength(11,buf),charset);
            log.info("包头：" + packhead);
			if(SendClientMessageBean.packHandler.equals(packhead)){
				//读取包的长度 4个字节 报文的长度，不包含包头和包尾
				int length = ByteTools.byteArrayToInt(ByteTools.getDataLength(4,buf));
				log.info("长度：" + length);
				log.info("test 长度2：" + buf.remaining());
				// 检查读取是否正常，不正常的话清空buffer
				if (length < 0 || length > maxPackLength) {
					log.info("报文长度[" + length + "] 超过最大长度：" + maxPackLength
							+ "或者小于0,清空buffer");
					buf.clear();
					break;
					//packHeadLength - 2 :减去包尾的长度，
					//length - 2 <= buf.remaining() ：代表length-本身长度占用的两个字节-包头长度
				}else if(length >= packHeadLength && length - 22 <= buf.remaining()){
					//读取协议类型4个字节
					int funcid = ByteTools.byteArrayToInt(ByteTools.getDataLength(4,buf));
					log.info("协议类型：" + funcid);
					//读取数据包标识码8个字节
					long packetIdCode = ByteTools.bytesToLong(ByteTools.getDataLength(8,buf));
					log.info("数据包标识码：" + packetIdCode);

					//读取报文正文内容
					int oldLimit = buf.limit();
//					log.info("limit:" + (buf.position() + length));
					//当前读取的位置 + 总长度  - 前面读取的字节长度 - 校验码
					buf.limit(buf.position() + length - packHeadLength);
					String content = buf.getString(ctx.getDecoder());
					buf.limit(oldLimit);
					log.info("报文正文内容：" + content);
					CRC32 crc = new CRC32();
					crc.update(content.getBytes(charset));

					//读取校验码 8个字节
                    long checkcode = ByteTools.bytesToLong(ByteTools.getDataLength(8,buf));
					log.info("校验码：" + checkcode);
					//验证校验码
					if(checkcode != crc.getValue()){
						// 如果消息包不完整,将指针重新移动消息头的起始位置
						buf.reset();
						break;
					}
					//读取包尾 2个字节
					String packtail = new String(ByteTools.getDataLength(11,buf),charset);
					log.info("包尾：" + packtail);
					if(!SendClientMessageBean.packTail.equals(packtail)){
						// 如果消息包不完整,将指针重新移动消息头的起始位置
						buf.reset();
						break;
					}
					SendClientMessageBean message = new SendClientMessageBean();
					message.setDataLength(length);
					message.setCrcCode(checkcode);
					message.setMsgTypeId(funcid);
					message.setCrcCodeId(packetIdCode);
					message.setContent(content);
					out.write(message);
				}else{
					// 如果消息包不完整,将指针重新移动消息头的起始位置
					buf.reset();
					break;
				}
			}else{
				// 如果消息包不完整,将指针重新移动消息头的起始位置
				buf.reset();
				break;
			}
		}
		if (buf.hasRemaining()) {
			// 将数据移到buffer的最前面
			IoBuffer temp = IoBuffer.allocate(maxPackLength).setAutoExpand(true);
			temp.put(buf);
			temp.flip();
			buf.clear();
			buf.put(temp);
		} else {// 如果数据已经处理完毕，进行清空
			buf.clear();
		}


//		// 如果有消息
//		while (in.hasRemaining()) {
//			// 判断消息是否是结束符，不同平台的结束符也不一样；
//			// windows换行符（\r\n）就认为是一个完整消息的结束符了； UNIX 是\n；MAC 是\r
//			byte b = in.get();
//			if (b == '\n') {
//				buff.flip();
//				byte[] bytes = new byte[buff.limit()];
//				buff.get(bytes);
//				String message = new String(bytes, charset);
//
//				buff = IoBuffer.allocate(100).setAutoExpand(true);
//
//				// 如果结束了，就写入转码后的数据
//				out.write(message);
//				 log.info("换行message: " + message);
//			} else {
//				buff.put(b);
//				log.info("当前message: " + String.valueOf(((char)b)));
//			}
//		}
	}

	@Override
	public void finishDecode(IoSession session, ProtocolDecoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		log.info("#########完成解码#########");
	}

	@Override
	public void dispose(IoSession session) throws Exception {
		// TODO Auto-generated method stub
		log.info("#########dispose#########");
        log.info(String.valueOf(session.getCurrentWriteMessage()));
	}

}
