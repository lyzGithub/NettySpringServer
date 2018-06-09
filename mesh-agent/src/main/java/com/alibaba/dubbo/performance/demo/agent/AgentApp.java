package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.consumer.server.HttpConsumerServer;
import com.alibaba.dubbo.performance.demo.agent.provider.client.ClientToProvider;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AgentApp {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。
    private static Logger logger = LoggerFactory.getLogger(AgentApp.class);

    public static void main(String[] args) {
        String type = System.getProperty("type");
        logger.info("docker type" + type);
        if ("consumer".equals(type)){
            logger.info("Start in Concumer !");
            try {
                String  hostIp = IpHelper.getHostIp();
                int port = Integer.valueOf(System.getProperty("server.port"));
                System.out.println("address is: " + hostIp + ":" + port);
                HttpConsumerServer.main( hostIp, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if ("provider".equals(type)){
            logger.info("Start in the provider!");
            try {
                String  hostIp = IpHelper.getHostIp();
                int port = Integer.valueOf(System.getProperty("server.port"));
                System.out.println("address is: " + hostIp + ":" + port);
                ClientToProvider clientToProvider = new ClientToProvider();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
