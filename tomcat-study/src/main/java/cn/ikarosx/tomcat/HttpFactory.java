package cn.ikarosx.tomcat;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.*;

public class HttpFactory {
  /**
   * 创建一个request
   *
   * @return
   */
  public static HttpServletRequest createRequest(byte[] requestBody) {
    // 实际就是根据http协议对请求的报文进行解析，取出来 请求路径，编码等等信息
    String requestString = new String(requestBody);
    return new HttpServletRequest() {
      @Override
      public String getMethod() {
        // 简单： 文本解析
        String method = requestString.split("\r\n")[0].split(" ")[0];
        return method;
      }

      @Override
      public String getContextPath() {
        // 根据路径来分析，第一个斜杠处就是对应我们的项目
        try {
          URI uri = new URI(requestString.split("\r\n")[0].split(" ")[1]);
          String project = uri.getPath().split("/")[1];
          return "/" + project;
        } catch (URISyntaxException e) {
          e.printStackTrace();
        }
        return null;
      }

      /** 获取请求路径 */
      @Override
      public String getRequestURI() {
        // 简单点就是文本解析，每一行都是一部分数据
        // 请求路径和请求方法method就是在第一行，所以我们做个简单的处理就能拿到路径
        try {
          URI uri = new URI(requestString.split("\r\n")[0].split(" ")[1]);
          return uri.getPath();
        } catch (URISyntaxException e) {
          e.printStackTrace();
        }
        return null;
      }

      @Override
      public String getServletPath() {
        try {
          // 简单点就是文本解析，每一行都是一部分数据
          // http请求路径和请求方法method就是在第一行，所以我们做个简单的处理就能拿到路径

          URI uri = new URI(requestString.split("\r\n")[0].split(" ")[1]);

          String project = uri.getPath().split("/")[1];
          String servletPath = uri.getPath().replace("/" + project, "");
          return servletPath;
        } catch (URISyntaxException e) {
          e.printStackTrace();
        }
        return null;
      }

      @Override
      public String[] getParameterValues(String name) {
        return getParameterMap().get(name);
      }

      @Override
      public Enumeration<String> getParameterNames() {
        return new Vector<>(getParameterMap().keySet()).elements();
      }

      @Override
      public Map<String, String[]> getParameterMap() {
        try {
          URI uri = new URI(requestString.split("\r\n")[0].split(" ")[1]);
          Map<String, String[]> map = new HashMap<String, String[]>();
          try {
            String[] keyValues = uri.getQuery().split("&");
            for (int i = 0; i < keyValues.length; i++) {
              String key = keyValues[i].substring(0, keyValues[i].indexOf("="));
              String value = keyValues[i].substring(keyValues[i].indexOf("=") + 1);
              map.put(key, value.split(","));
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          return map;
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }

      @Override
      public String getParameter(String name) {
        return getParameterMap().get(name)[0];
      }

      @Override
      public String getAuthType() {
        return null;
      }

      @Override
      public Cookie[] getCookies() {
        return new Cookie[0];
      }

      @Override
      public long getDateHeader(String name) {
        return 0;
      }

      @Override
      public String getHeader(String name) {
        return null;
      }

      @Override
      public Enumeration<String> getHeaders(String name) {
        return new Vector<String>().elements();
      }

      @Override
      public Enumeration<String> getHeaderNames() {
        return null;
      }

      @Override
      public int getIntHeader(String name) {
        return 0;
      }

      @Override
      public String getPathInfo() {
        return null;
      }

      @Override
      public String getPathTranslated() {
        return null;
      }

      @Override
      public String getQueryString() {
        return null;
      }

      @Override
      public String getRemoteUser() {
        return null;
      }

      @Override
      public boolean isUserInRole(String role) {
        return false;
      }

      @Override
      public Principal getUserPrincipal() {
        return null;
      }

      @Override
      public String getRequestedSessionId() {
        return null;
      }

      @Override
      public StringBuffer getRequestURL() {
        return null;
      }

      @Override
      public HttpSession getSession(boolean create) {
        return null;
      }

      @Override
      public HttpSession getSession() {
        return null;
      }

      @Override
      public String changeSessionId() {
        return null;
      }

      @Override
      public boolean isRequestedSessionIdValid() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromCookie() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromURL() {
        return false;
      }

      @Override
      public boolean isRequestedSessionIdFromUrl() {
        return false;
      }

      @Override
      public boolean authenticate(HttpServletResponse response)
          throws IOException, ServletException {
        return false;
      }

      @Override
      public void login(String username, String password) throws ServletException {}

      @Override
      public void logout() throws ServletException {}

      @Override
      public Collection<Part> getParts() throws IOException, ServletException {
        return null;
      }

      @Override
      public Part getPart(String name) throws IOException, ServletException {
        return null;
      }

      @Override
      public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass)
          throws IOException, ServletException {
        return null;
      }

      @Override
      public Object getAttribute(String name) {
        return null;
      }

      @Override
      public Enumeration<String> getAttributeNames() {
        return null;
      }

      @Override
      public String getCharacterEncoding() {
        return null;
      }

      @Override
      public void setCharacterEncoding(String env) throws UnsupportedEncodingException {}

      @Override
      public int getContentLength() {
        return 0;
      }

      @Override
      public long getContentLengthLong() {
        return 0;
      }

      @Override
      public String getContentType() {
        return null;
      }

      @Override
      public ServletInputStream getInputStream() throws IOException {
        return null;
      }

      @Override
      public String getProtocol() {
        return requestString.split("\r\n")[0].split(" ")[2];
      }

      @Override
      public String getScheme() {
        return null;
      }

      @Override
      public String getServerName() {
        return null;
      }

      @Override
      public int getServerPort() {
        return 0;
      }

      @Override
      public BufferedReader getReader() throws IOException {
        return null;
      }

      @Override
      public String getRemoteAddr() {
        return null;
      }

      @Override
      public String getRemoteHost() {
        return null;
      }

      @Override
      public void setAttribute(String name, Object o) {}

      @Override
      public void removeAttribute(String name) {}

      @Override
      public Locale getLocale() {
        return null;
      }

      @Override
      public Enumeration<Locale> getLocales() {
        return null;
      }

      @Override
      public boolean isSecure() {
        return false;
      }

      @Override
      public RequestDispatcher getRequestDispatcher(String path) {
        return null;
      }

      @Override
      public String getRealPath(String path) {
        return null;
      }

      @Override
      public int getRemotePort() {
        return 0;
      }

      @Override
      public String getLocalName() {
        return null;
      }

      @Override
      public String getLocalAddr() {
        return null;
      }

      @Override
      public int getLocalPort() {
        return 0;
      }

      @Override
      public ServletContext getServletContext() {
        return null;
      }

      @Override
      public AsyncContext startAsync() throws IllegalStateException {
        return null;
      }

      @Override
      public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse)
          throws IllegalStateException {
        return null;
      }

      @Override
      public boolean isAsyncStarted() {
        return false;
      }

      @Override
      public boolean isAsyncSupported() {
        return false;
      }

      @Override
      public AsyncContext getAsyncContext() {
        return null;
      }

      @Override
      public DispatcherType getDispatcherType() {
        return null;
      }
    };
  }

  public static HttpServletResponse createResponse(Socket connection) {
    return new HttpServletResponse() {
      private PrintWriter writer;

      @Override
      public ServletOutputStream getOutputStream() throws IOException {
        // 能够返回内容？ 里边就用 socket对象
        return new ServletOutputStream() {
          @Override
          public boolean isReady() {
            return true;
          }

          @Override
          public void setWriteListener(WriteListener writeListener) {}

          @Override
          public void write(int b) throws IOException {
            connection.getOutputStream().write(b);
          }

          @Override
          public void write(byte[] b) throws IOException {
            // 参数 返回的内容 -- HTTP响应报文
            connection.getOutputStream().write("HTTP/1.1 200 OK\r\n".getBytes());
            connection
                .getOutputStream()
                .write(("Content-Length: " + b.length + "\r\n\r\n").getBytes());
            connection.getOutputStream().write(b);
          }

          @Override
          public void write(byte[] b, int off, int len) throws IOException {
            connection.getOutputStream().write("HTTP/1.1 200 OK\r\n".getBytes());
            connection.getOutputStream().write(("Content-Length: " + len + "\r\n\r\n").getBytes());
            connection.getOutputStream().write(b, off, len);
          }

          @Override
          public void flush() throws IOException {
            connection.getOutputStream().flush();
          }
        };
      }

      @Override
      public void addCookie(Cookie cookie) {}

      @Override
      public boolean containsHeader(String name) {
        return false;
      }

      @Override
      public String encodeURL(String url) {
        return null;
      }

      @Override
      public String encodeRedirectURL(String url) {
        return null;
      }

      @Override
      public String encodeUrl(String url) {
        return null;
      }

      @Override
      public String encodeRedirectUrl(String url) {
        return null;
      }

      @Override
      public void sendError(int sc, String msg) throws IOException {}

      @Override
      public void sendError(int sc) throws IOException {}

      @Override
      public void sendRedirect(String location) throws IOException {}

      @Override
      public void setDateHeader(String name, long date) {}

      @Override
      public void addDateHeader(String name, long date) {}

      @Override
      public void setHeader(String name, String value) {}

      @Override
      public void addHeader(String name, String value) {}

      @Override
      public void setIntHeader(String name, int value) {}

      @Override
      public void addIntHeader(String name, int value) {}

      @Override
      public void setStatus(int sc) {}

      @Override
      public void setStatus(int sc, String sm) {}

      @Override
      public int getStatus() {
        return 0;
      }

      @Override
      public String getHeader(String name) {
        return null;
      }

      @Override
      public Collection<String> getHeaders(String name) {
        return null;
      }

      @Override
      public Collection<String> getHeaderNames() {
        return null;
      }

      @Override
      public String getCharacterEncoding() {
        return null;
      }

      @Override
      public String getContentType() {
        return null;
      }

      @Override
      public PrintWriter getWriter() throws IOException {
        if (writer == null) {
          OutputStreamWriter w = new OutputStreamWriter(connection.getOutputStream());
          writer = new PrintWriter(w);
        }
        return writer;
      }

      @Override
      public void setCharacterEncoding(String charset) {}

      @Override
      public void setContentLength(int len) {}

      @Override
      public void setContentLengthLong(long len) {}

      @Override
      public void setContentType(String type) {}

      @Override
      public void setBufferSize(int size) {}

      @Override
      public int getBufferSize() {
        return 0;
      }

      @Override
      public void flushBuffer() throws IOException {}

      @Override
      public void resetBuffer() {}

      @Override
      public boolean isCommitted() {
        return false;
      }

      @Override
      public void reset() {}

      @Override
      public void setLocale(Locale loc) {}

      @Override
      public Locale getLocale() {
        return null;
      }
    };
  }
}
