package com.atguigu.gmall.index.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/4 13:50
 */

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        //可以用rediss：//来启用SSl连接
        config.useSingleServer().setAddress("redis://192.168.110.166:6379");
        return Redisson.create(config);
    }

}
