package cn.ikarosx.mvcframework.v4.servlet;

import cn.ikarosx.mvcframework.annotation.GPController;
import cn.ikarosx.mvcframework.annotation.GPRequestMapping;
import cn.ikarosx.mvcframework.annotation.GPRequestParam;
import cn.ikarosx.mvcframework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class GPDispatcherServlet extends HttpServlet {


    // 保存Contrller中所有Mapping的对应关系
    private List<Handler> handlerMapping = new ArrayList<Handler>();

    private GPApplicationContext context;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // 委派模式
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        Handler handler = getHandler(req);
        if (null == handler) {
            resp.getWriter().write("404 Not Found");
            return;
        }
        Method method = handler.method;
        // 请求得到的参数
        Map<String, String[]> params = req.getParameterMap();
        // 方法上需要的参数
        Class<?>[] parameterTypes = method.getParameterTypes();
        //保存赋值参数的位置
        Object[] paramValues = new Object[parameterTypes.length];
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            String value = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");
            if (!handler.paramIndexMapping.containsKey(entry.getKey())) {
                continue;
            }
            Integer index = handler.paramIndexMapping.get(entry.getKey());
            paramValues[index] = convert(parameterTypes[index], value);
        }

        if (handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }

        if (handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        Object returnValue = handler.method.invoke(handler.controller, paramValues);
        if (returnValue == null || returnValue instanceof Void) {
            return;
        }
        resp.getWriter().write(returnValue.toString());
    }

    private Object convert(Class<?> parameterType, String value) {
        if (Integer.class == parameterType) {
            return Integer.valueOf(value);
        }
        return value;
    }

    private Handler getHandler(HttpServletRequest req) {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        // 取出根目录，以及多个//换成/
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        for (Handler handler : handlerMapping) {
            if (handler.pattern.matcher(url).find()) {
                return handler;
            }
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //模板模式

        ////1、加载配置文件
        //doLoadConfig(config.getInitParameter("contextConfigLocation"));
        ////2、扫描相关的类
        //doScanner(contextConfig.getProperty("scanPackage"));
        ////3、初始化所有相关的类的实例，并且放入到IOC容器之中
        //doInstance();
        ////4、完成依赖注入
        //doAutowired();
        //5、初始化HandlerMapping
        context = new GPApplicationContext(config.getInitParameter("contextConfigLocation"));
        initHandlerMapping();
        System.out.println("GP Spring framework is init.");
    }

    private void initHandlerMapping() {
        if (this.context.getBeanDefinitionCount() == 0) {
            return;
        }
        String[] beanNames = this.context.getBeanDefinitionNames();
        for (String beanName : beanNames) {
            Object instance = this.context.getBean(beanName);
            Class<?> clazz = instance.getClass();
            if (!clazz.isAnnotationPresent(GPController.class)) {
                continue;
            }

            GPController controller = clazz.getAnnotation(GPController.class);
            String baseUrl = "";
            // 获取RequestMapping配置
            if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                    continue;
                }
                GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                String value = requestMapping.value();
                String url = ("/" + baseUrl + "/" + value).replaceAll("/+", "/");
                handlerMapping.add(new Handler(instance, method, Pattern.compile(url)));
                System.out.println("Mapped " + url + "," + method);
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public class Handler {
        // 保存方法对应的实例
        protected Object controller;
        // 保存映射的方法
        protected Method method;
        protected Pattern pattern;

        // 参数顺序
        private Map<String, Integer> paramIndexMapping;

        protected Handler(Object controller, Method method, Pattern pattern) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;
            paramIndexMapping = new HashMap<>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {
            // 提取方法中加了注解的参数
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation annotation : pa[i]) {
                        if (annotation instanceof GPRequestParam) {
                            String paramName = ((GPRequestParam) annotation).value().trim();
                            if (!"".equals(paramName)) {
                                paramIndexMapping.put(paramName, i);
                            }
                        }
                    }
                }
            }
            // 提取方法中的request和response
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> type = parameterTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramIndexMapping.put(type.getName(), i);
                }
            }

        }
    }
}
