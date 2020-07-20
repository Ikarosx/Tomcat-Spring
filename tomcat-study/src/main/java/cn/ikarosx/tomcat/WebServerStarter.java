package cn.ikarosx.tomcat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Ikarosx
 * @date 2020/7/18 14:23
 */
public class WebServerStarter {
  // 处理请求的线程池
  public static final ThreadPoolExecutor THREAD_POOL_EXECUTOR =
      new ThreadPoolExecutor(25, 50, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

  public static void start() {
    ServerSocket serverSocket;
    try {
      // 监听端口
      System.out.println("main 启动 8888");
      serverSocket = new ServerSocket(8888);
      // 循环获取
      // BIO写法
      while (true) {
        // 不同操作系统有不同的实现
        // 获取连接，此处阻塞，没有新的连接就会一直停在这里
        Socket connection = serverSocket.accept();
        // 支撑多个请求  同时处理
        THREAD_POOL_EXECUTOR.execute(new SocketProcessor(connection));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
