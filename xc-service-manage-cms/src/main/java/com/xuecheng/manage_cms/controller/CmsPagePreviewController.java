package com.xuecheng.manage_cms.controller;

import com.xuecheng.framework.model.response.Response;
import com.xuecheng.framework.web.BaseController;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 * 页面预览
 * 请求页面id，查询得到页面的模板信息、数据模型url，根据模板和数据生成静态化内容，并输出到浏览器
 */

@Controller
@RequestMapping("/cms/preview")
public class CmsPagePreviewController extends BaseController{

    @Autowired
    private CmsPageService cmsPageService;

    /**
     * 页面预览
     * @param pageId
     * @throws IOException
     */
    @RequestMapping(value = "/{pageId}", method = RequestMethod.GET)
    public void preview(@PathVariable("pageId") String pageId) throws IOException {
        //页面静态化
        String pageHtml = cmsPageService.getPageHtml(pageId);
        if (StringUtils.isNotEmpty(pageHtml)){
            //将静态化内容通过response输出到浏览器显示
            ServletOutputStream outputStream = response.getOutputStream();
            //由于Nginx先请求cms的课程预览功能得到html页面，再解析页面中的ssi标签，这里必须保证cms页面预览返回的
            //页面的Content-Type为text/html;charset=utf-8
            response.setHeader("Content-type","text/html;charset=utf-8");
            outputStream.write(pageHtml.getBytes("utf-8"));
        }
    }
}
