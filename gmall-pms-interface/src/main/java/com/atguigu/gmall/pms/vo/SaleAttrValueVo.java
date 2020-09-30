package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.Set;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/4 20:01
 */
@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;
    private Set<String> attrValues;
}
