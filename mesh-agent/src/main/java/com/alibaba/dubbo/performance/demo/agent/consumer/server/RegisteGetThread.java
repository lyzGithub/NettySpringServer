package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.AgentApp;
import com.alibaba.dubbo.performance.demo.agent.agent.client.ConnectManage;
import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;

public class RegisteGetThread {
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private static Logger logger = LoggerFactory.getLogger(RegisteGetThread.class);

    private Object lock = new Object();
    private List<Endpoint> endpoints = null;
    private Random random = new Random();
    private int size = 0;

    public RegisteGetThread(){
        RunGetETCD runGetETCD = new RunGetETCD();
        Thread t = new Thread(runGetETCD);
        t.start();
    }
    class RunGetETCD implements Runnable{
        @Override
        public void run() {

            while(true) {
                synchronized (lock) {
                    try {
                        endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                        size = endpoints.size();
                        //logger.info("endpoint size: " + size);
                        ConnectManage.getInstance().updateConnectedServer( endpoints);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //System.out.println("endpoints.size()" + size);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
