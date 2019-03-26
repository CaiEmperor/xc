package com.xuecheng.manage_cms;

import com.xuecheng.manage_cms.service.CmsPageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * CmsPageService测试
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class CmsPageServiceTest {

    @Autowired
    private CmsPageService cmsPageService;

    /**
     * 页面静态化测试
     */
    @Test
    public void getPageHtmlTest(){
        String pageHtml = cmsPageService.getPageHtml("5c20bba2b7534f1d887bacfc");
        System.out.println(pageHtml);
    }

}
