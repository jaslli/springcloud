package com.yww.springcloud.service;

import cn.hutool.core.util.IdUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     业务接口类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/14 21:34
 **/
@Service
public class PaymentService {

    /**
     * 正常访问的情况
     * @param id ID
     * @return  返回OK
     */
    public String paymentInfoOk(Integer id) {
        return "线程池" + Thread.currentThread().getName() + "访问成功, id为: " + id;
    }

    /**
     * 超时访问的情况
     * 设置2秒钟之内可以正常执行，逻辑里停了3秒，模拟降级情况
     * @param id ID
     * @return  返回Fault
     */
    @HystrixCommand(fallbackMethod = "timeoutHandler", commandProperties = {
            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",value = "5000")
    })
    public String paymentInfoTimeOut(Integer id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "线程池" + Thread.currentThread().getName() + "超时访问, id为: " + id;
    }

    /**
     * 出现请求超时时出现的方法
     * @param id    ID
     * @return      返回信息
     */
    public String timeoutHandler(Integer id) {
        return "8001系统超时请求，请稍后再试！" + id;
    }


    /**
     *  服务熔断
     *  circuitBreaker.enabled                    	是否开启断路器
     *  circuitBreaker.requestVolumeThreshold       请求的次数
     *  circuitBreaker.sleepWindowInMilliseconds    时间窗口期
     *  circuitBreaker.errorThresholdPercentage     请求失败率的峰值
     *  下面的配置组合的意义
     *  在10000ms内，不足10次，不会打开断路器
     *	在10000ms内，超过了10次，若是失败率达到了60%就会触发断路器
     */
    @HystrixCommand(fallbackMethod = "paymentCircuitBreakerFallback", commandProperties = {
            @HystrixProperty(name = "circuitBreaker.enabled", value = "true"),
            @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
            @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
            @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60")
    })
    public String paymentCircuitBreaker(@PathVariable("id") Integer id) {
        if (id < 0) {
            throw new RuntimeException();
        }
        String number = IdUtil.simpleUUID();
        return Thread.currentThread().getName() + "\t" + "调用成功， 流水号为" + number;
    }

    public String paymentCircuitBreakerFallback(@PathVariable("id") Integer id) {
        return "id 不能为负数，请稍后再试！ 当前ID为：" + id;
    }

}
