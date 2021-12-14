package com.yww.springcloud.service;

import com.yww.springcloud.entity.Payment;
import com.yww.springcloud.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * <p>
 *     业务服务接口
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/14 18:55
 **/
@FeignClient("CLOUD-PAYMENT-PROVIDER")
public interface PaymentFeignService {

    /**
     * 通过ID获取订单
     * @param id    数据ID
     * @return      订单信息
     */
    @GetMapping("/payment/getById/{id}")
    Result<Payment> getById(@PathVariable("id") Long id);

}
