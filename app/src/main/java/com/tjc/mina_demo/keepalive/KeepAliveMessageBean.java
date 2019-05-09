package com.tjc.mina_demo.keepalive;

/*
 *   心跳组装类
 *
 */
public class KeepAliveMessageBean {

    public static final char typeData = 'K';
    private int typeId;
    private long dateHs;
    public static final char finality = 'C';


    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public long getDateHs() {
        return dateHs;
    }

    public void setDateHs(long dateHs) {
        this.dateHs = dateHs;
    }

    @Override
    public String toString() {
        return "KeepAliveMessageBean{" +
                "typeId=" + typeId +
                ", dateHs=" + dateHs +
                '}';
    }


}
