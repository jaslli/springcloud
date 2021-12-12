package com.yww.springcloud.controller;

import com.yww.springcloud.entity.Payment;
import com.yww.springcloud.entity.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * <p>
 *     前端控制器
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/12 15:14
 **/
@RestController
@Slf4j
@RequestMapping("/consumer")
public class OrderController {

    @Resource
    private RestTemplate restTemplate;

    private static final String URL = "http://CLOUD-PAYMENT-PROVIDER";

    @GetMapping("/save")
    public Result save(@RequestBody Payment payment) {
        return restTemplate.postForObject(URL + "/payment/save",payment,Result.class);
    }

    @GetMapping("/getById/{id}")
    public Result getById(@PathVariable("id") long id) {
        return restTemplate.getForObject(URL+"/payment/getById/" + id,Result.class);
    }

}
