package com.alibaba.dubbo.performance.demo.agent.consumer.server;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * Created by carl.yu on 2016/12/16.
 */
public class HttpConsumerServer {

    private static ThreadPoolExecutor threadPoolExecutor = null;
    private static final Logger logger = LoggerFactory.getLogger(HttpConsumerServer.class);

    private String host = "";
    private int port = 0;

    public HttpConsumerServer(String hostIp, int port){
        this.host = hostIp;
        this.port = port;
        RunInit runInit = new RunInit();
        Thread thread = new Thread(runInit);
        thread.start();
    }

    private class RunInit implements Runnable{
        public RunInit(){

        }
        @Override
        public void run() {
            EventLoopGroup bossGroup=new NioEventLoopGroup();
            EventLoopGroup workerGroup=new NioEventLoopGroup(400);
            RegisteGetThread registeGetThread = new RegisteGetThread();
            try{
                ServerBootstrap bootstrap=new ServerBootstrap();
                bootstrap.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                                socketChannel.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
                                socketChannel.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                                socketChannel.pipeline().addLast("ServerHandler",new HttpServerHandler(registeGetThread));
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


    public static void submit(Runnable task){
        if (threadPoolExecutor == null) {
            synchronized (HttpConsumerServer.class) {
                if (threadPoolExecutor == null) {
                    threadPoolExecutor = new ThreadPoolExecutor(16, 64, 600L,
                            TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
        logger.info("Consumer Server now active thread is: "+threadPoolExecutor.getActiveCount());
    }


    public static void main(String host, int port) throws Exception {
        new HttpConsumerServer(host,port);
    }

}