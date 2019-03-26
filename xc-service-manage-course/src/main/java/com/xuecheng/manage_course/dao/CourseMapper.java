package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import org.apache.ibatis.annotations.Mapper;
import com.github.pagehelper.Page;


/**
 * Created by Administrator.
 */
@Mapper
public interface CourseMapper {

   CourseBase findCourseBaseById(String id);

   /**
    * 根据条件查询我的课程信息
    * @param courseListRequest
    * @return
    */
   Page<CourseInfo> findCourseListPage(CourseListRequest courseListRequest);
}
