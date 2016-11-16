package com.quncao.core.http;

import com.quncao.core.http.annotation.HttpReqParam;

import java.util.TreeMap;

/**
 * Created by pengjin on 2016/10/30.
 * 简单的Http请求代理，没有公共域名和公共参数
 */

public class HttpRequestProxy extends AbsHttpRequestProxy {


    public static class Builder<T> {
        private RequestListener<T> listener;
        private Object requestParamBody;
        private String tag;
        private boolean cache;

        public Builder() {

        }

        public Builder create(Object requestParamBody, RequestListener<T> listener) {
            this.listener = listener;
            this.requestParamBody = requestParamBody;
            return this;
        }

        public Builder tag(String tag) {
            this.tag = tag;
            return this;
        }


        public Builder cache(boolean isCache) {
            this.cache = cache;
            return this;
        }

        public HttpRequestProxy build() {
            return new HttpRequestProxy(this);
        }
    }

    public HttpRequestProxy(Builder builder) {
        this.listener = builder.listener;
        this.requestParamBody = builder.requestParamBody;
        this.tag = builder.tag;
        this.cache = builder.cache;
        HttpReqParam annotation = requestParamBody.getClass().getAnnotation(
                HttpReqParam.class);
        protocal = annotation.protocal();
        format = annotation.format();
        clazz = (Class) annotation.responseType();
        method = annotation.method();
    }


    public HttpRequestProxy() {

    }

    public static Builder get() {
        return new Builder();
    }


    @Override
    protected String getDomain() {
        return null;
    }


    @Override
    protected TreeMap<String, String> getHeader() {
        return null;
    }

    ;

}
