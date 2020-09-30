package com.atguigu.gmall.scheduled.jobHandler;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import org.springframework.stereotype.Component;

/**
 * @author Baozhong Chen
 * @version 1.0
 * @date 2020/9/10 0:15
 */
@Component
public class MyJobHandler {

    @XxlJob("myJobHandler")
    public ReturnT<String> execute(String param){
        XxlJobLogger.log("使用XxlJobLogger打印执行日志，");
        System.out.println("我的任务执行了：" + param+",线程：" + Thread.currentThread().getName());
        return ReturnT.SUCCESS;
    }

}
