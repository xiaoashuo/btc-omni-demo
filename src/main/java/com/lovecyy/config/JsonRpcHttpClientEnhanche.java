package com.lovecyy.config;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.lovecyy.model.pojo.BlockInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JsonRpcHttpClientEnhanche extends JsonRpcHttpClient {
    public JsonRpcHttpClientEnhanche(URL serviceUrl, Map<String, String> headers) {
        super(serviceUrl, headers);
    }

    public JsonRpcHttpClientEnhanche(ObjectMapper mapper, URL serviceUrl, Map<String, String> headers) {
        super(mapper, serviceUrl, headers);
    }

    public JsonRpcHttpClientEnhanche(ObjectMapper mapper, URL serviceUrl, Map<String, String> headers, boolean gzipRequests, boolean acceptGzipResponses) {
        super(mapper, serviceUrl, headers, gzipRequests, acceptGzipResponses);
    }

    public JsonRpcHttpClientEnhanche(URL serviceUrl) {
        super(serviceUrl);
    }

    public <T> List<T> invokeList(String methodName, Object argument, Class<T> elementType) throws Throwable {
        Object invoke = this.invoke(methodName, argument, Object.class, new HashMap());
        if (ObjectUtil.isNull(invoke)){
            return Collections.emptyList();
        }
       // JSONUtil.toBean(JSONUtil.toJsonStr(getblock), BlockInfo.class)
        return JSONUtil.toList(JSONUtil.parseArray(invoke), elementType);
    }

}
