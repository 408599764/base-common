package com.godnb.base.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.rmi.server.RemoteServer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Http工具类
 * @author licong
 * @date 2017-3-20
 */
public class HttpUtil{

    private static Log log = LogFactory.getLog(HttpUtil.class);

    private static List<String> httpHeaderNames = Arrays.asList(new String[]{"accept", "accept-charset", "accept-encoding", "accept-language", "accept-ranges", "authorization",
            "cache-control", "connection", "cookie", "content-length", "content-type", "date", "expect", "from", "host", "if-match", "if-modified-since", "if-none-match",
            "if-range", "if-unmodified-since", "max-forwards", "pragma", "proxy-authorization", "range", "referer", "te", "upgrade", "user-agent", "via", "warning"});

    /**
     * SpringMVC跳转
     * @param url
     * @param params 只支持Map<String, String>或bean(String类型的属性)
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ModelAndView redirect(String url, Object params){
        ModelAndView view = new ModelAndView("redirect:" + url);
        Map<String, String> modelMap = new HashMap<String, String>();
        if(params != null){
            if(params instanceof Map){
                modelMap = (Map<String, String>)params;

                Iterator<Entry<String, String>> itr = modelMap.entrySet().iterator();
                while(itr.hasNext()){
                    Entry<String, String> entry = itr.next();
                    //过虑非String属性
                    if(!(entry.getValue() instanceof String)){
                        itr.remove();
                    }
                }
            }
            else{
                try{
                    BeanInfo beanInfo = Introspector.getBeanInfo(params.getClass());
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                    for(PropertyDescriptor property : propertyDescriptors){
                        String key = property.getName();

                        //过滤class属性
                        if(key.equals("class")){
                            continue;
                        }

                        //得到property对应的getter方法
                        Method getter = property.getReadMethod();
                        if(getter == null){
                            continue;
                        }

                        Object value = getter.invoke(params);

                        //过虑非String属性
                        if(value instanceof String){
                            modelMap.put(key, (String)value);
                        }
                    }
                }
                catch(Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }

        view.addAllObjects(modelMap);
        return view;
    }

    /**
     * servlet重定向参数组装
     * @param params
     * @return
     */
    @SuppressWarnings("unchecked")
    public static String redirectMessage(Object params){
        List<String> urlParams = new ArrayList<String>();
        if(params != null){
            if(params instanceof Map){
                Map<String, Object> map = (Map<String, Object>)params;
                Iterator<Entry<String, Object>> itr = map.entrySet().iterator();
                while(itr.hasNext()){
                    Entry<String, Object> entry = itr.next();
                    //过虑非String属性
                    if((entry.getValue() instanceof String)){
                        urlParams.add(entry.getKey() + "=" + entry.getValue());
                    }
                }
            }
            else{
                try{
                    BeanInfo beanInfo = Introspector.getBeanInfo(params.getClass());
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                    for(PropertyDescriptor property : propertyDescriptors){
                        String key = property.getName();
                        //过滤class属性
                        if(key.equals("class")){
                            continue;
                        }
                        //得到property对应的getter方法
                        Method getter = property.getReadMethod();
                        if(getter == null){
                            continue;
                        }
                        Object value = getter.invoke(params);
                        //过虑非String属性
                        if(value instanceof String){
                            urlParams.add(key + "=" + value);
                        }
                    }
                }
                catch(Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        return StringUtil.join(urlParams, "&");
    }

    /**
     * 获取客户端IP地址
     * @param request
     * @return
     */
    public static String getRemoteAddr(HttpServletRequest request){
        String remote_addr = request.getHeader("X-Real-IP");//nginx反向代理
        if(StringUtils.isNotBlank(remote_addr)){
            return remote_addr;
        }

        remote_addr = request.getHeader("X-Forwarded-For");//apache反向代理
        if(StringUtils.isNotBlank(remote_addr)){
            String[] ips = remote_addr.split(",");
            for(String ip : ips){
                if(!"null".equalsIgnoreCase(ip)){
                    return ip;
                }
            }
        }

        return request.getRemoteAddr();
    }

    @SuppressWarnings("unchecked")
    public static ModelAndView forward(String url, Object params){
        ModelAndView view = new ModelAndView(url);
        Map<String, String> modelMap = new HashMap<String, String>();
        if(params != null){
            if(params instanceof Map){
                modelMap = (Map<String, String>)params;

                Iterator<Entry<String, String>> itr = modelMap.entrySet().iterator();
                while(itr.hasNext()){
                    Entry<String, String> entry = itr.next();
                    //过虑非String属性
                    if(!(entry.getValue() instanceof String)){
                        itr.remove();
                    }
                }
            }
            else{
                try{
                    BeanInfo beanInfo = Introspector.getBeanInfo(params.getClass());
                    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                    for(PropertyDescriptor property : propertyDescriptors){
                        String key = property.getName();

                        //过滤class属性
                        if(key.equals("class")){
                            continue;
                        }

                        //得到property对应的getter方法
                        Method getter = property.getReadMethod();
                        if(getter == null){
                            continue;
                        }

                        Object value = getter.invoke(params);

                        //过虑非String属性
                        if(value instanceof String){
                            modelMap.put(key, (String)value);
                        }
                    }
                }
                catch(Exception e){
                    log.error(e.getMessage(), e);
                }
            }
        }
        view.addAllObjects(modelMap);
        return view;
    }

    /**
     * 获取RMI客户端口IP
     * @return
     */
    public static String getClientIp(){
        try{
            String clienthost = RemoteServer.getClientHost();
            InetAddress ia = java.net.InetAddress.getByName(clienthost);
            return ia.getHostAddress();
        }
        catch(Exception e){
            log.error(e.getMessage(), e);
            return "Unknow Client";
        }
    }

    /**
     * 获取http头信息(过虑http的header，只保留自定义的header)
     * @param request
     * @return
     */
    public static Map<String, String> getCustomHeaders(HttpServletRequest request){
        Map<String, String> headers = new HashMap<String, String>();
        if(request == null){
            return headers;
        }

        Enumeration<String> names = request.getHeaderNames();
        while(names != null && names.hasMoreElements()){
            String name = names.nextElement();
            if(StringUtils.isBlank(name)){
                continue;
            }

            //过虑http的header，只保留自定义的header
            if(httpHeaderNames.contains(name.toLowerCase())){
                continue;
            }

            String header = request.getHeader(name);
            headers.put(name, header);
        }

        return headers;
    }

    /**
     * 获取request参数
     * @param request
     * @return
     */
    public static Map<String, String> getRequestParams(HttpServletRequest request){
        Map<String, String> params = new HashMap<String, String>();
        if(request == null){
            return params;
        }

        Map<String, String[]> map = request.getParameterMap();
        Iterator<Entry<String, String[]>> itr = map.entrySet().iterator();
        while(itr.hasNext()){
            Entry<String, String[]> entry = itr.next();
            params.put(entry.getKey(), entry.getValue()[0]);
        }

        return params;
    }

    /**
     * Spring的rest操作模板工具.
     */
    private static RestTemplate restTemplate = null;

    /**
     * 返回spring的restTemplate.
     * @author zhouwenqing 2017-05-11
     * @return 返回spring的restTemplate
     */
    public static RestTemplate getRestTemplate() {
        if (restTemplate != null) {
            return restTemplate;
        }
        //如果有配置了restTemplate那么从容器中获取
        if (SpringUtil.containsBean("restTemplate")) {
            restTemplate = SpringUtil.getBean("restTemplate");
        } else {
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }
}