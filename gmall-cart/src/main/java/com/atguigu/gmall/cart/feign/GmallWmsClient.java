package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/4 20:11
 */

@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
