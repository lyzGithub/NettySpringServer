package com.alibaba.dubbo.performance.demo.agent.provider.client;

import com.alibaba.dubbo.performance.demo.agent.dubbo.RpcClient;
import com.alibaba.dubbo.performance.demo.agent.registry.EtcdRegistry;
import com.alibaba.dubbo.performance.demo.agent.registry.IRegistry;

public class ClientToProvider {
    private IRegistry registry = new EtcdRegistry(System.getProperty("etcd.url"));
    private RpcClient rpcClient = new RpcClient(registry);

    private String interfaceTo = "com.alibaba.dubbo.performance.demo.provider.IHelloService";
    private String methodTo = "hash";
    private String parameterTypesStringTo = "Ljava/lang/String;";
    private String parameterTo = "";

    public static void main(String args){
        new ClientToProvider();
    }

    public ClientToProvider(){
        parameterTo = "123475963rhehrehstnstnn";
        RunTest runTest = new RunTest();
        Thread t = new Thread(runTest);
        t.start();
    }



    private class RunTest implements Runnable{
        public RunTest(){

        }

        @Override
        public void run() {
            int i = 100;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while(i>0) {
                i--;
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Object result = null;
                try {
                    result = rpcClient.invoke(interfaceTo, methodTo, parameterTypesStringTo, parameterTo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String resultString = new String((byte[]) result);
                System.out.println("rpc client result: " + resultString);
            }
        }
    }
}
