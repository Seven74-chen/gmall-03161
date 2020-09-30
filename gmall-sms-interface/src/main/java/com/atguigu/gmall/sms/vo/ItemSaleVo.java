package com.atguigu.gmall.item.vo;

import lombok.Data;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/4 20:01
 */

@Data
public class ItemSaleVo {

    private String type; // 积分 满减 打折
    private String desc; // 描述信息
}
