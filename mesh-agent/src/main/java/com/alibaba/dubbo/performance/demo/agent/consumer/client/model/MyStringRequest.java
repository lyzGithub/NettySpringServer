package com.alibaba.dubbo.performance.demo.agent.consumer.client.model;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class MyStringRequest  implements Serializable {
    private String interfaceName;
    private String methodName;
    private String parameterTypesString;
    private String parameter;
    private static AtomicLong atomicLong = new AtomicLong();
    private String id;
    public MyStringRequest(String interfaceName, String methodName, String parameterTypesString, String parameter) {
        this.id = Long.toString(atomicLong.getAndIncrement());
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
    }

    public String getId(){
        return this.id;
    }

    public void setInterfaceName(String interfaceName) {

        this.interfaceName = interfaceName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setParameterTypesString(String parameterTypesString) {
        this.parameterTypesString = parameterTypesString;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getParameterTypesString() {
        return parameterTypesString;
    }

    public String getParameter() {
        return parameter;
    }
}
