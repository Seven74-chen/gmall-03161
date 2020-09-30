package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/4 20:02
 */
@Data
public class ItemGroupVo {

    private String groupName;
    private List<AttrValueVo> attrValues;
}
