package com.atguigu.gmall.search.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/28 22:56
 */
@Data
public class SearchParamVo {
    private String keyword; // 检索条件

    private List<Long> brandId; // 品牌过滤

    private List<Long> categoryId; // 分类过滤

    // props=5:高通-麒麟,6:骁龙865-硅谷1000
    private List<String> props; // 过滤的检索参数

    private Integer sort = 0;// 排序字段：0-默认，得分降序；1-按价格升序；2-按价格降序；3-按创建时间降序；4-按销量降序

    // 价格区间
    private Double priceFrom;
    private Double priceTo;

    private Integer pageNum = 1; // 页码
    private final Integer pageSize = 20; // 每页记录数

    private Boolean store; // 是否有货

}
