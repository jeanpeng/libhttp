package com.quncao.core.http.request;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.error.ParseError;
import com.android.volley.request.GsonRequest;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by pengjin on 2016/11/8.
 * 继承了GsonRequest，支持gzip
 */

public class GsonRequestEX<T> extends GsonRequest {
    private final Gson mGson = new Gson();
    private final Class<T> mClazz;

    public GsonRequestEX(String url, Class clazz, Map headers, Response.Listener listener, Response.ErrorListener errorListener) {
        super(url, clazz, headers, listener, errorListener);
        this.mClazz = clazz;
    }

    public GsonRequestEX(int type, String url, Class clazz, Map headers, Map params, Response.Listener listener, Response.ErrorListener errorListener) {
        super(type, url, clazz, headers, params, listener, errorListener);
        this.mClazz = clazz;
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        String json = "";
        Map<String, String> header = response.headers;
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
