package com.alibaba.dubbo.performance.demo.agent.consumer.client.model;

import java.util.concurrent.ConcurrentHashMap;

public class NettyRequestHolder {
    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,NettyClientFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,NettyClientFuture rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static NettyClientFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}
