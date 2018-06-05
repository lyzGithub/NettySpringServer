package com.alibaba.dubbo.performance.demo.agent.common.netty.http.common;

import java.util.concurrent.ConcurrentHashMap;

public class AgentRpcRequestHolder {

    // key: requestId     value: RpcFuture
    private static ConcurrentHashMap<String,AgentRpcFuture> processingRpc = new ConcurrentHashMap<>();

    public static void put(String requestId,AgentRpcFuture rpcFuture){
        processingRpc.put(requestId,rpcFuture);
    }

    public static AgentRpcFuture get(String requestId){
        return processingRpc.get(requestId);
    }

    public static void remove(String requestId){
        processingRpc.remove(requestId);
    }
}
