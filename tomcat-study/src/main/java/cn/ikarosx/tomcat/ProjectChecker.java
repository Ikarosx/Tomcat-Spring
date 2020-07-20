package cn.ikarosx.tomcat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 检查指定目录下是否有项目需要部署
 *
 * @author Ikarosx
 * @date 2020/7/18 15:15
 */
class ProjectChecker {
  static Set<String> check(String workSpace) throws Exception {
    Set<String> projects = new HashSet<>();
    File ws = new File(workSpace);
    // 过滤出以war结尾的包
    FilenameFilter warFilter = (dir, name) -> name.endsWith(".war");
    File[] wars = ws.listFiles(warFilter);
    if (wars != null) {
      for (File war : wars) {
        // 解压war
        unzip(war.getPath());
        System.out.println("解压" + war.getName());
      }
      System.out.println("******************解压完毕********************");
    } else {
      System.out.println("******************未发现war包********************");
    }

    // 根据文件查询出项目
    File file = new File(workSpace);
    File[] listFiles = file.listFiles();
    if (listFiles != null) {
      for (File project : listFiles) {
        if (project.isDirectory()) {
          System.out.println("发现项目：" + project.getName());
          projects.add(project.getName());
        }
      }
      System.out.println("******************项目搜寻完毕********************");
    } else {
      System.out.println("******************未发现项目********************");
    }
    return projects;
  }

  /** 解压war */
  private static void unzip(String filePath) throws Exception {
    File zipFile = new File(filePath);
    String descDir = zipFile.getParentFile().getAbsolutePath() + "\\";

    // 解决中文文件夹乱码
    ZipFile zip = new ZipFile(zipFile, Charset.forName("GBK"));
    String name =
        zip.getName()
            .substring(zip.getName().lastIndexOf('\\') + 1, zip.getName().lastIndexOf('.'));

    File pathFile = new File(descDir + name);
    if (!pathFile.exists()) {
      pathFile.mkdirs();
    }

    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
      ZipEntry entry = entries.nextElement();
      String zipEntryName = entry.getName();
      InputStream in = zip.getInputStream(entry);
      String outPath = (descDir + name + "/" + zipEntryName).replaceAll("\\*", "/");

      // 判断路径是否存在,不存在则创建文件路径
      File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
      if (!file.exists()) {
        file.mkdirs();
      }
      // 判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
      if (new File(outPath).isDirectory()) {
        continue;
      }

      FileOutputStream out = new FileOutputStream(outPath);
      byte[] buf1 = new byte[1024];
      int len;
      while ((len = in.read(buf1)) > 0) {
        out.write(buf1, 0, len);
      }
      in.close();
      out.close();
    }
  }
}
