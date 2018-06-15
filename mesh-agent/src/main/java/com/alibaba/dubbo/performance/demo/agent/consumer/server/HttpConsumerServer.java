package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.asynchttpclient.AsyncHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.asynchttpclient.Dsl.config;


/**
 * Created by carl.yu on 2016/12/16.
 */
public class HttpConsumerServer {

    private static ThreadPoolExecutor threadPoolExecutor = null;
    private static final Logger logger = LoggerFactory.getLogger(HttpConsumerServer.class);

    private String host = "";
    private int port = 0;
    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(800);

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
            int acceptThreads = 100;
            int readWriteThreads = 400;
            /*EventLoopGroup bossGroup=new NioEventLoopGroup(acceptThreads);
            EventLoopGroup workerGroup=new NioEventLoopGroup(readWriteThreads);*/
            EventLoopGroup bossGroup=new NioEventLoopGroup();
            EventLoopGroup workerGroup=new NioEventLoopGroup();
            RegisteGetThread registeGetThread = new RegisteGetThread();
            AsyncHttpClient asyncHttpClient = org.asynchttpclient.Dsl.asyncHttpClient();
            try{
                ServerBootstrap bootstrap=new ServerBootstrap();
                bootstrap.group(bossGroup,workerGroup)
                        .channel(NioServerSocketChannel.class)
                        /*.option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)*/
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                socketChannel.pipeline().addLast("http-decoder",new HttpRequestDecoder());
                                socketChannel.pipeline().addLast("http-aggregator",new HttpObjectAggregator(65536));
                                socketChannel.pipeline().addLast("http-encoder",new HttpResponseEncoder());
                                socketChannel.pipeline().addLast("ServerHandler",new ConsumerHttpServerHandler(registeGetThread,asyncHttpClient));

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
        //logger.info("Consumer Server now active thread is: "+threadPoolExecutor.getActiveCount());
    }


    public static void main(String host, int port) throws Exception {
        new HttpConsumerServer(host,port);
    }

}