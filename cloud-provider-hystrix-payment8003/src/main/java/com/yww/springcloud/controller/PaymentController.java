package com.yww.springcloud.controller;

import com.yww.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
 * @date 2021/12/14 21:39
 **/
@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    @Resource
    private PaymentService service;

    @Value("${server.port}")
    private String port;


    @GetMapping("/ok/{id}")
    public String infoOk(@PathVariable("id")Integer id) {
        return service.paymentInfoOk(id);
    }

    @GetMapping("/timeout/{id}")
    public String infoTimeOut(@PathVariable("id")Integer id) {
        return service.paymentInfoTimeOut(id);
    }

    @GetMapping("/circuit/{id}")
    public String circuit(@PathVariable("id")Integer id) {
        return service.paymentCircuitBreaker(id);
    }

}
