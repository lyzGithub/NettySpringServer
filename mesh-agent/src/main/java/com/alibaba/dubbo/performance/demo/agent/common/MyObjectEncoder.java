package com.alibaba.dubbo.performance.demo.agent.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MyObjectEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {
        byte[] datas = ObjConverter.objectToByte(obj);
        byteBuf.writeBytes(datas);
        channelHandlerContext.flush();
    }
}
