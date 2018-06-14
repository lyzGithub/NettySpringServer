package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpRequestHolder {
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(65536));

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,String> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,String rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static String get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}
