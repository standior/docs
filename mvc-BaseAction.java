 
package test.livesite.m.core;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import test.core.livesite.LivesiteContext;
import test.core.spring.SpringContextLivesite;
import test.livesite.m.core.annotation.IdentifyUser;
import test.livesite.m.core.annotation.LivesiteResource;
import test.livesite.m.util.filter.Filter;
import test.livesite.service.LoginService;
import com.interwoven.livesite.external.ParameterHash;
import com.interwoven.livesite.runtime.RequestContext;


public abstract class BaseAction {

	
	protected void init(RequestContext context) {
		LivesiteContext.setContext(context);
		setContext(context);
		setServletContext(context.getRequest().getSession().getServletContext());
		setRequest(context.getRequest());
		setResponse(context.getResponse());
		setSession(context.getRequest().getSession(true));
		injectSpring(this);
		injectLivesiteResource(this);
		injectParameters(this, "");
	}

	@SuppressWarnings("unchecked")
	protected int keyCode() {
		Map<String, String[]> parameterMap = context.getRequest()
				.getParameterMap();
		String[] excludes = { "submit", "componentID" };

		Map<String, String> keyMap = new HashMap<String, String>();
		Iterator<String> it = parameterMap.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			if (Arrays.asList(excludes).contains(key)) {
				continue;
			}
			String[] values = parameterMap.get(key);
			StringBuilder value = new StringBuilder("[");
			for (int i = 0, j = values.length; i < j; i++) {
				value.append(values[i]);
				if (i != j - 1) {
					value.append(",");
				}
			}
			value.append("]");
			keyMap.put(key, value.toString());
		}
		return keyMap.hashCode();
	}

	protected int keyCode(RequestContext context) {
		setContext(context);
		return keyCode();
	}

	protected RequestContext getContext() {
		return context;
	}

	protected void setContext(RequestContext context) {
		this.context = context;
	}

	protected ServletContext getServletContext() {
		return servletContext;
	}

	protected void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

	protected HttpServletRequest getRequest() {
		return request;
	}

	protected void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	protected HttpServletResponse getResponse() {
		return response;
	}

	protected void setResponse(HttpServletResponse response) {
		this.response = response;
	}

	protected HttpSession getSession() {
		return session;
	}

	protected void setSession(HttpSession session) {
		this.session = session;
	}

	protected String getParameter(String key) {
		return context.getParameterString(key);
	}

	protected String[] getParameterValues(String key) {
		ParameterHash paramHash = context.getParameters();
		if (paramHash.get(key) != null) {
			if (paramHash.get(key) instanceof AbstractList<?>) {
				return ((AbstractList<?>) paramHash.get(key))
						.toArray(new String[] {});
			}
			return new String[] { context.getParameterString(key) };
		}
		return null;
	}

	protected void setAttribute(String key, Object value) {
		if (context.getPageScopeData() != null) {
			context.getPageScopeData().put(key, value);
		}
		request.setAttribute(key, value);
	}

	protected Object getAttribute(String key) {
		return request.getAttribute(key);
	}

	protected String getAction() {
		return getAction(getContext());
	}

	protected String getAction(RequestContext context) {
		return context.getParameterString("iwPreActions");
	}

	protected String getLoginAccountId() {
		LoginService loginService = (LoginService) SpringContextLivesite
				.getInstance().getBean("fskk.livesite.loginService");
		if (loginService.isLogin()) {
			return loginService.getUser().getAccountId();
		}
		return null;
	}

	protected String getPageCacheKey() {
		return getPageCacheKey(getContext().getPageName(), getContext()
				.getComponentId(),
				this.getClass().isAnnotationPresent(IdentifyUser.class));
	}

	protected String getPageCacheKey(String pageName, String componentId,
			boolean identifyUser) {
		Map<String, String> cacheKeyMap = new HashMap<String, String>();
		cacheKeyMap.put("pageName", pageName);
		cacheKeyMap.put("componentId", componentId);
		if (identifyUser) {
			String accountId = getLoginAccountId();
			if (accountId != null) {
				cacheKeyMap.put("accountId", accountId);
			}
		}
		return String.valueOf(cacheKeyMap.hashCode()) + keyCode();
	}

	protected String getAjaxCacheKey(String contentType) {
		Map<String, String> cacheKeyMap = new HashMap<String, String>();
		cacheKeyMap.put("controller", getClass().getCanonicalName());
		cacheKeyMap.put("action", getAction());
		cacheKeyMap.put("contentType", contentType);
		try {
			if (this.getClass().getMethod(getAction())
					.isAnnotationPresent(IdentifyUser.class)) {
				String accountId = getLoginAccountId();
				if (accountId != null) {
					cacheKeyMap.put("accountId", accountId);
				}
			}
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		return String.valueOf(cacheKeyMap.hashCode()) + keyCode();
	}

	protected String getCookie(String cookieName) {
		HttpServletRequest request = getContext().getRequest();
		Cookie[] cookies = request.getCookies();
		String cookieValue = "";
		if (cookies != null) {
			for (int i = 0, j = cookies.length; i < j; i++) {
				if (cookies[i].getName().equalsIgnoreCase(cookieName)) {
					cookieValue = cookies[i].getValue();
				}
			}
		}
		return cookieValue;
	}

	protected String filter(Field field, String input)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		String value = input;
		Annotation[] annotations = field.getAnnotations();
		for (Annotation annotation : annotations) {
			if ("test.livesite.m.util.filter.annotation"
					.equals(annotation.annotationType().getPackage().getName())) {
				Filter filter = (Filter) Class.forName(
						new StringBuilder(
								"test.livesite.m.util.filter.")
								.append(annotation.annotationType()
										.getSimpleName()).append("Filter")
								.toString()).newInstance();
				value = filter.filter(value, annotation);
			}
		}
		return value;
	}

	private void injectSpring(Object obj) {
		try {
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(Autowired.class)) {
					if (field.getAnnotation(Autowired.class).required()) {
						field.setAccessible(true);
						String qualifier = field.getName();
						if (field.isAnnotationPresent(Qualifier.class)) {
							qualifier = field.getAnnotation(Qualifier.class)
									.value();
						}
						field.set(obj, SpringContextLivesite.getInstance()
								.getBean(qualifier));
					}
				}
			}
		} catch (SecurityException e) {
			logger.error("Controller error", e);
		} catch (IllegalArgumentException e) {
			logger.error("Controller error", e);
		} catch (IllegalAccessException e) {
			logger.error("Controller error", e);
		}
	}

	private void injectLivesiteResource(Object obj) {
		try {
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(LivesiteResource.class)) {
					field.setAccessible(true);
					Object resource = field.getType().newInstance();
					injectSpring(resource);
					field.set(obj, resource);
				}
			}
		} catch (SecurityException e) {
			logger.error("Controller error", e);
		} catch (IllegalArgumentException e) {
			logger.error("Controller error", e);
		} catch (IllegalAccessException e) {
			logger.error("Controller error", e);
		} catch (InstantiationException e) {
			logger.error("Controller error", e);
		}
	}

	@SuppressWarnings("unchecked")
	private void injectParameters(Object obj, String prefix) {
		try {
			Field[] fields = obj.getClass().getDeclaredFields();
			for (Field field : fields) {
				Method method = null;
				try {
					method = obj.getClass().getDeclaredMethod(
							new StringBuilder("set")
									.append(field.getName().substring(0, 1)
											.toUpperCase())
									.append(field.getName().substring(1))
									.toString(), field.getType());
				} catch (NoSuchMethodException e) {
					continue;
				}
				if (method.getModifiers() == Modifier.PUBLIC) {
					if ("java.lang.String".equals(field.getType()
							.getCanonicalName())) {
						if (getParameter(prefix + field.getName()) != null) {
							String value = filter(field, getParameter(prefix
									+ field.getName()));
							method.invoke(obj, value);
						}
					} else if ("java.util.List".equals(field.getType()
							.getCanonicalName())) {
						if (getParameterValues(prefix + field.getName()) != null) {
							List<String> list = new ArrayList<String>();
							String[] values = getParameterValues(prefix
									+ field.getName());
							for (String value : values) {
								list.add(filter(field, value));
							}
							method.invoke(obj, list);
						}
					} else if (!field.getType().getCanonicalName()
							.startsWith("java.lang.")
							&& !field.getType().isPrimitive()) {
						Iterator<String> iterator = getContext()
								.getParameters().keySet().iterator();
						boolean isContained = false;
						while (iterator.hasNext()) {
							if (iterator.next().startsWith(
									prefix + field.getName() + ".")) {
								isContained = true;
							}
						}
						if (isContained) {
							Object subObj = field.getType().newInstance();
							method.invoke(obj, subObj);
							injectParameters(subObj, prefix + field.getName()
									+ ".");
						}
					}
				}
			}
		} catch (SecurityException e) {
			logger.error("Controller error", e);
		} catch (IllegalArgumentException e) {
			logger.error("Controller error", e);
		} catch (IllegalAccessException e) {
			logger.error("Controller error", e);
		} catch (InvocationTargetException e) {
			logger.error("Controller error", e);
		} catch (InstantiationException e) {
			logger.error("Controller error", e);
		} catch (ClassNotFoundException e) {
			logger.error("Controller error", e);
		}
	}

	protected byte[] actionAsXmlBytes(String action, String method) {
		try {
			return actionAsDocument(action, method).asXML().getBytes(
					DEFAULT_ENCODING);
		} catch (UnsupportedEncodingException e) {
			logger.error("Controller error", e);
		}
		return null;
	}

	protected void addCorsHeader(RequestContext context) {

		if (this.response == null) {
			setResponse(context.getResponse());
		}
		// TODO: externalize the Allow-Origin
		this.response.addHeader("Access-Control-Allow-Origin", "*");
		this.response.addHeader("Access-Control-Allow-Methods",
				"POST, GET, OPTIONS, PUT, DELETE, HEAD");
		this.response.addHeader("Access-Control-Allow-Headers",
				"X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
		this.response.addHeader("Access-Control-Max-Age", "1728000");
	}

	@SuppressWarnings("unchecked")
	protected Document actionAsDocument(String action, String method) {
		ParameterHash parameterMap = getContext().getParameters();
		parameterMap.put("targetClass", action);
		parameterMap.put("targetMethod", method);
		Document result = new test.core.livesite.External()
				.execute(getContext());
		if (result.getRootElement().getName().equals("Error")) {
			throw new RuntimeException(
					"exception happened while action executing.");
		}
		return result;
	}
	
	protected static final String DEFAULT_ENCODING = "UTF-8";

	protected Log logger = LogFactory.getLog(this.getClass());

	private RequestContext context;
	private ServletContext servletContext;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private HttpSession session;

}
