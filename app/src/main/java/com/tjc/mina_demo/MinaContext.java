package com.tjc.mina_demo;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import org.apache.mina.core.buffer.IoBuffer;

/*
 * 用于处理解码流，分段
 */
public class MinaContext {
	
	private final CharsetDecoder decoder;
    private IoBuffer buf;
    private int matchCount = 0;
    private int overflowPosition = 0;

    public MinaContext(Charset charset) {
        decoder = charset.newDecoder();
        buf = IoBuffer.allocate(3000).setAutoExpand(true);
    }

    public CharsetDecoder getDecoder() {
        return decoder;
    }

    public IoBuffer getBuffer() {
        return buf;
    }

    public int getOverflowPosition() {
        return overflowPosition;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    public void reset() {
        overflowPosition = 0;
        matchCount = 0;
        decoder.reset();
    }

    public void append(IoBuffer in) {
        getBuffer().put(in);
    }

}
