package com.atguigu.gmall.oms.entity;

import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.sms.vo.ItemSaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/10 21:19
 */

@Data
public class OrderItemVo {

    private Long skuId;
    private String title;
    private String defaultImage;
    private BigDecimal price;
    private Integer count;
    private BigDecimal weight;
    private List<SkuAttrValueEntity> saleAttrs; // 销售属性
    private List<ItemSaleVo> sales; // 营销信息
    private Boolean store = false; // 库存信息

}
