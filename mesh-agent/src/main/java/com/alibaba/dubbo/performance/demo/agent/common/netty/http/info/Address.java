package com.alibaba.dubbo.performance.demo.agent.common.netty.http.info;

public class Address {
    private String city = "fgweg";
    private String country = "gweg";
    private String state = "wgeg";
    private String postCode = "wgeg";

    public Address(){

    }

    public Address(Address address){
        address.setCity(address.city);
        address.setCountry(address.country);
        address.setPostCode(address.postCode);
        address.setState(address.state);
    }

    public void setCity(String city){
        this.city = city;
    }
    public void setCountry(String country){
        this.country = country;
    }
    public void setState(String state){
        this.state = state;
    }
    public void setPostCode(String postCode){
        this.postCode = postCode;
    }
    public String toString(){
        return city+" "+country+" "+state+" "+postCode;
    }

}
