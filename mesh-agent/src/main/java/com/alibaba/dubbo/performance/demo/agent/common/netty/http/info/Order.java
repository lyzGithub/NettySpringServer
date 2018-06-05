package com.alibaba.dubbo.performance.demo.agent.common.netty.http.info;

public class Order {
    private Customer customer = null;
    private Address billAddress = null;
    private Address shipAddress = null;

    public Order(){
        this.customer = new Customer();
        this.billAddress = new Address();
        this.shipAddress = new Address();

    }
    public Customer getCustomer(){
        return this.customer;
    }
    public Address getBillTo(){
        return this.billAddress;
    }
    public void setBillTo(Address address){
        this.billAddress = new Address(address);
    }
    public void setShipTo(Address address){
        this.shipAddress = new Address(address);
    }
    public String toString(){
        return this.customer.toString()+billAddress.toString()+shipAddress.toString();
    }
}
