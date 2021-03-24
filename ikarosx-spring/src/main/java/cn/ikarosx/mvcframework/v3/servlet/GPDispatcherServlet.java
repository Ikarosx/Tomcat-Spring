package cn.ikarosx.mvcframework.v3.servlet;

import cn.ikarosx.mvcframework.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class GPDispatcherServlet extends HttpServlet {

    // 存储aplication.properties的配置内容
    private Properties contextConfig = new Properties();

    // 存储所有扫描到的类
    private List<String> classNames = new ArrayList<>();

    // IOC容器，保存所有实例化对象
    // 注册式单例模式
    private Map<String, Object> ioc = new HashMap<>();

    // 保存Contrller中所有Mapping的对应关系
    private List<Handler> handlerMapping = new ArrayList<Handler>();

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

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        //3、初始化所有相关的类的实例，并且放入到IOC容器之中
        doInstance();
        //4、完成依赖注入
        doAutowired();
        //5、初始化HandlerMapping
        initHandlerMapping();

        System.out.println("GP Spring framework is init.");
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
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
                handlerMapping.add(new Handler(entry.getValue(), method, Pattern.compile(url)));
                System.out.println("Mapped " + url + "," + method);
            }
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }
        try {
            for (Map.Entry<String, Object> entry : ioc.entrySet()) {
                Field[] fields = entry.getValue().getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (!field.isAnnotationPresent(GPAutowired.class)) {
                        continue;
                    }
                    GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                    String beanName = autowired.value();
                    if ("".equals(beanName)) {
                        beanName = toLowerFirstCase(field.getType().getSimpleName());
                    }
                    field.setAccessible(true);
                    field.set(entry.getValue(), ioc.get(beanName));
                }

            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 实例化
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        // 遍历时修改会报ConcurrentModificationException
        // 主要是在遍历Controller上的RequestMapping方法时会报错
        // 因为这些方法在扫描时并没有被扫描进mapping里，导致了put时会导致modcount++
        try {
            for (String className : classNames) {
                if (!className.contains(".")) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(GPController.class)) {
                    // 如果类有注解GPController，将其放入mapping中
                    Object instance = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(GPService.class)) {
                    // 默认名字
                    GPService gpService = clazz.getAnnotation(GPService.class);
                    String beanName = gpService.value();
                    if ("".equals(beanName)) {
                        // 指定名字
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    // 放入mapping
                    ioc.put(beanName, instance);
                    // 根据类型注入实现类，投机取巧的方式
                    for (Class<?> i : clazz.getInterfaces()) {
                        String ibeanName = toLowerFirstCase(i.getSimpleName());
                        if (ioc.containsKey(ibeanName)) {
                            throw new Exception("The beanName is exists!!");
                        }
                        // 遍历接口
                        // 接口注入也是同一个对象
                        ioc.put(ibeanName, instance);
                    }
                } else {
                    // 不是controller也不是service
                    continue;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream is = null;
        try {
            is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            contextConfig.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 扫描出所有的.class文件，并保存全限定类名到mapping中
     *
     * @param scanPackage 要扫描的根目录，会递归扫描
     */
    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classDir = new File(url.getFile());
        for (File file : classDir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String clazzName = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(clazzName);
            }
        }
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
