package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.registry.Endpoint;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;

import java.util.List;
import java.util.Random;

public class RegisteGetThread {
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private Object lock = new Object();
    private List<Endpoint> endpoints = null;
    private Random random = new Random();
    private int size = 0;

    public RegisteGetThread(){
        RunGetETCD runGetETCD = new RunGetETCD();
        Thread t = new Thread(runGetETCD);
        t.start();
    }

    public Endpoint getEndPoint(){

        int temp = random.nextInt(size);
        Endpoint endpoint;
        synchronized (lock) {
            endpoint = endpoints.get(temp);
        }
        //String url =  "http://" + endpoint.getHost() + ":" + endpoint.getPort();
        return endpoint;

    }



    class RunGetETCD implements Runnable{

        @Override
        public void run() {

            while(true) {

                synchronized (lock) {
                    try {
                        endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                size = endpoints.size();
                System.out.println("endpoints.size()" + size);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
