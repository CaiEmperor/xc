package com.xuecheng.framework.domain.cms.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程详情页面一键发布响应模型类
 * 响应:页面发布的url
 */
@Data
@NoArgsConstructor//无参构造器注解
public class CmsPostPageResult extends ResponseResult{
    String pageUrl;//页面发布的url
    public CmsPostPageResult(ResultCode resultCode, String pageUrl){
        super(resultCode);
        this.pageUrl = pageUrl;
    }
}
