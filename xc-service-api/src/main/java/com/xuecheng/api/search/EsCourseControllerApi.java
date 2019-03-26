package com.xuecheng.api.search;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;

/**
 * 搜索课程接口
 */
@Api(value="课程搜索管理接口",description = "课程搜索管理接口，提供文件的查")
public interface EsCourseControllerApi {

    @ApiOperation("搜索课程")
    QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam);

    @ApiOperation("根据课程id查询课程信息")
    Map<String, CoursePub> getall(String courseId);

    @ApiOperation("根据课程计划id查询媒资信息")
    TeachplanMediaPub getmedia(String teachplanId);
}
