package com.yww.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * <p>
 *     主启动类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/14 18:53
 **/
@SpringBootApplication
@EnableFeignClients
public class FeignOrderMain {
    public static void main(String[] args) {
        SpringApplication.run(FeignOrderMain.class, args);
    }
}
