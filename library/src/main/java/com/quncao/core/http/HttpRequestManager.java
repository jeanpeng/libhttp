package com.quncao.core.http;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;


/**
 * @author pengjin
 * @version V1.0
 * @ClassName: HttpRequestManager.java
 * @Description: 网络请求管理，维护一个请求队列
 * @Date 2015-10-24 上午10:14:49
 */
public class HttpRequestManager {

    private static HttpRequestManager mInstance;

    /**
     * 请求队列
     */
    private RequestQueue mRequestQueue;

    private Context mContext;


    private HttpRequestManager() {

    }

    public static HttpRequestManager getInstance() {
        if (mInstance == null) {
            synchronized (HttpRequestManager.class) {
                if (mInstance == null) {
                    mInstance = new HttpRequestManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化请求队列
     *
     * @param context
     */
    public void init(Context context) {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(context);
            mContext = context;
        }
    }


    public void cancelAll(String tag) {
        mRequestQueue.cancelAll(tag);
    }


    /**
     * 获取请求队列
     *
     * @return
     */
    RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }

}
