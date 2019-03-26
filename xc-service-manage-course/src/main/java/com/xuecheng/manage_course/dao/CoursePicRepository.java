package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CoursePic;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 课程与课程图片持久持久层接口
 */
public interface CoursePicRepository extends JpaRepository<CoursePic, String> {
    //CoursePicRepository父类提供的delete方法没有返回值，无法知道是否删除成功，这里我们在
    //CoursePicRepository下边自定义方法
    long deleteByCourseid(String courseId);
}
