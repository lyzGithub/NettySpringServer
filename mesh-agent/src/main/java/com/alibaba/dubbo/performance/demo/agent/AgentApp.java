package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentRpcServer;
import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentService;
import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentServiceImpl;
import com.alibaba.dubbo.performance.demo.agent.consumer.server.HttpConsumerServer;
import com.alibaba.dubbo.performance.demo.agent.dubbo.TestRPCClient;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class AgentApp {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。
    private static Logger logger = LoggerFactory.getLogger(AgentApp.class);

    public static void main(String[] args) {

        String type = System.getProperty("type");
        logger.info("docker type: " + type);
        if ("consumer".equals(type)){
            logger.info("Start in Concumer!");
            try {
                HttpConsumerServer.main("127.0.0.1", Integer.valueOf(System.getProperty("server.port")) );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if ("provider".equals(type)){
            logger.info("Start in the provider!");
            //SpringApplication.run(AgentApp.class, args);
            try {
                int port = Integer.valueOf(System.getProperty("server.port"));
                String hostIp = null;
                try {
                    hostIp = IpHelper.getHostIp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ProviderAgentRpcServer rpcServer = new ProviderAgentRpcServer(hostIp, port);
                ProviderAgentService providerAgentService = new ProviderAgentServiceImpl();
                rpcServer.addService("com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentService", providerAgentService);


                try {
                    rpcServer.start();
                } catch (Exception ex) {
                    logger.error("Exception: {}", ex);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
