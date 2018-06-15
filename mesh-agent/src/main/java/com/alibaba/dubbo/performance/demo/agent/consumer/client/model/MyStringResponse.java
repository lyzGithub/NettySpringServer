package com.alibaba.dubbo.performance.demo.agent.consumer.client.model;

public class MyStringResponse {
    private String requestId;
    private byte[] bytes;

    public MyStringResponse(String requestId, byte[] bytes) {

        this.requestId = requestId;
        this.bytes = bytes;
    }
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getRequestId() {

        return requestId;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
