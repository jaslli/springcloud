package com.yww.springcloud.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 *     业务接口类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/15 20:17
 **/
@FeignClient(value = "HYSTRIX-PROVIDER-PAYMENT",fallback = FeignConsumerImpl.class)
public interface FeignConsumer {

    /**
     * 正常访问
     * @param id    ID
     * @return      响应
     */
    @GetMapping("/payment/ok/{id}")
    String infoOk(@PathVariable("id")Integer id);

    /**
     * 超时访问
     * @param id    ID
     * @return      响应
     */
    @GetMapping("/payment/timeout/{id}")
    String infoTimeOut(@PathVariable("id")Integer id);

}
