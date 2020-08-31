package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValueVo;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Test
    void importData() {

        this.restTemplate.createIndex(Goods.class);
        this.restTemplate.getMapping(Goods.class);

        Integer pageNum = 1;
        Integer pageSize = 100;
        //批量导入
        do {
            //分批查询spu
            PageParamVo pageParamVo = new PageParamVo(pageNum, pageSize, null);
            ResponseVo<List<SpuEntity>> responseVo = this.pmsClient.querySpusByPage(pageParamVo);
            List<SpuEntity> spuEntities = responseVo.getData();
            if (CollectionUtils.isEmpty(spuEntities)){
                return;
            }
            //遍历spu，查询spu下的sku信息
            spuEntities.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = this.pmsClient.querySkuBySpuIdList(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if (!CollectionUtils.isEmpty(skuEntities)){
                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        //sku相关信息
                        goods.setSkuId(skuEntity.getId());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        //spu创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());

                        //在sku中获取brandId，获取brand的信息
                        ResponseVo<BrandEntity> brandEntityResponseVo = this.pmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity!=null){
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }
                        //在sku中获取category的id，通过id获取category的信息
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = this.pmsClient.queryCategoryById(skuEntity.getCatagoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity!=null){
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        //'根据skuId获取库存信息
                        ResponseVo<List<WareSkuEntity>> listResponseVo = this.wmsClient.queryWareSkuBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = listResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)){
                           /* boolean isHaveStore=false;
                            for (WareSkuEntity wareSkuEntity : wareSkuEntities) {
                                isHaveStore = wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0?true:false;
                            }*/
                           //anyMatch 返回的是个boolean值
                            boolean isHaveStore = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()-wareSkuEntity.getStockLocked()>0);
                            //库存
                            goods.setStore(isHaveStore);
                            Long aLong = wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b) -> a + b).get();
                            //营业额
                            goods.setSales(aLong);
                        }

                        //用于收集所有的搜索属性
                        List<SearchAttrValueVo> attrValueVos = new ArrayList<>();
                        //sku销售属性
                        ResponseVo<List<SkuAttrValueEntity>> skuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySkuId(skuEntity.getId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)){
                            attrValueVos.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(skuAttrValueEntity,searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList()));
                        }
                        //spu通用属性
                        ResponseVo<List<SpuAttrValueEntity>> spuAttrValueResponseVo = this.pmsClient.querySearchAttrValueBySpuId(spuEntity.getId());
                        List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)){
                            attrValueVos.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValueVo searchAttrValueVo = new SearchAttrValueVo();
                                BeanUtils.copyProperties(spuAttrValueEntity,searchAttrValueVo);
                                return searchAttrValueVo;
                            }).collect(Collectors.toList()));
                        }
                        goods.setSearchAttrs(attrValueVos);
                        return goods;
                    }).collect(Collectors.toList());
                    this.goodsRepository.saveAll(goodsList);
                }
            });
            pageSize = spuEntities.size();
            pageNum++;
        }while (pageSize==100);
    }
}
