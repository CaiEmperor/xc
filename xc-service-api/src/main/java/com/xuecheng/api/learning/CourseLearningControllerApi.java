package com.xuecheng.api.learning;

import com.xuecheng.framework.domain.learning.response.GetMediaResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 学习服务接口
 */
@Api(value = "录播课程学习管理接口",description = "录播课程学习管理")
public interface CourseLearningControllerApi {

    @ApiOperation("根据课程计划id获取课程学习地址")
    GetMediaResult getmedia(String courseId, String teachplanId);
}
