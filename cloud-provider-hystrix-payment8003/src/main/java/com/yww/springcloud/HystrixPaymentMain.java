package com.yww.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * <p>
 *     主启动类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/14 21:33
 **/
@SpringBootApplication
@EnableEurekaClient
@EnableCircuitBreaker
public class HystrixPaymentMain {
    public static void main(String[] args) {
        SpringApplication.run(HystrixPaymentMain.class, args);
    }
}
