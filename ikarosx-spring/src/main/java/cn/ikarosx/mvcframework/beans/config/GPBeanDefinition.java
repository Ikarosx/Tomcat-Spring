package cn.ikarosx.mvcframework.beans.config;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class GPBeanDefinition {
    private String factoryBeanName;
    private String beanClassName;

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }
}
