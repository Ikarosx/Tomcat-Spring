package cn.ikarosx.tomcat;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * 类加载工具
 *
 * @author Ikarosx
 * @date 2020/7/18 15:40
 */
public class ProjectLoader {
  private String project;
  URLClassLoader loader;

  public ProjectLoader(String project) throws Exception {

    this.project = project;
    // class文件和第三方jar包的存储路径
    List<URL> urls = new ArrayList<>();
    File libs = new File(BootStraper.WORK_SPACE + "\\" + getProject() + "\\WEB-INF\\lib");
    if (libs.exists()) {
      for (String lib : libs.list()) {
        System.out.println("找到了lib");
        urls.add(
            new URL(
                "file:" + BootStraper.WORK_SPACE + "\\" + getProject() + "\\WEB-INF\\lib\\" + lib));
      }
    }
    urls.add(
        new URL("file:" + BootStraper.WORK_SPACE + "\\" + getProject() + "\\WEB-INF\\classes\\"));
    // 每个项目一个类加载器
    this.loader = new URLClassLoader(urls.toArray(new URL[] {}));
  }

  public ProjectLoader load() throws Exception {
    // 设置后续类加载所有用的类加载器为刚刚创建的那个，后面初始化servlet需要用到的
    Thread.currentThread().setContextClassLoader(this.loader);
    // 获取项目的配置
    ProjectConfigBean projectConfigBean = BootStraper.PROJECT_CONFIG_BEAN.get(project);
    // 遍历该项目的servlets
    for (Map.Entry<String, Object> entry : projectConfigBean.servlets.entrySet()) {

      String servletName = entry.getKey();
      String servletClass = entry.getValue().toString();
      // 加载
      Class<?> aClass = loader.loadClass(servletClass);
      // 实例化
      Servlet servlet = (Servlet) aClass.newInstance();
      // 初始化，spring要读取
      servlet.init(
          new ServletConfig() {
            @Override
            public String getServletName() {
              return null;
            }

            @Override
            public ServletContext getServletContext() {
              return new ServletContext() {
                @Override
                public String getContextPath() {
                  return null;
                }

                @Override
                public ServletContext getContext(String uripath) {
                  return null;
                }

                @Override
                public int getMajorVersion() {
                  return 0;
                }

                @Override
                public int getMinorVersion() {
                  return 0;
                }

                @Override
                public int getEffectiveMajorVersion() {
                  return 0;
                }

                @Override
                public int getEffectiveMinorVersion() {
                  return 0;
                }

                @Override
                public String getMimeType(String file) {
                  return null;
                }

                @Override
                public Set<String> getResourcePaths(String path) {
                  return null;
                }

                @Override
                public URL getResource(String path) throws MalformedURLException {
                  return null;
                }

                @Override
                public InputStream getResourceAsStream(String path) {
                  return null;
                }

                @Override
                public RequestDispatcher getRequestDispatcher(String path) {
                  return null;
                }

                @Override
                public RequestDispatcher getNamedDispatcher(String name) {
                  return null;
                }

                @Override
                public Servlet getServlet(String name) throws ServletException {
                  return null;
                }

                @Override
                public Enumeration<Servlet> getServlets() {
                  return null;
                }

                @Override
                public Enumeration<String> getServletNames() {
                  return null;
                }

                @Override
                public void log(String msg) {}

                @Override
                public void log(Exception exception, String msg) {}

                @Override
                public void log(String message, Throwable throwable) {}

                @Override
                public String getRealPath(String path) {
                  return null;
                }

                @Override
                public String getServerInfo() {
                  return null;
                }

                @Override
                public String getInitParameter(String name) {
                  return projectConfigBean.servletParam.get(servletName).get(name);
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                  return new Vector<>(projectConfigBean.servletParam.get(servletName).keySet())
                      .elements();
                }

                @Override
                public boolean setInitParameter(String name, String value) {
                  return false;
                }

                @Override
                public Object getAttribute(String name) {
                  return null;
                }

                @Override
                public Enumeration<String> getAttributeNames() {
                  return new Vector<String>().elements();
                }

                @Override
                public void setAttribute(String name, Object object) {}

                @Override
                public void removeAttribute(String name) {}

                @Override
                public String getServletContextName() {
                  return null;
                }

                @Override
                public ServletRegistration.Dynamic addServlet(
                    String servletName, String className) {
                  return null;
                }

                @Override
                public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
                  return null;
                }

                @Override
                public ServletRegistration.Dynamic addServlet(
                    String servletName, Class<? extends Servlet> servletClass) {
                  return null;
                }

                @Override
                public <T extends Servlet> T createServlet(Class<T> clazz) {
                  return null;
                }

                @Override
                public ServletRegistration getServletRegistration(String servletName) {
                  return null;
                }

                @Override
                public Map<String, ? extends ServletRegistration> getServletRegistrations() {
                  return null;
                }

                @Override
                public FilterRegistration.Dynamic addFilter(String filterName, String className) {
                  return null;
                }

                @Override
                public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
                  return null;
                }

                @Override
                public FilterRegistration.Dynamic addFilter(
                    String filterName, Class<? extends Filter> filterClass) {
                  return null;
                }

                @Override
                public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
                  return null;
                }

                @Override
                public FilterRegistration getFilterRegistration(String filterName) {
                  return null;
                }

                @Override
                public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
                  return null;
                }

                @Override
                public SessionCookieConfig getSessionCookieConfig() {
                  return null;
                }

                @Override
                public void setSessionTrackingModes(
                    Set<SessionTrackingMode> sessionTrackingModes) {}

                @Override
                public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
                  return null;
                }

                @Override
                public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
                  return null;
                }

                @Override
                public void addListener(String className) {}

                @Override
                public <T extends EventListener> void addListener(T t) {}

                @Override
                public void addListener(Class<? extends EventListener> listenerClass) {}

                @Override
                public <T extends EventListener> T createListener(Class<T> clazz)
                    throws ServletException {
                  return null;
                }

                @Override
                public JspConfigDescriptor getJspConfigDescriptor() {
                  return null;
                }

                @Override
                public ClassLoader getClassLoader() {
                  return null;
                }

                @Override
                public void declareRoles(String... roleNames) {}

                @Override
                public String getVirtualServerName() {
                  return null;
                }
              };
            }

            @Override
            public String getInitParameter(String name) {
              return projectConfigBean.servletParam.get(servletName).get(name);
            }

            @Override
            public Enumeration<String> getInitParameterNames() {
              return new Vector<>(projectConfigBean.servletParam.get(servletName).keySet())
                  .elements();
            }
          });
      // 3.保存起来
      projectConfigBean.servletInstances.putIfAbsent(servletName, servlet);
    }
    return this;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }
}
