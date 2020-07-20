package cn.ikarosx.tomcat;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 处理网络请求
 *
 * @author Ikarosx
 * @date 2020/7/18 14:29
 */
public class SocketProcessor implements Runnable {
  private Socket socket;

  public SocketProcessor(Socket socket) {
    this.socket = socket;
  }

  @Override
  public void run() {

    // Tomcat没有业务逻辑
    // 网络客户端 ——> 操作系统 ——> jvm/jdk ——>socket ——> tomcat ——>Servlet(request, response)
    // 封装的本质是为了降低开发难度
    try {
      byte[] requestBody = new byte[1024];
      socket.getInputStream().read(requestBody);
      String requestString = new String(requestBody, StandardCharsets.UTF_8);
      System.out.println("收到Http请求，内容如下：");
      System.out.println(requestString);
      // request response 对象 转化 -- 网络数据 转  java 对象
      HttpServletRequest request = HttpFactory.createRequest(requestBody);
      HttpServletResponse response = HttpFactory.createResponse(socket);
      // 项目 -- 基于 servlet-- servlet 从哪里来？ ---
      // 拿到项目名称
      String project = request.getContextPath().split("/")[1];
      // 根据请求的路径，匹配servlet
      // 此处仅仅做了简单的url匹配
      // 获取路径相匹配的servlet名称
      // 1. 判断顶级的路径有没有被配置映射，斜杠最大
      ProjectConfigBean projectConfigBean = BootStraper.PROJECT_CONFIG_BEAN.get(project);
      String servletName = projectConfigBean.servletMapping.get("/");
      if (servletName == null) {
        // 2. 如果没有配置"/",则根据请求路径去百分百匹配
        servletName =
            BootStraper.PROJECT_CONFIG_BEAN
                .get(project)
                .servletMapping
                .get(request.getServletPath());
      }
      // 3. 如果还没找到，404
      if (servletName == null) {
        response.getOutputStream().write("404".getBytes());
        return;
      }
      // 根据servlet名称，获取对应的servlet实例
      Servlet servlet =
          (Servlet)
              BootStraper.PROJECT_CONFIG_BEAN.get(project).getServletInstances().get(servletName);

      // 调用servlet实例的service方法继续处理这个请求
      // 这里往后就是业务系统的处理范畴了！！
      servlet.service(request, response);
    } catch (IOException | ServletException e) {
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
