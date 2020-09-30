package com.atguigu.gmall.oms.feign;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/10 23:34
 */

@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {
}
