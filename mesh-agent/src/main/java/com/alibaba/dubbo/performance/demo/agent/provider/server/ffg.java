package com.alibaba.dubbo.performance.demo.agent.provider.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class ProviderNettyHandler extends ChannelHandlerAdapter {
    private int counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            String body = (String) msg;
            JsonDataObject request = JsonUtil.fromJson(body, JsonDataObject.class);

            counter = counter + 1;
            JsonDataObject response = new JsonDataObject();
            response.setCode(0);
            response.setMsg("Success");
            response.setData(counter+"");
            String respJson = JsonUtil.toJson(response);

            byte[] respUtf8 = respJson.getBytes("UTF-8");
            int respLength = respUtf8.length;
            ByteBuf respLengthBuf = PooledByteBufAllocator.DEFAULT.buffer(4);
            respLengthBuf.writeInt(respLength);
            respLengthBuf.order(ByteOrder.BIG_ENDIAN);
            ctx.write(respLengthBuf);
            ByteBuf resp = PooledByteBufAllocator.DEFAULT.buffer(respUtf8.length);
            resp.writeBytes(respUtf8);
            ctx.write(resp);
        } catch (Exception e) {
            NettyCommandServer.logger.error(e.getMessage() + "\r\n");
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
            NettyCommandServer.logger.error(sw.toString());
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}