package com.alibaba.dubbo.performance.demo.agent.agent.client.proxy;


import com.alibaba.dubbo.performance.demo.agent.agent.client.RPCFuture;

/**
 * Created by luxiaoxun on 2016/3/16.
 */
public interface IAsyncObjectProxy {
    public RPCFuture call(String funcName, Object... args);
}