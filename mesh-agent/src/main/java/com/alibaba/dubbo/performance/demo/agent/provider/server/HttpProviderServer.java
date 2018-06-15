package com.alibaba.dubbo.performance.demo.agent.provider.server;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by carl.yu on 2016/12/16.
 */
public class HttpProviderServer {

    //private static ThreadPoolExecutor threadPoolExecutor = null;
    private static final Logger logger = LoggerFactory.getLogger(HttpProviderServer.class);
    private RpcClient rpcClient ;
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));

    private String host = "";
    private int port = 0;

    public HttpProviderServer(String hostIp, int port, RpcClient rpcClient){
        this.host = hostIp;
        this.port = port;
        this.rpcClient = rpcClient;
        RunInit runInit = new RunInit();
        Thread thread = new Thread(runInit);
        thread.start();
    }

    private class RunInit implements Runnable{
        public RunInit(){

        }
        @Override
        public void run() {
            int acceptThreads = 40;
            int readWriteThreads = 80;
            if(host.equals("10.10.10.3")){
                acceptThreads = 50;
                readWriteThreads = 300;
            }else if(host.equals("10.10.10.4")){
                acceptThreads = 50;
                readWriteThreads = 360;
            }
            else if(host.equals("10.10.10.5")){
                acceptThreads = 50;
                readWriteThreads = 400;
            }

            EventLoopGroup bossGroup=new NioEventLoopGroup(acceptThreads);
            EventLoopGroup workerGroup=new NioEventLoopGroup(readWriteThreads);
            try{
                ServerBootstrap bootstrap=new ServerBootstrap();
                bootstrap.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast(new ObjectDecoder(1024, ClassResolvers.cacheDisabled(this
                                                .getClass().getClassLoader())));
                                socketChannel.pipeline().addLast(new ObjectEncoder());
                                socketChannel.pipeline().addLast("ServerHandler",new ProviderHttpServerHandler(rpcClient));
                            }
                        });
                System.out.println("服务器网址:"+host+":"+port);
                ChannelFuture future = bootstrap.bind(host,port).sync();
                System.out.println("服务器已启动>>网址:"+host+":"+port);
                future.channel().closeFuture().sync();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }


    public static void main(String host, int port, RpcClient rpcClient) throws Exception {
        new HttpProviderServer(host,port,rpcClient);
    }

}