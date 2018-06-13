package com.alibaba.dubbo.performance.demo.agent.myAgent;

public class RequestPara {
    private String interfaceName;
    private String methodName;
    private String parameterTypesString;
    private String parameter;
    public RequestPara(String interfaceName, String methodName, String parameterTypesString, String parameter){
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypesString = parameterTypesString;
        this.parameter = parameter;
    }
    public void setInterfaceName(String interfaceName){
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
    public String getInterfaceName(){
        return this.interfaceName;
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
