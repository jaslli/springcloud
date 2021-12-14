package com.yww.springcloud.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * <p>
 *     RestTemplate的配置类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/12 15:17
 **/
@Configuration
public class RestConfig {

    /**
     *  LoadBalanced开启负载均衡
     */
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }

}
