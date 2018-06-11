package com.alibaba.dubbo.performance.demo.agent;
import com.alibaba.dubbo.performance.demo.agent.agent.client.ConsumerAgentRpcClient;
import com.alibaba.dubbo.performance.demo.agent.agent.client.RPCFuture;
import com.alibaba.dubbo.performance.demo.agent.agent.client.proxy.IAsyncObjectProxy;
import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentRpcServer;
import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentService;
import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentServiceImpl;
import com.alibaba.dubbo.performance.demo.agent.consumer.server.HttpConsumerServer;
import com.alibaba.dubbo.performance.demo.agent.provider.client.ClientToProvider;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.concurrent.TimeUnit;

public class AgentApp2 {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。
    private static Logger logger = LoggerFactory.getLogger(AgentApp.class);

    public static void main(String[] args) {


            logger.info("Start in Concumer!");
            try {
                String  hostIp = "127.0.0.1";
                ConsumerAgentRpcClient rpcClient = new ConsumerAgentRpcClient(hostIp,30001);
                IAsyncObjectProxy client = rpcClient.createAsync(ProviderAgentService.class);
                System.out.println("write: "+"hello");
                RPCFuture helloFuture = client.call("hello", "hello");
                System.out.println("write finish!");
                String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
                System.out.println("get result: "+result);


                //HttpConsumerServer.main( hostIp, port);
            } catch (Exception e) {
                e.printStackTrace();
            }



    }
}
