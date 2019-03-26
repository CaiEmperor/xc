package com.xuecheng.manage_course.feign;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_course.client.CmsPageClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

/**
 *Ribbon负载均衡测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class FeignTest {

    @Autowired
    ////接口代理对象，由Feign生成代理对象
    private CmsPageClient cmsPageClient;

    @Test
    public void feignTest(){
            //通过服务id远程调用cms的查询页面接口
            CmsPage cmsPage = cmsPageClient.findCmsPageById("5a754adf6abb500ad05688d9");
            System.out.println(cmsPage);
    }
}
