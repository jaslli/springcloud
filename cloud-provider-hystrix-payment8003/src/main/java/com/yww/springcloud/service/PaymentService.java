package com.yww.springcloud.service;

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
     * @param id ID
     * @return  返回Fault
     */
    public String paymentInfoTimeOut(Integer id) {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "线程池" + Thread.currentThread().getName() + "超时访问, id为: " + id;
    }

}
