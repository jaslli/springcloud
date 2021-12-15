package com.yww.springcloud.service;

import org.springframework.stereotype.Component;

/**
 * <p>
 *     服务降级处理类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/15 21:18
 **/
@Component
public class FeignConsumerImpl implements FeignConsumer{
    @Override
    public String infoOk(Integer id) {
        return "系统繁忙，请稍后再试!";
    }

    @Override
    public String infoTimeOut(Integer id) {
        return "系统繁忙，请稍后再试!";
    }
}
