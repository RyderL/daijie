package org.daijie.core.util.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 
 * @author daijie
 * @date 2017年6月5日
 * 获取http会话数据的工具类
 * 
 */
public class HttpConversationUtil {
	
	private static Logger logger = LoggerFactory.getLogger(HttpConversationUtil.class);

	public static final String TOKEN_NAME = "token";
	
	/**
	 * 获取当前请求会话
	 * @return
	 */
	public static HttpServletRequest getRequest(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
	}
	
	/**
	 * 获取当前响应会话
	 * @return
	 */
	public static HttpServletResponse getResponse(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
	}

	/**
	 * 获取当前请求会话IP
	 * @return
	 */
	public static String getIP(){
		String ip = getRequest().getHeader("x-forwarded-for");
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = getRequest().getHeader("Proxy-Client-IP");
	    }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = getRequest().getHeader("WL-Proxy-Client-IP");
	    }
	    if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
	        ip = getRequest().getRemoteAddr();
	    }
	    return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
	}

	/**
	 * 获取当前请求会话IP城市地址
	 * @return
	 */	
	public String getIPAddress(){
		try {
			return MacAddressUtil.ip2Location(getIP());
		} catch (Exception e) {
			logger.error("IP Address not found!");
			return "IP Address not found!";
		}
	}

	/**
	 * 获取当前请求会话token
	 * @param tokenName 
	 * @return
	 */
	public static String getToken(){
		if(getRequest() != null){
			if(!StringUtils.isEmpty(getRequest().getAttribute(TOKEN_NAME))){
				return getRequest().getAttribute(TOKEN_NAME).toString();
			}else if(!StringUtils.isEmpty(getRequest().getParameter(TOKEN_NAME))){
				return getRequest().getParameter(TOKEN_NAME);
			}else if(!StringUtils.isEmpty(getRequest().getHeader(TOKEN_NAME))){
				return getRequest().getHeader(TOKEN_NAME);
			}else if(!StringUtils.isEmpty(CookieUtil.get(TOKEN_NAME))){
				return CookieUtil.get(TOKEN_NAME);
			}else if(!StringUtils.isEmpty(getRequest().getSession().getAttribute(TOKEN_NAME))){
				return (String) getRequest().getSession().getAttribute(TOKEN_NAME);
			}
		}
		return null;
	}
	
	/**
	 * 获取请求Body转换为json字符串
	 * 
	 * @param request
	 * @return
	 */
	public static String getBodyString() {
		StringBuilder sb = new StringBuilder();
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = getRequest().getInputStream();
			reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8")));
			String line = "";
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
}
