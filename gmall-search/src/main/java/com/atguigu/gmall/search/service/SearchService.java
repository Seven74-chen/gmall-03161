package com.atguigu.gmall.search.service;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.*;
import com.baomidou.mybatisplus.extension.api.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/28 23:07
 */
@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //解析json
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public SearchResponseVo search(SearchParamVo paramVo) {
        try {
            //构建查询条件
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(paramVo));
            //执行查询结果
            SearchResponse response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析结果集
            SearchResponseVo responseVo = this.parseResult(response);
            responseVo.setPageNum(paramVo.getPageNum());
            responseVo.setPageSize(paramVo.getPageSize());

            return responseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析搜索结果集
     * @param response
     * @return
     */
    private SearchResponseVo parseResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();
        //解析hits
        SearchHits hits = response.getHits();
        //获取总纪录数
        responseVo.setTotal(hits.getTotalHits());
        //获取当前页
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hitsHits).map(hitsHit -> {
            try {
                //获取hitsHit中的_sourse
                String json = hitsHit.getSourceAsString();
                //反序列化，获取了Goods对象
                Goods goods = MAPPER.readValue(json, Goods.class);
                //获取高亮标题替换普通标题
                HighlightField highlightFieId = hitsHit.getHighlightFields().get("title");
                Text[] fragments = highlightFieId.getFragments();
                goods.setTitle(fragments[0].string());
                return goods;
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toList());
        responseVo.setGoodsList(goodsList);

        //解析搜索数据中的聚合数据
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();

        //获取品牌聚合，解析出品牌集合
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> brandBuckets = brandIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(brandBuckets)){
            List<BrandEntity> collect = brandBuckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                brandEntity.setId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //获取桶内所有的子聚合
                Map<String, Aggregation> brandAggregationnMap = ((Terms.Bucket) bucket).getAggregations().asMap();
                //获取品牌名称的子聚合
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandAggregationnMap.get("brandNameAgg");
                // 获取品牌名称子聚合中的桶
                List<? extends Terms.Bucket> nameAggBuckets = brandNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    brandEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                //获取logo的子聚合
                ParsedStringTerms logoAgg = (ParsedStringTerms) brandAggregationnMap.get("logoAgg");
                List<? extends Terms.Bucket> logoAggBuckets = logoAgg.getBuckets();
                if (!CollectionUtils.isEmpty(logoAggBuckets)) {
                    brandEntity.setLogo(logoAggBuckets.get(0).getKeyAsString());
                }
                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(collect);
        }

        //获取分类id的聚合
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(categoryBuckets)){
            List<CategoryEntity> categoryNameAgg1 = categoryBuckets.stream().map(categoryBucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(((Terms.Bucket) categoryBucket).getKeyAsNumber().longValue());

                //获取分类下的子聚合
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms) ((Terms.Bucket) categoryBucket).getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(nameAggBuckets)) {
                    categoryEntity.setName(nameAggBuckets.get(0).getKeyAsString());
                }
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(categoryNameAgg1);
        }

        //获取规格参数下的子聚合(嵌套类型)
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        //嵌套类型下的子聚合
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        List<? extends Terms.Bucket> idAggBuckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(idAggBuckets)){
            List<SearchResponseAttrVo> collect = idAggBuckets.stream().map(bucket -> {
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //设置attr参数id
                searchResponseAttrVo.setAttrId(((Terms.Bucket) bucket).getKeyAsNumber().longValue());
                //获取桶中的子聚合
                Map<String, Aggregation> attrAggregationMap = ((Terms.Bucket) bucket).getAggregations().asMap();

                //获取参数名的子聚合
                ParsedStringTerms attrNameAgg = (ParsedStringTerms) attrAggregationMap.get("attrNameAgg");
                List<? extends Terms.Bucket> attrNameAggBuckets = attrNameAgg.getBuckets();
                if (!CollectionUtils.isEmpty(attrNameAggBuckets)) {
                    searchResponseAttrVo.setAttrName(attrNameAggBuckets.get(0).getKeyAsString());
                }

                //获取参数值下的子聚合
                ParsedStringTerms attrValueAgg = (ParsedStringTerms) attrAggregationMap.get("attrValueAgg");
                List<? extends Terms.Bucket> valueAggBuckets = attrValueAgg.getBuckets();
                if (!CollectionUtils.isEmpty(valueAggBuckets)) {
                    List<String> arrayList = new ArrayList<>();
                    for (Terms.Bucket valueAggBucket : valueAggBuckets) {
                        String keyAsString = valueAggBucket.getKeyAsString();
                        arrayList.add(keyAsString);
                    }
                    //List<String> stringList = valueAggBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrVo.setAttrValues(arrayList);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(collect);
        }
        return responseVo;
    }

    /**
     * 接收前端返回的条件，查询Request
     * @param paramVo
     * @return
     */
    private SearchSourceBuilder buildDsl(SearchParamVo paramVo) {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        //获取keyword
        String keyword = paramVo.getKeyword();
        if (StringUtils.isBlank(keyword)) {
            //打广告
            return sourceBuilder;
        }
        //1构建查询条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //sourceBuilder.query(boolQueryBuilder);

        //1.1匹配查询
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //1.2品牌查询
        List<Long> brandId = paramVo.getBrandId();
        if (!CollectionUtils.isEmpty(brandId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandId));
        }
        //1.3分类过滤
        List<Long> categoryId = paramVo.getCategoryId();
        if (!CollectionUtils.isEmpty(categoryId)) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", categoryId));
        }
        //1.4规格参数过滤
        List<String> props = paramVo.getProps();
        if (!CollectionUtils.isEmpty(props)) {
            props.forEach(prop -> {
                // // &props=4:6G-8G-12G&props=5:128G
                //分割参数
                String[] split = StringUtils.split(prop, ":");
                //判断参数是否是正确的格式
                if (split != null && split.length == 2) {
                    //创建子Boolean查询
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    //查询attrId
                    boolQuery.must(QueryBuilders.termQuery("searchAttrs.attrId", split[0]));
                    //分割参数
                    String[] split1 = StringUtils.split(split[1], "-");
                    //查询attrValue
                    boolQuery.must(QueryBuilders.termsQuery("searchAttrs.attrValue", split1));
                    //总的过滤，nestedQuery防止数据的扁平化
                    boolQueryBuilder.filter(QueryBuilders.nestedQuery("searchAttrs", boolQuery, ScoreMode.None));
                }
            });
        }

        //1.5价格区间过滤
        Double priceFrom = paramVo.getPriceFrom();
        Double priceTo = paramVo.getPriceTo();
        if (priceFrom != null || priceTo != null) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("price");
            if (priceFrom != null) {
                rangeQuery.gte(priceFrom);
            }
            if (priceTo != null) {
                rangeQuery.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQuery);
        }

        //1.6是否有
        Boolean store = paramVo.getStore();
        if (store != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("store", store));
        }
        sourceBuilder.query(boolQueryBuilder);
        //2排序
        Integer sort = paramVo.getSort();
        if (sort == null) {
            sort = 0;
        }
        switch (sort) {
            case 1:
                sourceBuilder.sort("price", SortOrder.ASC);
                break;
            case 2:
                sourceBuilder.sort("price", SortOrder.DESC);
                break;
            case 3:
                sourceBuilder.sort("createTime", SortOrder.DESC);
                break;
            case 4:
                sourceBuilder.sort("sales", SortOrder.DESC);
                break;
            default:
                sourceBuilder.sort("_score", SortOrder.DESC);
                break;
        }

        //3分页
        Integer pageNum = paramVo.getPageNum();
        Integer pageSize = paramVo.getPageSize();
        sourceBuilder.from((pageNum - 1) * pageSize);
        sourceBuilder.size(pageSize);

        //4高亮
        sourceBuilder.highlighter(
                new HighlightBuilder()
                        .field("title")
                        .preTags("<font style='color:red;'>")
                        .postTags("</font>")
        );

        //5构建聚合
        //5.1品牌聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("logoAgg").field("logo"))
        );
        //5.2分类聚合
        sourceBuilder.aggregation(
                AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                        .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName"))
        );

        //5.3规格参数聚合
        sourceBuilder.aggregation(
                AggregationBuilders.nested("attrAgg", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue")))
        );

        //6结果集过滤
        sourceBuilder.fetchSource(new String[]{"skuId", "defaultImage", "price", "title", "subTitle"},null);
        System.out.println(sourceBuilder.toString());
        return sourceBuilder;
    }
}





















