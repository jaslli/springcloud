package com.yww.springcloud.controller;

import com.yww.springcloud.entity.Payment;
import com.yww.springcloud.entity.Result;
import com.yww.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *     前端控制器
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/11 19:49
 **/
@RestController
@Slf4j
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Value("${server.port}")
    private String serverPort;

    @PostMapping("/save")
    public Result<Long> save(@RequestBody Payment payment) {
        long result = paymentService.save(payment);
        log.info("插入结果------" + result);
        if (result > 0) {
            return new Result<>(200,"插入数据成功---" + serverPort,result);
        } else {
            return new Result<>(500,"插入数据失败---" + serverPort);
        }
    }

    @GetMapping("/getById/{id}")
    public Result<Payment> getById(@PathVariable("id") Long id){
        Payment payment = paymentService.getById(id);
        if (payment != null) {
            return new Result<>(200,"查询成功---" + serverPort,payment);
        } else {
            return new Result<>(500,"查询失败---" + serverPort);
        }
    }

}
