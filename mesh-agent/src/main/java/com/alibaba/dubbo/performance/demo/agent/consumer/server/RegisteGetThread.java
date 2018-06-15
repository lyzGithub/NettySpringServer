package com.alibaba.dubbo.performance.demo.agent.consumer.server;

import com.alibaba.dubbo.performance.demo.agent.consumer.client.NettyClient;
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

    private int[] array = new int[6];


    public RegisteGetThread(){
        array[0] =0;
        array[1] =1;array[2] =1;
        array[3] =2;array[4] =2;array[5] = 2;

        RunGetETCD runGetETCD = new RunGetETCD();
        Thread t = new Thread(runGetETCD);
        t.start();
    }
    public Endpoint getEndPoint(){
        //调度

        int   index= array[ random.nextInt(6) ];
        if(index == 0){
            return new Endpoint("10.10.10.3", 30000);
        }else if(index == 1){
            return new Endpoint("10.10.10.4", 30000);
        }else{
            return new Endpoint("10.10.10.5", 30000);
        }
        //Endpoint  endpoint;
        /*synchronized (lock) {
             endpoint= endpoints.get(index);
        }
        return endpoint;*/

    }
    class RunGetETCD implements Runnable{
        @Override
        public void run() {

            while(true) {
                synchronized (lock) {
                    try {
                        endpoints = registry.find("com.alibaba.dubbo.performance.demo.provider.IHelloService");
                        size = endpoints.size();
                        for(int i = 0; i<size; i++){
                            Endpoint endpoint = endpoints.get(i);
                            final String host = endpoint.getHost();
                            final int port = endpoint.getPort();
                            ToProviderHolder.setClient(host, port, new NettyClient(host,port));
                        }
                        logger.info("endpoint size: " + size);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //System.out.println("endpoints.size()" + size);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
