package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/28 17:38
 */
public interface GmallPmsApi {

    /**
     * 分页获取SpuEntity信息
     * @param pageParamVo
     * @return
     */
    @PostMapping("pms/spu/page")
    public ResponseVo<List<SpuEntity>> querySpusByPage(@RequestBody PageParamVo pageParamVo);

    /**
     * 根据spuId获取sku信息
     * @param spuId
     * @return
     */
    @ApiOperation("根据spuId查询spu下的sku")
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkuBySpuIdList(@PathVariable("spuId") Long spuId);

    /**
     * 根据Id获取分类信息
     * @param id
     * @return
     */
    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);


    /**
     * 根据id获取品牌信息
     * @param id
     * @return
     */
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    /**
     * 根据spuId获取通用规范参数
     * @param spuId
     * @return
     */
    @GetMapping("pms/spuattrvalue/spu/{spuId}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValueBySpuId(@PathVariable("spuId")Long spuId);

    /**
     *根据skuId获取销售规范参数
     * @param skuId
     * @return
     */
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValueBySkuId(@PathVariable("skuId")Long skuId);

}
