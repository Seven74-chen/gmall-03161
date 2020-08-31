package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private SpuDescMapper spuDescMapper;
    @Autowired
    private SpuAttrValueService spuAttrValueService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuAttrValueService skuAttrValueService;
    @Autowired
    private GmallSmsClient smsClient;


    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpu(PageParamVo pageParamVo, Long cid) {
        QueryWrapper<SpuEntity> queryWrapper = new QueryWrapper<>();
        if (cid != 0) {
            queryWrapper.eq("category_id", cid);
        }
        //判断条件查询是否为空，
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.and(t -> t.like("name", key).or().like("id", key));
        }
        //创建分页查询
        IPage<SpuEntity> page = this.page(pageParamVo.getPage(), queryWrapper);
        //通过构造器将Ipage类型转为page类型，返回。
        PageResultVo pageResultVo = new PageResultVo(page);
        return pageResultVo;
    }

    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        //1、保存spu相关信息
        //1.1、spu表信息
        //默认上架
        Long spuId = saveSpu(spuVo);

        //1.2、spu详情信息
        saveSpuDesc(spuVo, spuId);

        //1.3、spu基本参数信息
        saveBaseAttr(spuVo, spuId);

        //2、保存sku相关信息
        saveSku(spuVo, spuId);
        //制造异常。
        //int i = 1/0;
    }

    private void saveSku(SpuVo spuVo, Long spuId) {
        //获取skus相关属性
        List<SkuVo> skuVos = spuVo.getSkus();
        //判断是否等于null
        if (CollectionUtils.isEmpty(skuVos)) {
            return;
        }
        //遍历skuVos集合，获取skuVo
        skuVos.forEach(skuVo -> {
            //2.1、sku基本信息
            //创建一个skuEntity类
            SkuEntity skuEntity = new SkuEntity();
            //将skuvo属性拷贝到skuEntity
            BeanUtils.copyProperties(skuVo, skuEntity);
            //添加skuEntity没有的属性
            //品牌id
            skuEntity.setBrandId(spuVo.getBrandId());
            //分类id
            skuEntity.setCatagoryId(spuVo.getCategoryId());
            //获取图片信息
            List<String> images = skuVo.getImages();
            //设置默认图片
            if (!CollectionUtils.isEmpty(images)) {
                skuEntity.setDefaultImage(skuEntity.getDefaultImage() == null ? images.get(0) : skuEntity.getDefaultImage());
            }
            skuEntity.setSpuId(spuId);
            this.skuMapper.insert(skuEntity);
            //获取skuId
            Long skuId = skuEntity.getId();

            //2.2、sku图片信息
            if (!CollectionUtils.isEmpty(images)){
                String defaultImage = images.get(0);
                List<SkuImagesEntity> collect = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(defaultImage, image) ? 1 : 0);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setSort(0);
                    skuImagesEntity.setUrl(image);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(collect);
            }
            //2.3、sku基本参数信息
            List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
            saleAttrs.forEach(saleAttr -> {
                // 设置属性名，需要根据id查询AttrEntity
                saleAttr.setSort(0);
                saleAttr.setSkuId(skuId);
            });
            this.skuAttrValueService.saveBatch(saleAttrs);
            //3、保存营销相关的信息
            SkuSaleVo skuSaleVo = new SkuSaleVo();
            BeanUtils.copyProperties(skuVo,skuSaleVo);
            skuSaleVo.setSkuId(skuId);
            this.smsClient.saveSkuSaleInfo(skuSaleVo);
        });
    }

    private void saveBaseAttr(SpuVo spuVo, Long spuId) {
        //获取基本参数的集合
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        //判断基本参数集合不为空
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            //把一个集合（baseAtters）转为另一个集合（collect）。spuAttrValueVo为baseAtters集合对象
            List<SpuAttrValueEntity> collect = baseAttrs.stream().map(spuAttrValueVo -> {
                spuAttrValueVo.setSpuId(spuId);
                spuAttrValueVo.setSort(0);
                return spuAttrValueVo;
            }).collect(Collectors.toList());
            //批量插入
            this.spuAttrValueService.saveBatch(collect);
        }
    }

    private void saveSpuDesc(SpuVo spuVo, Long spuId) {
        SpuDescEntity spuDescEntity = new SpuDescEntity();
        spuDescEntity.setSpuId(spuId);
        //将list集合转化为string类型并以 "," 分割。
        spuDescEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
        this.spuDescMapper.insert(spuDescEntity);
    }

    private Long saveSpu(SpuVo spuVo) {
//        SpuEntity spuEntity = new SpuEntity();
//        BeanUtils.copyProperties(spuVo,spuEntity);
//        spuEntity.setCreateTime(new Date());
//        spuEntity.setPublishStatus(1);
//        spuEntity.setUpdateTime(spuEntity.getCreateTime());
//        this.save(spuEntity);
//        return spuEntity.getId();

        spuVo.setPublishStatus(1);
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime());
        this.save(spuVo);
        return spuVo.getId();
    }


}