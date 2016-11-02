package com.quncao.core.http.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @ClassName: HttpReqParam.java
 * @Description: 协议名和Response类型
 * 
 * @author pengjin
 * @version V1.0
 * @Date 2015-10-24 下午4:44:59
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpReqParam {

	public enum HttpReqMethod {
		HTTP_GET, HTTP_POST
	}

	String protocal();

	Class<?> responseType();

	HttpReqMethod method() default HttpReqMethod.HTTP_GET;

}
