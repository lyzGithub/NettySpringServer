package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.consumer.client.NettyClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public  class ToProviderHolder {
    private static Map<String, NettyClient> clientMap = new ConcurrentHashMap<>();

    public static NettyClient getClient(String host, int port) {
        String key = host+":"+port;
        if(!clientMap.containsKey(key)){
            clientMap.put(key,new NettyClient(host,port));
        }
        return clientMap.get(key);
    }
    public static void setClient(String host,int port, NettyClient client){
        String key = host+":"+port;
        if(!clientMap.containsKey(key)) {
            clientMap.put(key, client);
        }
    }
    public static void remove(String key){
        clientMap.remove(key);
    }
}
