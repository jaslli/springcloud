package com.yww.springcloud.controller;

import com.yww.springcloud.entity.Payment;
import com.yww.springcloud.entity.Result;
import com.yww.springcloud.service.PaymentFeignService;
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
 * @date 2021/12/14 19:02
 **/
@RestController
@Slf4j
@RequestMapping("/consumer")
public class OrderController {

    @Resource
    private PaymentFeignService service;
    @GetMapping("getById/{id}")
    public Result<Payment> getById(@PathVariable("id")Long id) {
        return service.getById(id);
    }

}
