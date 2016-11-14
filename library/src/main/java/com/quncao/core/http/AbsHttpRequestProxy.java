package com.quncao.core.http;

import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.error.VolleyError;
import com.google.gson.Gson;
import com.quncao.core.http.annotation.HttpReqParam;
import com.quncao.core.http.annotation.HttpReqParam.HttpReqMethod;
import com.quncao.core.http.request.GsonRequestEX;
import com.quncao.core.http.request.JsonObjectRequestEX;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author pengjin
 * @version V1.0
 * @Description: Http请求代理，支持扩展域名、公共参数、加密算法
 * @date 2015-10-24 下午2:17:15
 */
public abstract class AbsHttpRequestProxy<T> {
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 10000;

    private Request request;
    protected Class<T> clazz;
    protected RequestListener<T> listener;
    protected Object requestParamBody;
    protected String protocal;
    protected HttpReqMethod method;
    protected HttpReqParam.DataFormat format;
    protected String tag;
    protected boolean gzip = false;
    protected boolean cache = false;


    /**
     * 域名
     *
     * @return
     */
    protected abstract String getDomain();


    protected abstract TreeMap<String, String> getHeader();


    public interface RequestListener<T> {
        public void onSuccess(T response);

        public void onFailed(VolleyError error);
    }


    /*@SuppressWarnings("unchecked")
    public AbsHttpRequestProxy(Object requestParamBody, RequestListener<T> listener) {
        this.listener = listener;
        this.requestParamBody = requestParamBody;
        HttpReqParam annotation = requestParamBody.getClass().getAnnotation(
                HttpReqParam.class);
        protocal = annotation.protocal();
        clazz = (Class<T>) annotation.responseType();
        method = annotation.method();
    }*/


    /**
     * 执行http请求
     */
    public void excute() {
        if (method == HttpReqMethod.HTTP_GET) {
            doGet();
        } else {
            doPost();
        }
    }


    /**
     * 取消请求
     */
    public void cancel() {
        request.cancel();
    }


    /**
     * get方式请求
     */
    private void doGet() {
        RequestQueue requestQueue = HttpRequestManager.getInstance().getRequestQueue();
        Map<String, String> params = getRequestParams();
        String requestUrl = buildGetRequestUrl(protocal, params);
        request = new GsonRequestEX<T>(requestUrl, clazz, gzip, null,
                new Listener<T>() {
                    @Override
                    public void onResponse(T response) {
                        // 请求成功
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 请求失败
                listener.onFailed(error);
            }
        });

        if (!TextUtils.isEmpty(tag)) {
            request.setTag(tag);
        }
        Map<String, String> header = getHeader();
        if (header != null && header.size() > 0) {
            request.setHeaders(header);
        }
        request.setShouldCache(cache);
        request.setRetryPolicy(new DefaultRetryPolicy(
                DEFAULT_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private Gson mGson = new Gson();


    /**
     * post方式请求
     */
    private void doPost() {
        RequestQueue requestQueue = HttpRequestManager.getInstance().getRequestQueue();
        String requestUrl = buildPostRequestUrl(protocal);

        if (format == HttpReqParam.DataFormat.MAP) {// MAP格式的数据
            // 公共参数
            TreeMap<String, String> params = new TreeMap<String, String>();

            // 请求参数
            params.putAll(getRequestParams());
            request = new GsonRequestEX<T>(Method.POST, requestUrl, gzip, clazz, null,
                    params, new Listener<T>() {

                @Override
                public void onResponse(T response) {
                    // 请求成功
                    listener.onSuccess(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onFailed(error);
                }
            });
        } else if (format == HttpReqParam.DataFormat.JSON) { // JSON格式的数据
            try {
                String reqParam = getRequestJsonString();

                request = new JsonObjectRequestEX<T>(Method.POST, requestUrl, reqParam, clazz, gzip, new Listener<T>() {
                    @Override
                    public void onResponse(T response) {
                        // 请求成功
                        listener.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // 请求失败
                        listener.onFailed(error);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(tag)) {
            request.setTag(tag);
        }
        Map<String, String> header = getHeader();
        if (header != null && header.size() > 0) {
            request.setHeaders(header);
        }
        request.setShouldCache(cache);
        request.setRetryPolicy(new DefaultRetryPolicy(
                DEFAULT_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }


    /**
     * 获取Map格式请求参数
     */
    private TreeMap<String, String> getRequestParams() {
        TreeMap<String, String> filedMap = new TreeMap<String, String>();
        // 反射publicFiled类的所有字段
        Class cla = requestParamBody.getClass();

        // 获得该类下面所有的字段集合
        Field[] filed = cla.getDeclaredFields();
        for (Field fd : filed) {
            String filedName = fd.getName();
            String firstLetter = filedName.substring(0, 1).toUpperCase(); // 获得字段第一个字母大写
            String getMethodName = "get" + firstLetter + filedName.substring(1); // 转换成字段的get方法

            try {
                java.lang.reflect.Method getMethod = cla.getMethod(getMethodName, new Class[]{});
                Object value = getMethod.invoke(requestParamBody, new Object[]{}); // 这个对象字段get方法的值
                filedMap.put(filedName, value + ""); // 添加到Map集合

            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        return filedMap;
    }

    /**
     * 获取Json类型的请求参数
     */
    protected String getRequestJsonString() {
        return mGson.toJson(requestParamBody);
    }


    /**
     * 域名处理
     *
     * @return
     */
    private String buildRequestDomain() {
        String domain = getDomain();
        return TextUtils.isEmpty(domain) ? "" : domain;
    }

    ;


    /**
     * 构造Get请求Url
     *
     * @param protocol 协议名
     * @param params   Get请求传参
     * @return
     */
    private String buildGetRequestUrl(String protocol, Map<String, String> params) {
        if (TextUtils.isEmpty(protocol)) {
            throw new IllegalArgumentException(
                    "argument protocol can not be null or \"\"");
        }
        StringBuilder builder = new StringBuilder(buildRequestDomain());
        builder.append(protocol);
        builder.append("?");
        int i = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (i++ > 0) {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=")
                    .append(entry.getValue());
        }
        return builder.toString();
    }


    /**
     * 构造Post请求Url
     *
     * @param protocol 协议名
     * @return
     */
    private String buildPostRequestUrl(String protocol) {
        if (TextUtils.isEmpty(protocol)) {
            throw new IllegalArgumentException(
                    "argument protocol can not be null or \"\"");
        }
        StringBuilder builder = new StringBuilder(buildRequestDomain());
        builder.append(protocol);
        return builder.toString();
    }

}
