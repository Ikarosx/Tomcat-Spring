package cn.ikarosx.mvcframework.context;

import cn.ikarosx.mvcframework.annotation.GPAutowired;
import cn.ikarosx.mvcframework.annotation.GPController;
import cn.ikarosx.mvcframework.annotation.GPService;
import cn.ikarosx.mvcframework.beans.GPBeanWrapper;
import cn.ikarosx.mvcframework.beans.config.GPBeanDefinition;
import cn.ikarosx.mvcframework.beans.support.GPBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class GPApplicationContext {
    private String[] configLocations;
    private GPBeanDefinitionReader gpBeanDefinitionReader;
    private Map<String, GPBeanDefinition> beanDefinitionMap = new HashMap<>();
    private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new HashMap<>();
    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    public GPApplicationContext(String... configLocations) {
        this.configLocations = configLocations;

        // 1.读取配置文件，并且封装成BeanDefinition对象
        gpBeanDefinitionReader = new GPBeanDefinitionReader(configLocations);
        List<GPBeanDefinition> beanDefinitionList = gpBeanDefinitionReader.doLoadBeanDefinitions();
        // 2. 将BeanDefinition对象缓存到beanDefinitionMap中
        doRegisityBeanDefinition(beanDefinitionList);
        // 3. 触发对象实例化的动作,循环调用getBean
        doCreateBean();

    }

    private void doCreateBean() {
        for (Map.Entry<String, GPBeanDefinition> beanDefinitionEntry : this.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            // 1. 真正完成创建动作的方法
            // 2. 完成DI
            getBean(beanName);
        }

    }

    private void doRegisityBeanDefinition(List<GPBeanDefinition> beanDefinitionList) {
        for (GPBeanDefinition beanDefinition : beanDefinitionList) {
            if (this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new RuntimeException("The " + beanDefinition.getFactoryBeanName() + " is exists!!");
            }
            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            //this.beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }

    }

    public Object getBean(Class<?> className) {
        return getBean(className.getName());
    }

    public Object getBean(String beanName) {
        // 1. 拿到BeanName对应的配置信息BeanDefinition
        GPBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        // 2. 实例化对象
        Object instance = instantiateBean(beanName, beanDefinition);
        // 3. 将实例化对象封装成BeanWrapper
        GPBeanWrapper gpBeanWrapper = new GPBeanWrapper(instance);
        // 4. 将BeanWrapper缓存到IOC容器中
        factoryBeanInstanceCache.put(beanName, gpBeanWrapper);
        // 5. 完成DI
        populateBean(beanName, beanDefinition, gpBeanWrapper);

        return this.factoryBeanInstanceCache.get(beanName).getWrapperInstance();
    }

    private void populateBean(String beanName, GPBeanDefinition beanDefinition, GPBeanWrapper gpBeanWrapper) {
        Object instance = gpBeanWrapper.getWrapperInstance();
        Class<?> clazz = gpBeanWrapper.getWrapperClass();
        if (!clazz.isAnnotationPresent(GPController.class) && !clazz.isAnnotationPresent(GPService.class)) {
            return;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(GPAutowired.class)) {
                continue;
            }
            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            String autowiredBeanName = autowired.value();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = toLowerFirstCase(field.getType().getSimpleName());
            }
            field.setAccessible(true);
            try {
                if (this.factoryBeanInstanceCache.get(autowiredBeanName) == null) {
                    continue;
                }
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }

    private Object instantiateBean(String beanName, GPBeanDefinition beanDefinition) {
        Object instance = null;
        try {
            String className = beanDefinition.getBeanClassName();
            Class<?> clazz = Class.forName(className);
            instance = clazz.newInstance();
            // 如果对象要生成代理，此处就是预留入口
            this.factoryBeanObjectCache.put(beanName, instance);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return instance;
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[0]);
    }
}
