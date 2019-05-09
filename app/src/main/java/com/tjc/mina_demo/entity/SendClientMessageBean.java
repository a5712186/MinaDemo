package com.tjc.mina_demo.entity;

/*
 *   消息协议 用于服务发送到客户端
 */
public class SendClientMessageBean {

	// 头部信息
	public static final String packHandler = "===start===";
	// 内容长度
	private int dataLength;
	//报文类型
	private int msgTypeId;
	// CRC 校验码
	private long crcCode;
	// CRC 校验码Id
	private long crcCodeId;
	// 内容
	private String content;
	// 结尾
	public static final  String packTail = "====end====";




	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public int getMsgTypeId() {
		return msgTypeId;
	}

	public void setMsgTypeId(int msgTypeId) {
		this.msgTypeId = msgTypeId;
	}

	public long getCrcCode() {
		return crcCode;
	}

	public void setCrcCode(long crcCode) {
		this.crcCode = crcCode;
	}

	public long getCrcCodeId() {
		return crcCodeId;
	}

	public void setCrcCodeId(long crcCodeId) {
		this.crcCodeId = crcCodeId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.dataLength = content.getBytes().length;
		this.content = content;
	}

	@Override
	public String toString() {
		return "BaseMessageClient [packHandler=" + packHandler + ", dataLength=" + dataLength + ", msgTypeId="
				+ msgTypeId + ", crcCode=" + crcCode + ", crcCodeId=" + crcCodeId + ", content=" + content
				+ ", packTail=" + packTail + "]";
	}
}
