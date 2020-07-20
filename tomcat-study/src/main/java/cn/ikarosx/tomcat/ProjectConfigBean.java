package cn.ikarosx.tomcat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * 每个项目不同的配置，读取到这个对象里
 *
 * @author Ikarosx
 * @date 2020/7/18 15:22
 */
public class ProjectConfigBean extends DefaultHandler {
  /** Servlet 集合 */
  Map<String, Object> servlets = new HashMap<>();
  /** Servlet 实例集合 */
  Map<String, Object> servletInstances = new HashMap<>();

  /** Servlet 参数 */
  Map<String, Map<String, String>> servletParam = new HashMap<>();

  /** Servlet 映射 */
  Map<String, String> servletMapping = new HashMap<>();

  /** 项目 */
  private String project;
  /** xml文件地址 */
  private String xmlPath;

  public ProjectConfigBean(String project) {
    this.project = project;
  }

  /**
   * 读取web.xml文件中的信息， 比如(context_param,listener,filter,servlet)
   *
   * <p>这个示例中仅读取servlet信息
   */
  public ProjectConfigBean loadXml() throws Exception {
    this.xmlPath = BootStraper.WORK_SPACE + "\\" + this.project + "\\WEB-INF\\web.xml";
    // 创建一个解析XML的工厂对象
    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
    // 创建一个解析XML的对象
    SAXParser parser = parserFactory.newSAXParser();
    // 创建一个解析助手类
    parser.parse(this.xmlPath, this);

    System.out.println(
        "******************项目" + this.getProject() + "中的web.xml加载完毕********************");
    return this;
  }

  private String currentServlet = null;
  private String currentServletMapping = null;
  private String currentParam = null;
  private String qName = null;

  /** 开始解析文档，即开始解析XML根元素时调用该方法 */
  @Override
  public void startDocument() {
    System.out.println("--开始解析: " + this.xmlPath);
  }

  /** 开始解析每个元素时都会调用该方法 */
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    // 判断正在解析的元素是不是开始解析的元素
    this.qName = qName;
  }

  /** 解析到每个元素的内容时会调用此方法 */
  @Override
  public void characters(char[] ch, int start, int length) throws SAXException {
    String currentValue = new String(ch, start, length);
    // 如果内容不为空和空格，也不是换行符则将该元素名和值和存入map中
    if ("".equals(currentValue.trim()) || "\n".equals(currentValue.trim())) {
      return;
    }
    if ("servlet-name".equals(qName)) {
      currentServlet = currentValue;
      currentServletMapping = currentValue;
    } else if ("servlet-class".equals(qName)) {
      // servlet信息
      String servletClass = currentValue;
      servlets.put(currentServlet, servletClass);
    } else if ("param-name".equals(qName)) {
      currentParam = currentValue;
    } else if ("param-value".equals(qName)) {
      String paramValue = currentValue;
      // servlet param 参数
      HashMap<String, String> params = new HashMap<>();
      params.put(currentParam, paramValue);
      servletParam.put(currentServlet, params);
    } else if ("servlet-name".equals(qName)) {
      currentServletMapping = currentValue;
    } else if ("url-pattern".equals(qName)) {
      String urlPattern = currentValue;
      // url映射
      servletMapping.put(urlPattern, currentServletMapping);
    }
  }

  /** 每个元素结束的时候都会调用该方法 */
  @Override
  public void endElement(String uri, String localName, String qName) throws SAXException {}

  /** 结束解析文档，即解析根元素结束标签时调用该方法 */
  @Override
  public void endDocument() throws SAXException {
    super.endDocument();
  }

  public Map<String, Object> getServlets() {
    return servlets;
  }

  public void setServlets(Map<String, Object> servlets) {
    this.servlets = servlets;
  }

  public Map<String, Map<String, String>> getServletParam() {
    return servletParam;
  }

  public void setServletParam(Map<String, Map<String, String>> servletParam) {
    this.servletParam = servletParam;
  }

  public Map<String, String> getServletMapping() {
    return servletMapping;
  }

  public void setServletMapping(Map<String, String> servletMapping) {
    this.servletMapping = servletMapping;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public Map<String, Object> getServletInstances() {
    return servletInstances;
  }

  public void setServletInstances(Map<String, Object> servletInstances) {
    this.servletInstances = servletInstances;
  }
}
