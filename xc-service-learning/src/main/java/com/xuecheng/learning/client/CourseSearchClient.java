package com.xuecheng.learning.client;

import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 搜索服务的客户端接口
 * 在学习服务创建搜索服务的客户端接口，此接口会生成代理对象，调用搜索服务
 */
@FeignClient(XcServiceList.XC_SERVICE_SEARCH)//远程调用的服务
public interface CourseSearchClient {

    /**
     * 根据课程计划id查询媒资信息
     * @param teachplanId
     * @return
     */
    @GetMapping("/search/course/getmedia/{teachplanId}")
    TeachplanMediaPub getmedia(@PathVariable("teachplanId") String teachplanId);
}
