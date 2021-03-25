package cn.ikarosx.mvcframework.beans.support;

import cn.ikarosx.mvcframework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class GPBeanDefinitionReader {
    // 存储aplication.properties的配置内容
    private Properties contextConfig = new Properties();
    // 存储所有扫描到的类
    private List<String> registryBeanClasses = new ArrayList<>();

    public GPBeanDefinitionReader(String... contextConfigLocations) {
        // 1. 读取配置文件
        doLoadConfig(contextConfigLocations[0]);
        // 2. 扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
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
                registryBeanClasses.add(clazzName);
            }
        }
    }


    public List<GPBeanDefinition> doLoadBeanDefinitions() {
        List<GPBeanDefinition> result = new ArrayList<>();
        try {
            for (String registryBeanClass : registryBeanClasses) {
                Class<?> beanClass = Class.forName(registryBeanClass);
                if (beanClass.isInterface()) {
                    continue;
                }
                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));
                for (Class<?> i : beanClass.getInterfaces()) {
                    result.add(doCreateBeanDefinition(toLowerFirstCase(i.getSimpleName()), beanClass.getName()));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private GPBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;
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

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
