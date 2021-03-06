package com.quncao.core.http.request;


import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.error.ParseError;
import com.android.volley.request.JsonRequest;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by pengjin on 2016/11/8.
 * 继承JsonRequest，支持gzip模式
 */

public class JsonObjectRequestEX<T> extends JsonRequest<T> {
    private final Gson mGson = new Gson();
    private final Class<T> mClazz;

    /**
     * Creates a new request.
     *
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param jsonRequest   A {@link JSONObject} to post with the request. Null is allowed and
     *                      indicates no parameters will be posted along with request.
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public JsonObjectRequestEX(int method, String url, String jsonRequest, Class<T> clazz,
                               Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.mClazz = clazz;
    }

    /*public JsonObjectRequestEX(int method, String url, String jsonRequest, Class<T> clazz, boolean gzip,
                               Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(method, url, (jsonRequest == null) ? null : (jsonRequest.length() == 0) ? "" : jsonRequest.toString(), listener,
                errorListener);
        this.mClazz = clazz;
        this.mGzip = gzip;
    }*/

    /**
     * Constructor which defaults to <code>GET</code> if <code>jsonRequest</code> is
     * <code>null</code>, <code>POST</code> otherwise.
     */
/*    public JsonObjectRequestEX(String url, String jsonString, Class<T> clazz, boolean gzip, Response.Listener<T> listener,
                               Response.ErrorListener errorListener) {
        this(jsonRequest == null ? Method.GET : Method.POST, url, jsonRequest, clazz, gzip,
                listener, errorListener);
    }*/
    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String json = "";
        Map<String,String> header = response.headers;
        String encoding = header.get("Content-Encoding");
        try {
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                GZIPInputStream gStream = new GZIPInputStream(new ByteArrayInputStream(response.data));
                InputStreamReader reader = new InputStreamReader(gStream);
                BufferedReader in = new BufferedReader(reader);
                String read;
                while ((read = in.readLine()) != null) {
                    json += read;
                }
                reader.close();
                in.close();
                gStream.close();
            } else {
                json = new String(
                        response.data, HttpHeaderParser.parseCharset(response.headers));
            }
        } catch (IOException e) {
            return Response.error(new ParseError());
        } catch (JsonSyntaxException e) {
            return Response.error(new ParseError(e));
        }
        return Response.success(
                mGson.fromJson(json, mClazz), HttpHeaderParser.parseCacheHeaders(response));
    }
}
