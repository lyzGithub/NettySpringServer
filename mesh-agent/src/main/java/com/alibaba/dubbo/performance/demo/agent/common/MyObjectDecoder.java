package com.alibaba.dubbo.performance.demo.agent.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class MyObjectDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        Object obj = ObjConverter.byteToObject(ObjConverter.read(byteBuf));
        list.add(obj);
    }
}
