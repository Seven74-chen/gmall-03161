package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/11 17:50
 */
@Data
public class SkuLockVo {

    private Long skuId; // 锁定的商品id
    private Integer count; // 购买的数量
    private Boolean lock; // 锁定状态
    private Long wareSkuId; // 锁定成功时，锁定的仓库id
    private String orderToken; // 方便以订单为单位缓存订单的锁定信息


}
