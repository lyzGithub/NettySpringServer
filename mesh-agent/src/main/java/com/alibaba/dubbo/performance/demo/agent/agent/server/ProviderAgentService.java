package com.alibaba.dubbo.performance.demo.agent.agent.server;

import com.alibaba.dubbo.performance.demo.agent.agent.RequestPara;

public interface ProviderAgentService {
    String hello(String name);
    String getHashCode(RequestPara requestPara);
}
