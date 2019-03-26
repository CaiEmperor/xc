package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * CmsPage分页查询Controller接口
 * @Api：修饰整个类，描述Controller的作用
 * @ApiOperation：描述一个类的一个方法，或者说一个接口
 * @ApiParam：单个参数描述
 * @ApiModel：用对象来接收参数
 * @ApiModelProperty：用对象接收参数时，描述对象的一个字段
 * @ApiResponse：HTTP响应其中1个描述
 * @ApiResponses：HTTP响应整体描述
 * @ApiIgnore：使用该注解忽略这个API
 * @ApiError：发生错误返回的信息
 * @ApiImplicitParam：一个请求参数
 @ApiImplicitParams：多个请求参数
 @ApiImplicitParam属性：
 属性                 取值                  作用
 paramType                              查询参数类型
            path 以地址的形式提交数据
            query 直接跟参数完成自动映射赋值
            body 以流的形式提交 仅支持POST
            header 参数在request headers 里边提交
            form 以form表单的形式提交 仅支持POST
 dataType                               参数的数据类型 只作为标志说明，并没有实际验证
            Long
            String
 name                                   接收参数名
 value                                  接收参数的意义描述
 required                               参数是否必填
            true                        必填
            false                       非必填
 defaultValue                           默认值
 */
@Api(value="cms页面管理接口",description = "cms页面管理接口，提供页面的增、删、改、查")
public interface CmsPageControllerApi {

    /**
     * 条件分页查询
     * @param page
     * @param size
     * @param queryPageRequest
     * @return
     */
    @ApiOperation("条件分页查询页面")
    @ApiImplicitParams({
            @ApiImplicitParam(name="page",value = "页码",required=true,paramType="path",dataType="int"),
            @ApiImplicitParam(name="size",value = "每页记录数",required=true,paramType="path",dataType="int")
                    })
    QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    /**
     * 新增页面
     * @param cmsPage
     * @return
     */
    @ApiOperation("新增页面")
    CmsPageResult add(CmsPage cmsPage);

    /**
     * 通过id查询页面
     * @param id
     * @return
     */
    @ApiOperation("根据id查询页面")
    CmsPage findById(String id);

    /**
     * 修改页面(先查询在修改)
     * @param id
     * @param cmsPage
     * @return
     */
    @ApiOperation("修改页面")
    CmsPageResult update(String id, CmsPage cmsPage);

    /**
     * 删除页面
     * @param id
     * @return
     */
    @ApiOperation("删除页面")
    ResponseResult delete(String id);

    /**
     * 页面发布
     * @param pageId
     * @return
     */
    @ApiOperation("页面发布")
    ResponseResult postPage(String pageId);

    /**
     * 保存页面:没有添加,有就修改
     * @param cmsPage 没有添加,有就修改
     * @return
     */
    @ApiOperation("保存页面")
    CmsPageResult save(CmsPage cmsPage);

    /**
     * 一键发布页面
     * @return
     */
    @ApiOperation("一键发布页面")
    CmsPostPageResult postPageQuick(CmsPage cmsPage);
}
