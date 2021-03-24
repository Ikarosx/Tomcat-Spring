package cn.ikarosx.demo.controller;

import cn.ikarosx.demo.service.IDemoService;
import cn.ikarosx.mvcframework.annotation.GPAutowired;
import cn.ikarosx.mvcframework.annotation.GPController;
import cn.ikarosx.mvcframework.annotation.GPRequestMapping;
import cn.ikarosx.mvcframework.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Ikarosx
 * @date 2021/03/24
 */
@GPController
@GPRequestMapping("/demo")
public class DemoController {
    @GPAutowired
    private IDemoService demoService;

    @GPRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response, @RequestParam("name") String name) {
        String s = demoService.get(name);
        try {
            response.getWriter().write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GPRequestMapping("/add")
    public void add(HttpServletRequest request, HttpServletResponse response, @RequestParam("a") Integer a, @RequestParam("b") Integer b) {
        try {
            response.getWriter().write(String.format("%d + %d = %d", a, b, a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
