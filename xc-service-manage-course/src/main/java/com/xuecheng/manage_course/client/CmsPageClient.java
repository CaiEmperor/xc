package com.xuecheng.manage_course.client;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * FeignClient接口
 * 注意接口的Url、请求参数类型、返回值类型与Swagger接口一致
 * SpringCloud对Feign进行了增强兼容了SpringMVC的注解 ，我们在使用SpringMVC的注解时需要注意：
     1、feignClient接口 有参数在参数必须加@PathVariable("XXX")和@RequestParam("XXX")
     2、feignClient返回值为复杂对象时其类型必须有无参构造函数。
 */
@FeignClient("XC-SERVICE-MANAGE-CMS")//指定远程调用的服务名,Feign会从注册中心获取cms服务列表，并通过负载均衡算法进行服务调用
public interface CmsPageClient {

    //根据页面id查询页面信息，远程调用cms请求数据
    @GetMapping("/cms/page/get/{id}")//用GetMapping标识远程调用的http的方法类型
    CmsPage findCmsPageById(@PathVariable("id") String id);

    /**
     * 添加页面,用于课程详情页面预览
     * @param cmsPage
     * @return
     */
    @PostMapping("/cms/page/save")
    CmsPageResult saveCmsPage(@RequestBody CmsPage cmsPage);

    /**
     * 课程详情页面发布
     * @param cmsPage
     * @return
     */
    @PostMapping("/cms/page/postPageQuick")
    CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage);

}
