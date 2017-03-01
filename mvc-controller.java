package test.*.*.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.xml.bind.annotation.XmlRootElement;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.dom4j.Document;
import org.json.JSONException;
import org.json.XML;

import test.test.mapper.jaxb.JaxbFactory;
import test.test.mapper.jaxb.JaxbMapper;
import test.test.mapper.json.JsonMapper;
import test.test.spring.SpringContextLivesite;
import test.interwoven.livesite.common.web.ForwardAction;
import test.interwoven.livesite.common.web.StopForwardAction;
import test.interwoven.livesite.runtime.RequestContext;


public class Controller extends Livesite {
    

    public ForwardAction renderPng(RequestContext context) {
        init(context);
        create("image/" + PNG_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderPngWithCache(RequestContext context) {
        init(context);
        cache("image/" + PNG_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderPngWithLocalCache(RequestContext context) {
        localCache = true;
        return renderPngWithCache(context);
    }

    public ForwardAction renderJpeg(RequestContext context) {
        init(context);
        create("image/" + JPEG_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderJpegWithCache(RequestContext context) {
        init(context);
        cache("image/" + JPEG_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderJpegWithLocalCache(RequestContext context) {
        localCache = true;
        return renderJpegWithCache(context);
    }

    public ForwardAction renderText(RequestContext context) {
        init(context);
        create(TEXT_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderTextWithCache(RequestContext context) {
        init(context);
        cache(TEXT_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderTextWithLocalCache(RequestContext context) {
        localCache = true;
        return renderTextWithCache(context);
    }

    public ForwardAction renderHtml(RequestContext context) {
        init(context);
        create(HTML_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderHtmlWithCache(RequestContext context) {
        init(context);
        cache(HTML_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderHtmlWithLocalCache(RequestContext context) {
        localCache = true;
        return renderHtmlWithCache(context);
    }

    public ForwardAction renderXML(RequestContext context) {
        init(context);
        create(XML_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderXMLWithCache(RequestContext context) {
        init(context);
        cache(XML_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderXMLWithLocalCache(RequestContext context) {
        localCache = true;
        return renderXMLWithCache(context);
    }

    public ForwardAction renderJSON(RequestContext context) {
        init(context);
        create(JSON_TYPE);
        return new StopForwardAction();
    }

    
    public ForwardAction renderJSONWithAllowAll(RequestContext context) {
        init(context);
        addCorsHeader(context);
        create(JSON_TYPE);
        return new StopForwardAction();
    }
    
    public ForwardAction renderJSONWithCache(RequestContext context) {
        init(context);
        cache(JSON_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction renderJSONWithLocalCache(RequestContext context) {
        localCache = true;
        return renderJSONWithCache(context);
    }

    public ForwardAction renderJSONP(RequestContext context) {
        init(context);
        create(JS_TYPE);
        return new StopForwardAction();
    }

    public ForwardAction render(RequestContext context) {
        init(context);
        Object result = null;
        try {
            result = doAction();
        } catch (Exception e) {
            logger.error("Controller error", e);
        }
        if (result instanceof ForwardAction) {
            return (ForwardAction) result;
        }
        return new StopForwardAction();
    }

    protected Object execute() throws Exception {
        return doAction();
    }

    private void create(String contentType) {
        output(getResult(contentType), contentType);
    }

    private void cache(String contentType) {
        Ehcache ehcache = (Ehcache) SpringContextLivesite.getInstance().getBean("fskk.livesite.contentCache");
        fromCache(contentType, ehcache);
    }

    private void fromCache(String contentType, Ehcache cache) {
        String cacheKey = getAjaxCacheKey(contentType);
        Element element = cache.get(cacheKey);
        byte[] content = null;
        if (element == null) {
            content = getResult(contentType);
            if (content != null) {
                element = new net.sf.ehcache.Element(cacheKey, content);
                cache.put(element);
            }
        } else {
            content = (byte[]) element.getObjectValue();
        }
        if (!isNotModified(element)) {
            output(content, contentType);
        }
    }

    protected boolean isNotModified(RequestContext context, Element element) {
        setRequest(context.getRequest());
        setResponse(context.getResponse());
        return isNotModified(element);
    }

    protected boolean isNotModified(Element element) {
        if (element == null) {
            return false;
        }
        String etag = "W/\"" + (String) element.getKey() + "\"";
        SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_GMT_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Locale.setDefault(Locale.ENGLISH);
        String lastModified = sdf.format(new Date(element.getCreationTime()));

        if (etag.equals(getRequest().getHeader("If-None-Match"))
                && (lastModified != null && lastModified.equals(getRequest().getHeader("If-Modified-Since")))) {
            getResponse().setStatus(304);
            return true;
        } else {
            long maxAge = 0;
            Date now = new Date();
            maxAge = (element.getExpirationTime() - now.getTime()) / 1000;
            getResponse().setHeader("Etag", etag);
            getResponse().setHeader("Last-Modified", lastModified);
            if (localCache && maxAge > 0) {
                getResponse().setHeader("Cache-Control", "max-age=" + maxAge);
            }
            return false;
        }
    }

    private Object doAction() throws Exception {
        Method action = this.getClass().getMethod(getAction());
        return action.invoke(this);
    }

    @XmlRootElement(name = "result")
    protected class Result {
        private String message;

        public void setMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    protected String xmlToJson(String xml) throws JSONException {
        xml = xml.replaceAll("(>)([\\+\\-]?[0-9]+|[\\+\\-]?[0-9]+.[0-9]+)(</)", "$1<![CDATA[$2]]>$3");
        xml = xml.replaceAll("(=\\\")([\\+\\-]?[0-9]+|[\\+\\-]?[0-9]+.[0-9]+)(\\\")", "$1<![CDATA[$2]]>$3");
        return XML.toJSONObject(xml).toString().replaceAll("<!\\[CDATA\\[", "").replaceAll("\\]\\]>", "");
    }

    protected String asXML(Object bean) {
        if (bean != null) {
            if (bean instanceof Collection<?>) {
                Collection<?> collection = ((Collection<?>) bean);
                if (!collection.isEmpty()) {
                    JaxbMapper mapper = JaxbFactory.getInstance().getMapper(collection.iterator().next().getClass());
                    return mapper.toXml(collection, "list", DEFAULT_ENCODING);
                }
            } else {
                JaxbMapper mapper = JaxbFactory.getInstance().getMapper(bean.getClass());
                List<Object> collection = new ArrayList<Object>();
                collection.add(bean);
                return mapper.toXml(collection, "list", DEFAULT_ENCODING);
            }
        }
        return null;
    }

    private byte[] getResult(String contentType) {
        byte[] content = null;
        try {
            Object result = execute();
            if (result instanceof byte[]) {
                content = (byte[]) result;
            } else if (result instanceof String) {
                content = ((String) result).getBytes(DEFAULT_ENCODING);
            } else if (result instanceof Document) {
                String xmlString = ((Document) result).asXML();
                if (JSON_TYPE.equals(contentType)) {
                    content = xmlToJson(xmlString).getBytes(DEFAULT_ENCODING);
                } else if (XML_TYPE.equals(contentType)) {
                    content = xmlString.getBytes(DEFAULT_ENCODING);
                }
            } else {
                if (JSON_TYPE.equals(contentType)) {
                    content = JsonMapper.buildNormalMapper().toJson(result).getBytes(DEFAULT_ENCODING);
                } else if (XML_TYPE.equals(contentType)) {
                    content = asXML(result).getBytes(DEFAULT_ENCODING);
                } else if (JS_TYPE.equals(contentType)) {
                    content = JsonMapper.buildNormalMapper().toJsonP(getRequest().getParameter("callback"), result)
                            .getBytes(DEFAULT_ENCODING);
                }
            }

        } catch (Exception e) {
            logger.error("Controller error", e);
            return null;
        }

        return content;
    }

    private void output(byte[] bytes, String contentType) {
        if (bytes != null) {
            ServletOutputStream outputStream = null;
            try {
                getResponse().setContentType(contentType);
                outputStream = getResponse().getOutputStream();
                outputStream.write(bytes);
            } catch (IOException e) {
                logger.error("Controller error", e);
                getResponse().setStatus(500);
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    logger.error("Controller error", e);
                    getResponse().setStatus(500);
                }
            }
        } else {
            logger.error("No result");
            getResponse().setStatus(500);
        }
    }

    public boolean isLocalCache() {
        return localCache;
    }

    public void setLocalCache(boolean localCache) {
        this.localCache = localCache;
    }
	
	protected static final String HTML_TYPE = "text/html";
    protected static final String JS_TYPE = "text/javascript";
    protected static final String JSON_TYPE = "application/json";
    protected static final String XML_TYPE = "text/xml";
    protected static final String TEXT_TYPE = "text/plain";
    protected static final String PNG_TYPE = "png";
    protected static final String JPEG_TYPE = "jpeg";
    protected static final String STANDARD_GMT_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

    private boolean localCache;
}
