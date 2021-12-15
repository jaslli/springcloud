package com.yww.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <p>
 *     主启动类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/15 20:16
 **/
@SpringBootApplication
@EnableFeignClients
@EnableHystrix
public class FeignHystrixMain {
    public static void main(String[] args) {
        SpringApplication.run(FeignHystrixMain.class, args);
    }
}
