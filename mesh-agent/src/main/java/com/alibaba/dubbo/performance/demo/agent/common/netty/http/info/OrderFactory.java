package com.alibaba.dubbo.performance.demo.agent.common.netty.http.info;

public class OrderFactory {
    public static Object create(int numObject){
        Object obj = new Order();
        return obj;
    }
}
