package cn.ikarosx.mvcframework.beans;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
public class GPBeanWrapper {
    private Object wrapperInstance;
    private Class<?> wrapperClass;

    public GPBeanWrapper(Object wrapperInstance) {
        this.wrapperClass = wrapperInstance.getClass();
        this.wrapperInstance = wrapperInstance;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Class<?> getWrapperClass() {
        return wrapperClass;
    }
}
