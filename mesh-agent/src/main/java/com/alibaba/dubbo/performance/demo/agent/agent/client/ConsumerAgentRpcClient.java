package com.alibaba.dubbo.performance.demo.agent.agent.client;

import com.alibaba.dubbo.performance.demo.agent.agent.client.proxy.IAsyncObjectProxy;
import com.alibaba.dubbo.performance.demo.agent.agent.client.proxy.ObjectProxy;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * RPC Client（Create RPC proxy）
 *
 * @author luxiaoxun
 */
public class ConsumerAgentRpcClient {

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));



    /*public RpcClient(String hostIp, int port, IRegistry registry ) {
        if(registry == null){
            throw new NullPointerException("should init the IRegistry!");
        }
        this.hostIp = hostIp;
        this.port = port;
    }*/

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T>(interfaceClass)
        );
    }

    public static <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        ConnectManage.getInstance().stop();
    }
}

