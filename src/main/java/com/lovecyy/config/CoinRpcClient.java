package com.lovecyy.config;

import cn.hutool.core.codec.Base64;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * coin rpc client
 * @author Yakir
 */
@Slf4j
@Configuration
public class CoinRpcClient {
    /**
     * 用户
     */
    @Value("${btc.rpc.user}")
    private String user;
    /**
     * 密码
     */
    @Value("${btc.rpc.password}")
    private String password;
    /**
     * 地址
     */
    @Value("${btc.rpc.address}")
    private String address;
    /**
     * 端口
     */
    @Value("${btc.rpc.port}")
    private String port;


    public JsonRpcHttpClientEnhanche getClient(){
        JsonRpcHttpClientEnhanche client = null;
        try {
            String cred = Base64.encode(user + ":" + password);
            Map<String, String> headers = new HashMap<String, String>(1);
            headers.put("Authorization", "Basic " + cred);
            client = new JsonRpcHttpClientEnhanche(new URL("http://" + address + ":" + port), headers);
        } catch (Exception e) {
            log.error("===com.bscoin.bit.env.CoinRpcClient:{} btc client !===",e.getMessage(),e);
        }
        return client;

    }

}
