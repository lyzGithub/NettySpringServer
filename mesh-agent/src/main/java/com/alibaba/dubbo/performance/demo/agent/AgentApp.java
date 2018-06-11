package com.alibaba.dubbo.performance.demo.agent;

import com.alibaba.dubbo.performance.demo.agent.agent.server.ProviderAgentRpcServer;
import com.alibaba.dubbo.performance.demo.agent.consumer.server.HttpConsumerServer;
import com.alibaba.dubbo.performance.demo.agent.provider.client.ClientToProvider;
import com.alibaba.dubbo.performance.demo.agent.registry.IpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

@SpringBootApplication
public class AgentApp {
    // agent会作为sidecar，部署在每一个Provider和Consumer机器上
    // 在Provider端启动agent时，添加JVM参数-Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889
    // 在Consumer端启动agent时，添加JVM参数-Dtype=consumer -Dserver.port=20000
    // 添加日志保存目录: -Dlogs.dir=/path/to/your/logs/dir。请安装自己的环境来设置日志目录。
    private static Logger logger = LoggerFactory.getLogger(AgentApp.class);

    public static void main(String[] args) {

        for(int i = 0; i<args.length; i++){
            System.out.println("args"+i+": "+args[i]);
        }

        String type = System.getProperty("type");
        logger.info("docker type" + type);
        if ("consumer".equals(type)){
            logger.info("Start in Concumer!");
            try {
                String  hostIp = "127.0.0.1";
                int port = Integer.valueOf(System.getProperty("server.port"));
                System.out.println("address is: " + hostIp + ":" + port);
                HttpConsumerServer.main( hostIp, port);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        else if ("provider".equals(type)){
            logger.info("Start in the provider!");
            SpringApplication.run(AgentApp.class, args);
            try {
                String  hostIp = IpHelper.getHostIp();
                //String  hostIp = "127.0.0.1";
                int port = Integer.valueOf(System.getProperty("server.port"));
                System.out.println("address is: " + hostIp + ":" + port);
                ClientToProvider.run();
                /*ApplicationContext ac = new FileSystemXmlApplicationContext("spring.xml");
                ProviderAgentRpcServer providerAgentRpcServer = (ProviderAgentRpcServer)ac.getBean("providerAgentRpcServer");*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
