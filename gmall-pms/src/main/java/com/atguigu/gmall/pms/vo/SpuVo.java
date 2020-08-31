package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuEntity;
import lombok.Data;

import java.util.List;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/24 16:42
 */
@Data
public class SpuVo extends SpuEntity {
    //图片信息
    private List<String> spuImages;
    //基本属性信息
    private List<SpuAttrValueVo> baseAttrs;
    //sku信息
    private List<SkuVo> skus;
}
