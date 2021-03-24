package cn.ikarosx.mvcframework.v1.servlet;

import cn.ikarosx.mvcframework.annotation.GPAutowired;
import cn.ikarosx.mvcframework.annotation.GPController;
import cn.ikarosx.mvcframework.annotation.GPRequestMapping;
import cn.ikarosx.mvcframework.annotation.GPService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class GPDispatcherServlet extends HttpServlet {
    private Map<String, Object> mapping = Collections.synchronizedMap(new HashMap<>());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");
        if (!this.mapping.containsKey(url)) {
            resp.getWriter().write("404 not found");
            return;
        }
        Method method = (Method) this.mapping.get(url);
        Map<String, String[]> params = req.getParameterMap();
        method.invoke(this.mapping.get(method.getDeclaringClass().getName()), req, resp, params.get("name")[0]);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try {
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);
            String scanPackage = configContext.getProperty("scanPackage");
            doScanner(scanPackage);
            // 遍历时修改会报ConcurrentModificationException
            // 主要是在遍历Controller上的RequestMapping方法时会报错
            // 因为这些方法在扫描时并没有被扫描进mapping里，导致了put时会导致modcount++
            HashMap<String, Object> temp = new HashMap<>();
            for (String className : mapping.keySet()) {
                if (!className.contains(".")) {
                    continue;
                }
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(GPController.class)) {
                    // 如果类有注解GPController，将其放入mapping中
                    mapping.put(className, clazz.newInstance());
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                        // Controller类的RequestMapping，为baseUrl
                        GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        // 遍历方法
                        if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                            continue;
                        }
                        // 方法上有RequestMapping则将其放入mapping中
                        GPRequestMapping methodRequestMapping = method.getAnnotation(GPRequestMapping.class);
                        // 多个/ 变为 1个/
                        String url = (baseUrl + "/" + methodRequestMapping.value()).replaceAll("/+", "/");
                        temp.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                } else if (clazz.isAnnotationPresent(GPService.class)) {
                    // 如果类是Service类
                    GPService gpService = clazz.getAnnotation(GPService.class);
                    String beanName = gpService.value();
                    // 如果注解没有指定名字，默认为clazz.getName()
                    if ("".equals(beanName)) {
                        beanName = clazz.getName();
                    }
                    Object instance = clazz.newInstance();
                    // 放入mapping
                    mapping.put(beanName, instance);
                    for (Class<?> i : clazz.getInterfaces()) {
                        // 遍历接口
                        // 接口注入也是同一个对象
                        mapping.put(i.getName(), instance);
                    }
                } else {
                    // 不是controller也不是service
                    continue;
                }
            }
            mapping.putAll(temp);
            for (Object object : mapping.values()) {
                // 遍历预处理映射好的对象
                if (object == null) {
                    continue;
                }
                Class<?> clazz = object.getClass();
                if (clazz.isAnnotationPresent(GPController.class)) {
                    // 如果对象为Controller
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        // 遍历字段处理Autowired
                        if (!field.isAnnotationPresent(GPAutowired.class)) {

                            continue;
                        }
                        GPAutowired gpAutowired = field.getAnnotation(GPAutowired.class);
                        // 获取beanName
                        String beanName = gpAutowired.value();
                        if ("".equals(beanName)) {
                            // 为空默认为类型的名称
                            beanName = field.getType().getName();
                        }
                        field.setAccessible(true);
                        // 直接设置字段的值为对应的实例，从mapping中获取
                        // key为哪个类的值，value为具体值
                        field.set(mapping.get(clazz.getName()), mapping.get(beanName));
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("GP MVC Framework is init");
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
                mapping.put(clazzName, null);
            }
        }
    }
}
