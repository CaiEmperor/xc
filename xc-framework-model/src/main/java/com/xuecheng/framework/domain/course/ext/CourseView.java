package com.xuecheng.framework.domain.course.ext;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.CourseMarket;
import com.xuecheng.framework.domain.course.CoursePic;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * 课程数据
 */
@Data//生成set,get方法
@NoArgsConstructor//生成无参构造
@ToString
public class CourseView implements Serializable {
    private CourseBase courseBase;//课程基本信息
    private CourseMarket courseMarket;//课程营销
    private CoursePic coursePic;//课程图片
    private TeachplanNode teachplanNode;//课程计划
}
