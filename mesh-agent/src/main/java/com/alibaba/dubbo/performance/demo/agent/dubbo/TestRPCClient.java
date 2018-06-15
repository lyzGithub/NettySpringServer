package com.alibaba.dubbo.performance.demo.agent.dubbo;

import java.util.Random;
import org.apache.commons.lang3.RandomStringUtils;


public class TestRPCClient {
    RpcClient rpcClient ;
    Random r = new Random(1);
    private long dulationTimeMill = 30000;

    private long count= 0;
    public TestRPCClient(){
        this.rpcClient = new RpcClient();
        int getNum = 100;
        RunGetETCD[] rS = new RunGetETCD[getNum];
        for(int i = 0; i< getNum; i++) {
            RunGetETCD runGetETCD = new RunGetETCD();
            Thread thread1 = new Thread(runGetETCD);
            thread1.start();
            rS[i] = runGetETCD;
        }

        int stop = 0;
        while(stop<getNum){
            for(int i = 0; i< getNum; i++) {
                if (!rS[i].isGetDone() && rS[i].getStop()) {
                    stop ++;
                    long myCount  = rS[i].getCount();
                    count += myCount;
                }
            }
        }
        System.out.println("~~~~~~~~~~~~~~~all average~~~~~~~~~~~~~~: " + (double)count/(dulationTimeMill/1000));
    }


    class RunGetETCD implements Runnable {

        private final String interfaceName = "com.alibaba.dubbo.performance.demo.provider.IHelloService";
        private final String parameterTypesString = "Ljava/lang/String;";
        private final String methodName = "hash";
        private final String str = RandomStringUtils.random(1024, true, true);
        private boolean isStop = false;
        private long count = 0;
        private boolean getDone = false;
        @Override
        public void run() {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long averageTime = 0;
            long startTime = System.currentTimeMillis();
            while(!isStop) {
                Object result = null;
                long duTime = System.currentTimeMillis();
                try {
                    result = rpcClient.invoke(interfaceName, parameterTypesString, methodName, str);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String nb = new String((byte[]) result);
                count ++;
                long nowTime = System.currentTimeMillis();
                averageTime += nowTime-duTime;
                //System.out.println("thread time: " + (nowTime- startTime));
                if(Math.abs(nowTime- startTime) > dulationTimeMill){
                    isStop = true;
                }
            }
            averageTime = averageTime/count;
            System.out.println("~~~~~~~~~~~average time for each rpc client request: " + averageTime);
        }

        public boolean isGetDone(){
            return this.getDone;
        }
        public boolean getStop(){
            getDone = isStop;
            return this.isStop;
        }
        public long getCount(){
            return this.count;
        }
    }
}
