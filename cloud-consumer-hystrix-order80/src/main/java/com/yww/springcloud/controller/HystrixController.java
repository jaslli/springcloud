package com.yww.springcloud.controller;

import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import com.yww.springcloud.service.FeignConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *     前端控制器
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/15 20:20
 **/
@RestController
@Slf4j
@RequestMapping("consumer")
@DefaultProperties(defaultFallback = "globalTimeout", commandProperties = {
        @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1000")
})
public class HystrixController {

    @Resource
    private FeignConsumer server;

    @GetMapping("/ok/{id}")
    public String ok(@PathVariable("id")Integer id) {
        return server.infoOk(id);
    }

    @GetMapping("/timeout/{id}")
    @HystrixCommand(fallbackMethod = "timeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "1000")
    })
    public String timeout(@PathVariable("id")Integer id) {
        return server.infoTimeOut(id);
    }


    @GetMapping("/timeout2/{id}")
    @HystrixCommand
    public String timeout2(@PathVariable("id")Integer id) {
        return server.infoTimeOut(id);
    }

    /**
     * 特定的方法的服务降级
     */
    public String timeoutHandler(@PathVariable("id")Integer id) {
        return "消费端调用超时，请稍后再试！" + id;
    }

    /**
     * 全局的降级处理
     */
    public String globalTimeout() {
        return "全局降级！";
    }
}
