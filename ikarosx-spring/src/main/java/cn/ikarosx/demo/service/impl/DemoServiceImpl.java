package cn.ikarosx.demo.service.impl;

import cn.ikarosx.demo.service.IDemoService;
import cn.ikarosx.mvcframework.annotation.GPService;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
@GPService
public class DemoServiceImpl implements IDemoService {
    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
