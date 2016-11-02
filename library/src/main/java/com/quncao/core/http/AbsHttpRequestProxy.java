package com.quncao.core.http;

import android.text.TextUtils;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GZipRequest;
import com.android.volley.request.GsonRequest;
import com.google.gson.Gson;
import com.quncao.core.http.annotation.HttpReqParam.HttpReqMethod;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
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
    protected String tag;
    protected boolean gzip = false;
    protected boolean cache = false;
    private Map<String,String> header = new HashMap<String,String>();


    /**
     * 域名
     *
     * @return
     */
    protected abstract String getDomain();

    /**
     * 获取公共参数
     *
     * @return
     */
    protected abstract TreeMap<String, String> getCommonParamMap();


    public interface RequestListener<T> {
        public void onSuccess(T response);

        public void onFailed(VolleyError error);
    }


  /*  @SuppressWarnings("unchecked")
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
        Map<String, String> params = getRequestParamMap(requestParamBody);
        String requestUrl = buildGetRequestUrl(protocal, params);
        if (gzip) {
            request = new GZipRequest(requestUrl, new Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!TextUtils.isEmpty(response)) {
                        try {
                            listener.onSuccess(new Gson().fromJson(response, clazz));
                        } catch (Exception error) {
                            listener.onFailed(new VolleyError(error.getMessage()));
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onFailed(error);
                }
            });
            header.put("Accept-Encoding", "gzip,deflate");

        } else {
            request = new GsonRequest<T>(requestUrl, clazz, null,
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
        }
        if (!TextUtils.isEmpty(tag)) {
            request.setTag(tag);
        }
        request.setHeaders(header);
        request.setShouldCache(cache);
        request.setRetryPolicy(new DefaultRetryPolicy(
                DEFAULT_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }


    /**
     * post方式请求
     */
    private void doPost() {
        RequestQueue requestQueue = HttpRequestManager.getInstance().getRequestQueue();
        String requestUrl = buildPostRequestUrl(protocal);
        TreeMap<String, String> params = getCommonParamMap();
        if (params == null) {
            params = new TreeMap<String, String>();
        }
        params.putAll(getRequestParamMap(requestParamBody));
        if (gzip) {
            request = new GZipRequest(Method.POST, requestUrl, new Listener<String>() {
                @Override
                public void onResponse(String response) {
                    if (!TextUtils.isEmpty(response)) {
                        try {
                            listener.onSuccess(new Gson().fromJson(response, clazz));
                        } catch (Exception error) {
                            listener.onFailed(new VolleyError(error.getMessage()));
                        }
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    listener.onFailed(error);
                }
            });
            header.put("Accept-Encoding", "gzip,deflate");

        } else {
            request = new GsonRequest<T>(Method.POST, requestUrl, clazz, null,
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
        }
        if (!TextUtils.isEmpty(tag)) {
            request.setTag(tag);
        }
        request.setHeaders(header);
        request.setShouldCache(cache);
        request.setRetryPolicy(new DefaultRetryPolicy(
                DEFAULT_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }


    /**
     * 获取请求参数
     */
    private TreeMap<String, String> getRequestParamMap(Object publicFiled) {
        TreeMap<String, String> filedMap = new TreeMap<String, String>();
        // 反射publicFiled类的所有字段
        Class cla = publicFiled.getClass();

        // 获得该类下面所有的字段集合
        Field[] filed = cla.getDeclaredFields();
        for (Field fd : filed) {
            String filedName = fd.getName();
            String firstLetter = filedName.substring(0, 1).toUpperCase(); // 获得字段第一个字母大写
            String getMethodName = "get" + firstLetter + filedName.substring(1); // 转换成字段的get方法

            try {
                java.lang.reflect.Method getMethod = cla.getMethod(getMethodName, new Class[]{});
                Object value = getMethod.invoke(publicFiled, new Object[]{}); // 这个对象字段get方法的值
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
     * 获取公共请求参数
     *
     * @return
     */
    private String getCommonParamString() {
        TreeMap<String, String> commonParams = getCommonParamMap();
        if (commonParams == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int paramNum = 0;
        for (Map.Entry entry : commonParams.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            if (paramNum != commonParams.size() - 1) {
                sb.append("&");
            }
            paramNum++;
        }

        return sb.toString();

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
        builder.append(getCommonParamString());
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.append("&").append(entry.getKey()).append("=")
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
