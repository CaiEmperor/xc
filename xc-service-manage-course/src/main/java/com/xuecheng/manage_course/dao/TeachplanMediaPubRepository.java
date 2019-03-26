package com.xuecheng.manage_course.dao;

import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by Administrator.
 */
public interface TeachplanMediaPubRepository extends JpaRepository<TeachplanMediaPub,String> {

    /**
     * 根据课程id删除TeachplanMediaPub中课程媒资关联的信息
     * @param courseId
     * @return
     */
    long deleteByCourseId(String courseId);
}
