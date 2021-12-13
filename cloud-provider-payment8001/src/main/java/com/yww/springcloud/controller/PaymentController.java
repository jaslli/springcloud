package com.yww.springcloud.controller;

import com.yww.springcloud.entity.Payment;
import com.yww.springcloud.entity.Result;
import com.yww.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    /**
     * 引入服务发现
     */
    @Resource
    private DiscoveryClient discoveryClient;

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

    @GetMapping("/discovery")
    public Result<DiscoveryClient> discovery() {
        // 获取注册的微服务列表信息
        List<String> services = discoveryClient.getServices();
        for (String service : services) {
            log.info("服务------" + service);
        }
        // 根据具体的微服务ID获取其中所有的实例
        List<ServiceInstance> instances = discoveryClient.getInstances("CLOUD-PAYMENT-PROVIDER");
        for (ServiceInstance instance : instances) {
            log.info("服务ID-----" + instance.getServiceId());
            log.info("主机名称-----" + instance.getHost());
            log.info("实例端口号-----" + instance.getPort());
            log.info("实例地址" + instance.getUri());
        }
        return new Result<>(200,"获取信息成功",this.discoveryClient);
    }

}
