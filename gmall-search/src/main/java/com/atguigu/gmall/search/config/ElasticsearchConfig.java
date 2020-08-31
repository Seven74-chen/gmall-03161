package com.atguigu.gmall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/28 23:06
 */

@Configuration
public class ElasticsearchConfig {
    @Bean
    public RestHighLevelClient restHighLevelClient(){
        return new RestHighLevelClient(RestClient.builder(HttpHost.create("192.168.110.166:9200")));
    }
}
