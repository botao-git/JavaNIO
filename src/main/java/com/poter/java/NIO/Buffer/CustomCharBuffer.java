package com.poter.java.NIO.Buffer;


import org.junit.Test;

import java.nio.CharBuffer;

public class CustomCharBuffer {

    @Test
    public void testCharBuffer(){
        CharBuffer buffer = CharBuffer.allocate(100);
        String[] greeting = {"hello","world123"};

        for (String word : greeting) {
            buffer.put(word);
            buffer.flip();
            int remaining = buffer.remaining();
            for (int i = 0; i < remaining; i++) {
                System.out.print(buffer.get());
            }
            System.out.println("");
            buffer.compact();// 对缓冲区进行压缩: 已经读取的记录释放掉,使buffer处于可重新写入状态
                             // 此处可换成 clear() 方法,clear不在于剩余的元素直接将position置为0, limit置为capacity
        }

    }
}
