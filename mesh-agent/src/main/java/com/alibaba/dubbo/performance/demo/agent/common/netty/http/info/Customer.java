package com.alibaba.dubbo.performance.demo.agent.common.netty.http.info;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private String  fistName ="re";
    private String  lastName ="twetg";
    private List<String> midName = new ArrayList<String>();
    public Customer(){

    }


    public void setFirstName(String fistName){
        this.fistName = fistName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public void setMiddleNames(List<String> midName){
        this.midName.addAll(midName);
    }
    public String toString(){
        return fistName+" "+lastName+ midName.toString();
    }
}
