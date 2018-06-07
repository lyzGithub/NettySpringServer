package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.common.netty.http.common.*;
import com.alibaba.dubbo.performance.demo.agent.common.netty.http.consumer.ConsumerAgentConnectManager;

import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class ConsumerAgentClient {



    public void connect(int port, String host) throws Exception {
        // 配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(
                                    new LineBasedFrameDecoder(1024));
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new ConsumerAgentClientHandler());
                        }
                    });

            // 发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();

            // 当代客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    /**
     * @throws Exception
     */
    public static void run(final String host, final int port) throws Exception {

        new ConsumerAgentClient().connect(port, host);
    }











    /*private Logger logger = LoggerFactory.getLogger(ConsumerAgentClient.class);

    private ConsumerAgentConnectManager connectManager;

    public ConsumerAgentClient(IRegistry registry){
        this.connectManager = new ConsumerAgentConnectManager();
    }

    public Object invoke(String interfaceName, String method, String parameterTypesString, String parameter,final String hostName, final int port) throws Exception {

        Channel channel = connectManager.getChannel(hostName,port);

        AgentRpcInvocation invocation = new AgentRpcInvocation();
        invocation.setMethodName(method);
        invocation.setAttachment("path", interfaceName);
        invocation.setParameterTypes(parameterTypesString);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        AgentJsonUtils.writeObject(parameter, writer);
        invocation.setArguments(out.toByteArray());

        AgentRequest request = new AgentRequest();
        request.setVersion("2.0.0");
        request.setTwoWay(true);
        request.setData(invocation);


        logger.info("requestId=" + request.getId());

        AgentRpcFuture future = new AgentRpcFuture();
        AgentRpcRequestHolder.put(String.valueOf(request.getId()),future);

        channel.writeAndFlush(request);

        Object result = null;
        try {
            result = future.get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }*/
}
