package com.tjc.mina_demo.entity;

import java.nio.charset.Charset;

/*
 *  消息协议 用于客户端发送到服务
 */
public class SendServiceMessageBean {

    public static final char dataType = 'S';
    private int dataId;
    private int dataLength;
    private long codeId;
    private String content;
    public static final char finality = 'C';


    @Override
    public String toString() {
        return "SendServiceMessageBean{\n" +
                "\ndataType=" + dataType +
                "\n, dataId=" + dataId +
                "\n, dataLength=" + dataLength +
                "\n, codeId=" + codeId +
                "\n, content='" + content + '\'' +
                "\n, finality=" + finality +
                '}';
    }

//    private String TAG;//标记
//
//    public String getTAG() {
//        return TAG;
//    }
//
//    public void setTAG(String TAG) {
//        this.TAG = TAG;
//    }



    public int getDataId() {
        return dataId;
    }

    public void setDataId(int dataId) {
        this.dataId = dataId;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public long getCodeId() {
        return codeId;
    }

    public void setCodeId(long codeId) {
        this.codeId = codeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.dataLength = content.getBytes(Charset.defaultCharset()).length;
        this.content = content;
    }




}
