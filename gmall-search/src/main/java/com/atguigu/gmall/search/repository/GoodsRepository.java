package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/28 18:10
 */
public interface GoodsRepository extends ElasticsearchRepository<Goods,Long> {
}
