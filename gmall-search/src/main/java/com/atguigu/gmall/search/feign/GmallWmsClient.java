package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/8/28 18:05
 */
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {

}
