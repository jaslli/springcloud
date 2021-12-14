package com.yww.springcloud.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * <p>
 *     前端控制器
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/14 17:10
 **/
@RestController
@Slf4j
@RequestMapping("/consumer")
public class OrderController {

    @Resource
    private RestTemplate restTemplate;

    private static final String URL = "http://consul-provider-payment";

    @GetMapping("paymentInfo")
    public String paymentInfo() {
        return restTemplate.getForObject(URL+"/payment/consul", String.class);
    }


}
