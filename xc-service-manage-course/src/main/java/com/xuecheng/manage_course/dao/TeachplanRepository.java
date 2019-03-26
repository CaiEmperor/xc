package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 课程计划持久层接口.
 */
public interface TeachplanRepository extends JpaRepository<Teachplan,String> {

    /**
     * 根据课程courseId和父结点parentId查询出结点列表，查询添加结点的根结点
     * @param parentId
     * @param courseId
     * @return
     */
   List<Teachplan> findByParentidAndCourseid(String parentId, String courseId);
}
