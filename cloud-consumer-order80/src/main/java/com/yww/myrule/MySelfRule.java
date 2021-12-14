package com.yww.myrule;

import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.RandomRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <p>
 *     自定义负载均衡规则类
 * </p>
 *
 * @author yww
 * @version 1.0
 * @date 2021/12/14 18:05
 **/
@Configuration
public class MySelfRule {
    @Bean
    public IRule myRule() {
        return new RandomRule();
    }
}
