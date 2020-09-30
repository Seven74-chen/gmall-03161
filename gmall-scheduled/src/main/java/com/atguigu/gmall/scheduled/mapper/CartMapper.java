package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.cart.bean.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/8 12:42
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart> {
}
