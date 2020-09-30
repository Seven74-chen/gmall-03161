package com.atguigu.gmall.oms.entity;

import com.atguigu.gmall.ums.entity.UserAddressEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/11 16:50
 */

@Data
public class OrderSubmitVo {

    private String orderToken; // 防重
    private BigDecimal totalPrice; // 总价，校验价格变化
    private UserAddressEntity address; // 收货人信息
    private Integer payType; // 支付方式
    private String deliveryCompany; // 配送方式
    private List<OrderItemVo> items; // 订单详情信息
    private Integer bounds; // 积分信息
    private BigDecimal postFee;

    // 发票信息TODO

    // 营销信息TODO
}
