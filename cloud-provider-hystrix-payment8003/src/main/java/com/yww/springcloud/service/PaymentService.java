package com.yww.springcloud.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.stereotype.Service;

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

}
