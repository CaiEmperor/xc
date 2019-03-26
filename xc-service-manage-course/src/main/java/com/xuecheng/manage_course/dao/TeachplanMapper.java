package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import org.apache.ibatis.annotations.Mapper;

/**
 * Teachplan持久层接口
 */
@Mapper
public interface TeachplanMapper {

   /**
    * 根据课程id查询课程计划
    * @param courseId
    * @return
    */
   TeachplanNode selectList(String courseId);
}
