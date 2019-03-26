package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.CoursePic;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.TeachplanMedia;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 课程的接口
 */
@Api(value="课程页面管理接口",description = "课程页面管理接口，提供页面的增、删、改、查")
public interface CourseControllerApi {

    /**
     * 根据id查询课程计划
     * @param courseId
     * @return
     */
    @ApiOperation("课程计划查询")
    TeachplanNode findTeachplanList(String courseId);

    /**
     * 添加课程计划
     * @param teachplan
     * @return
     */
    @ApiOperation("添加课程计划")
    ResponseResult addTeachplan(Teachplan teachplan);

    /**
     *添加课程与课程图片的对应关系
     * @param courseId
     * @param pic
     * @return
     */
    @ApiOperation("添加课程与课程图片的对应关系")
    ResponseResult addCoursePic(String courseId, String pic);

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    @ApiOperation("查询课程图片")
    CoursePic findCoursePic(String courseId);

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @ApiOperation("删除课程图片")
    ResponseResult deleteCoursePic(String courseId);

    /**
     * 课程视图查询
     * @param id 课程id
     * @return
     */
    @ApiOperation("课程视图查询")
    CourseView findCourseView(String id);

    /**
     * 课程详情页面预览
     * @param courseId
     * @return
     */
    @ApiOperation("课程详情页面预览")
    CoursePublishResult preview(String courseId);

    /**
     * 课程详情页面发布
     * @param courseId
     * @return
     */
    @ApiOperation("课程详情页面发布")
    CoursePublishResult publish(String courseId);

    /**
     * 课程计划和媒资文件关联关系
     * @param teachplanMedia
     * @return
     */
    @ApiOperation("课程计划和媒资文件关联关系")
    ResponseResult savemedia(TeachplanMedia teachplanMedia);

    @ApiOperation("课程查询")
    QueryResponseResult<CourseInfo> findCourseList(int page, int size, CourseListRequest courseListRequest);
}
