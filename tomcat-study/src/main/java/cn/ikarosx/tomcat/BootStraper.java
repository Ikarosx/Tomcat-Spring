package cn.ikarosx.tomcat;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 启动引导类
 *
 * @author Ikarosx
 * @date 2020/7/18 14:23
 */
public class BootStraper {

  /** 工作空间 - 也就是war包的发布目录 */
  static final String WORK_SPACE = "D:\\ikarosx-tomcat\\webapps";
  /** 不同项目对应的配置信息 */
  static final Map<String, ProjectConfigBean> PROJECT_CONFIG_BEAN = new HashMap<>();

  public static void main(String[] args) throws Exception {
    // 部署项目
    // 1. 检查目录下是否有项目，文件夹/war包：文件操作
    Set<String> projects = ProjectChecker.check(WORK_SPACE);
    // 2. 找到servlet：web.xml/注解
    for (String project : projects) {
      ProjectConfigBean projectConfigBean = new ProjectConfigBean(project).loadXml();
      PROJECT_CONFIG_BEAN.put(project, projectConfigBean);
    }
    // 3. 加载和初始化servlet
    for (String project : projects) {
      new ProjectLoader(project).load();
    }
    // 启动Web服务
    WebServerStarter.start();
  }
}
