package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.CmsPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * CmsPageControllerApi实现类
 */
@RestController
@RequestMapping("/cms/page")
public class CmsPageController implements CmsPageControllerApi {

    @Autowired
    private CmsPageService cmsPageService;

    /**
     * 条件分页查询
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    @Override
    //get请求的注解
    @GetMapping("/list/{page}/{size}")
    //@@PathVariable 统一指定参数名称，如：@PathVariable("id")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {

       /* 静态资源测试
        QueryResult<CmsPage> queryResult = new QueryResult<>();
        ArrayList<CmsPage> list = new ArrayList<>();
        CmsPage cmsPage = new CmsPage();
        cmsPage.setPageName("斗帝-萧炎");
        list.add(cmsPage);
        queryResult.setList(list);
        QueryResponseResult qrResult = new QueryResponseResult(CommonCode.SUCCESS, queryResult);*/
       //调用CmsPageService的findList方法
        QueryResponseResult queryResponseResult = cmsPageService.findList(page, size, queryPageRequest);
        return queryResponseResult;
    }

    /**
     * 新增页面
     * @param cmsPage
     * @return
     */
    @Override
    //post请求注解
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage) {

        //调用CmsPageService的add方法
        CmsPageResult cmsPageResult = cmsPageService.add(cmsPage);
        return cmsPageResult;
    }

    /**
     * 根据id查询页面
     * @param id
     * @return
     */
    @Override
    @GetMapping("/get/{id}")
    public CmsPage findById(@PathVariable("id") String id) {
        CmsPage cmsPage = cmsPageService.findById(id);
        return cmsPage;
    }

    /**
     * 更新页面(先查询后修改)
     * @param id
     * @param cmsPage
     * @return
     */
    @Override
    //提交数据使用post、put都可以，只是根据http方法的规范，
    // put方法是对服务器指定资源进行修改，所以这里使用put方法对页面修改进行修改。
    @PutMapping("edit/{id}")
    public CmsPageResult update(@PathVariable("id") String id, @RequestBody CmsPage cmsPage) {
        return cmsPageService.update(id, cmsPage);
    }

    /**
     * 删除页面
     * @param id
     * @return
     */
    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult delete(@PathVariable("id") String id) {
        return cmsPageService.delete(id);
    }

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult postPage(@PathVariable("pageId") String pageId) {
        return cmsPageService.postPage(pageId);
    }

    /**
     * 保存页面
     * @param cmsPage 没有添加,有就修改
     * @return
     */
    @Override
    @PostMapping("/save")
    public CmsPageResult save(@RequestBody CmsPage cmsPage) {
        return cmsPageService.save(cmsPage);
    }

    /**
     * 课程详情页面一键发布
     * @return
     */
    @Override
    @PostMapping("/postPageQuick")
    public CmsPostPageResult postPageQuick(@RequestBody CmsPage cmsPage) {
        return cmsPageService.postPageQuick(cmsPage);
    }
}
