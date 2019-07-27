package com.zju.redis;

public interface KeyPrefix {

    public int expireSeconds();

    public String getPrefix();
}
