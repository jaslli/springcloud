package com.yww.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;

/**
 * <p>
 *     主启动类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/16 17:10
 **/
@SpringBootApplication
@EnableHystrixDashboard
public class HystrixDashBoard {
    public static void main(String[] args) {
        SpringApplication.run(HystrixDashBoard.class, args);
    }
}
