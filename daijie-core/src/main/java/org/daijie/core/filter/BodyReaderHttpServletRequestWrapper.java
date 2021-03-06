package org.daijie.core.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.daijie.core.controller.enums.JSONType;
import org.daijie.core.util.http.HttpConversationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 将请求body参数转换为form-data
 * @author daijie
 * @since 2017年6月13日
 */
@SuppressWarnings({"unchecked","rawtypes"})
public class BodyReaderHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private static Logger logger = LoggerFactory.getLogger(BodyReaderHttpServletRequestWrapper.class);

	private final byte[] body;

	private Map<String, Object> params = new HashMap<String, Object>();

	public BodyReaderHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		params.putAll(request.getParameterMap());
		String bodyString = HttpConversationUtil.getBodyString();
		logger.info("params = {}", params);
		logger.info("body = {}", bodyString);
		if (bodyString == null || "".equals(bodyString)) {
			body = new byte[0];
			return;
		}
		body = bodyString.getBytes(Charset.forName("UTF-8"));
		
		
		if (request.getContentType().contains("application/json")) {
			String param = new String(body, Charset.forName("UTF-8"));
			// json串 转换为Map
			if (param != null & param.contains("=")) {
				param = param.split("=")[1];
			}
			ObjectMapper mapper = new ObjectMapper();
			if(JSONType.getJSONType(param).equals(JSONType.JSON_TYPE_OBJECT)){
				params.putAll(mapper.readValue(param, Map.class)); //json转换成map
			}
		}
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {

		final ByteArrayInputStream bais = new ByteArrayInputStream(body);

		return new ServletInputStream() {

			@Override
			public boolean isFinished() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean isReady() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void setReadListener(ReadListener listener) {
				// TODO Auto-generated method stub

			}

			@Override
			public int read() throws IOException {
				// TODO Auto-generated method stub
				// return 0;
				return bais.read();
			}
		};
	}

	@Override
	public String getHeader(String name) {
		return super.getHeader(name);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return super.getHeaderNames();
	}

	@Override
	public Enumeration<String> getHeaders(String name) {
		return super.getHeaders(name);
	}

	@Override
	public Map getParameterMap() {
		return params;
	}

	@Override
	public Enumeration getParameterNames() {
		Vector<Object> l = new Vector<Object>(params.keySet());
		return l.elements();
	}

	@Override
	public String[] getParameterValues(String name) {
		Object v = params.get(name);
		if (v == null) {
			return null;
		} else if (v instanceof String[]) {
			return (String[]) v;
		} else if (v instanceof String) {
			return new String[] { (String) v };
		} else {
			return new String[] { v.toString() };
		}
	}

	@Override
	public String getParameter(String name) {
		Object v = params.get(name);
		if (v == null) {
			return null;
		} else if (v instanceof String[]) {
			String[] strArr = (String[]) v;
			if (strArr.length > 0) {
				return strArr[0];
			} else {
				return null;
			}
		} else if (v instanceof String) {
			return (String) v;
		} else {
			return v.toString();
		}
	}

}