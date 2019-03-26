package com.xuecheng.manage_course.service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.domain.course.*;
import com.xuecheng.framework.domain.course.ext.CourseInfo;
import com.xuecheng.framework.domain.course.ext.CourseView;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.domain.course.request.CourseListRequest;
import com.xuecheng.framework.domain.course.response.CourseCode;
import com.xuecheng.framework.domain.course.response.CoursePublishResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.client.CmsPageClient;
import com.xuecheng.manage_course.dao.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * 课程业务层接口
 */
@Service
public class CourseService {

    @Autowired
    private TeachplanMapper teachplanMapper;
    @Autowired
    private CmsPageClient cmsPageClient;
    @Autowired
    private TeachplanRepository teachplanRepository;
    @Autowired
    private CourseBaseRepository courseBaseRepository;
    @Autowired
    private CoursePicRepository coursePicRepository;
    @Autowired
    private CourseMarketRepository courseMarketRepository;
    @Autowired
    private CoursePubRepository coursePubRepository;
    @Autowired
    private TeachplanMediaRepository teachplanMediaRepository;
    @Autowired
    private TeachplanMediaPubRepository teachplanMediaPubRepository;
    @Autowired
    private CourseMapper courseMapper;


    //从配置文件中获取数据
    @Value("${course-publish.dataUrlPre}")
    private String publish_dataUrlPre;
    @Value("${course-publish.pagePhysicalPath}")
    private String publish_page_physicalpath;
    @Value("${course-publish.pageWebPath}")
    private String publish_page_webpath;
    @Value("${course-publish.siteId}")
    private String publish_siteId;
    @Value("${course-publish.templateId}")
    private String publish_templateId;
    @Value("${course-publish.previewUrl}")
    private String previewUrl;

    /**
     * 根据课程查询课程计划
     * @param courseId
     * @return
     */
    public TeachplanNode findTeachplanList(String courseId){
        return teachplanMapper.selectList(courseId);
    }

    /**
     * 添加课程计划
     * @param teachplan
     * @return
     */
    @Transactional
    public ResponseResult addTeachplan(Teachplan teachplan) {

        if(teachplan == null || StringUtils.isEmpty(teachplan.getPname()) || StringUtils.isEmpty(teachplan.getCourseid())){
            //没有指定必备参数
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //获取课程id
        String courseId = teachplan.getCourseid();
        //获取该课程父结点的id
        String parentId = teachplan.getParentid();
        //如果该课程父结点的id为空
        if (StringUtils.isEmpty(parentId)){
            //获取该课程父结点的id
            parentId = this.getTeachplanRoot(courseId);
        }
        //查询父结点的信息
        Optional<Teachplan> optional = teachplanRepository.findById(parentId);
        String parent_grade = null;
        if (optional.isPresent()){
            Teachplan teachplan1 = optional.get();
            //获取父结点的级别
            parent_grade = teachplan1.getGrade();
        }
        //创建一个新结点准备添加
        Teachplan new_teachplan = new Teachplan();
        //将teachplan的属性拷贝到teachplanNew中
        BeanUtils.copyProperties(teachplan, new_teachplan);
        //设置必要的属性
        new_teachplan.setParentid(parentId);
        //判断父结点级别
        if (parent_grade.equals("1")){
            //设置添加课程的结点级别
            new_teachplan.setGrade("2");
        }else {
            new_teachplan.setGrade("3");
        }
        //结点状态
        new_teachplan.setStatus("0");//未发布
        //保存添加的结点
        teachplanRepository.save(new_teachplan);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取课程的父结点
     * @param courseId
     * @return
     */
    public String getTeachplanRoot(String courseId){

        ////调用dao查询teachplan表得到该课程的根结点（一级结点）
        List<Teachplan> teachplanList = teachplanRepository.findByParentidAndCourseid("0", courseId);
        //获取课程的父结点
        String id = teachplanList.get(0).getId();
        return id;
    }

    /**
     * 保存课程与课程图片的对应关系
     * @param courseId
     * @param pic
     * @return
     */
    @Transactional
    public ResponseResult addCoursePic(String courseId, String pic) {
        //查询课程图片,有就获取,没有新建一个
        CoursePic coursePic = null;
        Optional<CoursePic> optionalPic = coursePicRepository.findById(courseId);
        if (optionalPic.isPresent()){
            coursePic = optionalPic.get();
        }
        if (coursePic == null){
            coursePic = new CoursePic();
        }
        coursePic.setCourseid(courseId);
        coursePic.setPic(pic);
        coursePicRepository.save(coursePic);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 查询课程图片
     * @param courseId
     * @return
     */
    public CoursePic findCoursePic(String courseId) {
        Optional<CoursePic> optionalPic = coursePicRepository.findById(courseId);
        if (optionalPic.isPresent()){
            CoursePic coursePic = optionalPic.get();
            return coursePic;
        }
        return null;
    }

    /**
     * 删除课程图片
     * @param courseId
     * @return
     */
    @Transactional
    public ResponseResult deleteCoursePic(String courseId) {
        long result = coursePicRepository.deleteByCourseid(courseId);
        if (result > 0){
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 课程视图查询
     * @param id
     * @return
     */
    public CourseView findCourseView(String id) {
        CourseView courseView = new CourseView();
        //查询课程信息
        Optional<CourseBase> courseBaseOptional = courseBaseRepository.findById(id);
        if (courseBaseOptional.isPresent()){
            CourseBase courseBase = courseBaseOptional.get();
            courseView.setCourseBase(courseBase);
        }
        //查询课程营销信息
        Optional<CourseMarket> courseMarketOptional = courseMarketRepository.findById(id);
        if (courseMarketOptional.isPresent()){
            CourseMarket courseMarket = courseMarketOptional.get();
            courseView.setCourseMarket(courseMarket);
        }
        //查询课程图片信息
        Optional<CoursePic> coursePicOptional = coursePicRepository.findById(id);
        if (coursePicOptional.isPresent()){
            CoursePic coursePic = coursePicOptional.get();
            courseView.setCoursePic(coursePic);
        }
        //查询课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(id);
        courseView.setTeachplanNode(teachplanNode);

        return courseView;
    }

    /**
     * 课程详情页面预览
     * @param courseId
     * @return
     */
    public CoursePublishResult preview(String courseId) {
        //根据课程id查询课程的基本信息
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //准备添加页面的信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);//数据模型url
        cmsPage.setPageName(courseId+".html");//页面名称
        cmsPage.setPageAliase(courseBase.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        //从注册中心远程请求cms,添加页面
        CmsPageResult cmsPageResult = cmsPageClient.saveCmsPage(cmsPage);
        if (!cmsPageResult.isSuccess()){
            //返回失败
            return new CoursePublishResult(CommonCode.FAIL, null);
        }
        CmsPage cmsPage1 = cmsPageResult.getCmsPage();
        String pageId = cmsPage1.getPageId();
        //拼接课程详情页面预览的url
        String url = previewUrl + pageId;
        //返回CoursePublishResult对象（当中包含了页面预览的url）
        return new CoursePublishResult(CommonCode.SUCCESS, url);
    }

    /**
     * 根据课程id查询课程基本信息
     * @param courseId
     * @return
     */
    public CourseBase findCourseBaseById(String courseId){
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if(baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            return courseBase;
        }
        return null;
    }

    /**
     * 课程详情页面发布
     *      1.准备页面信息
     *      2.调用cms一键发布接口将课程详情页面发布到服务器
     *      3.更新课程的发布为"已发布"
     *      4.获取页面url,返回
     * @param courseId
     * @return
     */
    @Transactional
    public CoursePublishResult publish(String courseId) {
        //1.准备页面信息
        CmsPage cmsPage = cmsPage_information(courseId);
        //2.调用cms一键发布接口将课程详情页面发布到服务器
        CmsPostPageResult cmsPostPageResult = cmsPageClient.postPageQuick(cmsPage);
        if (!cmsPostPageResult.isSuccess()){
            //发布失败
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //3.更新课程的发布为"已发布"
        CourseBase courseBase = saveCoursePubState(courseId);
        if (courseBase == null){
            //更新失败
            return new CoursePublishResult(CommonCode.FAIL,null);
        }
        //保存课程索引信息
        //先添加一个coursePub信息
        CoursePub coursePub = addCoursePub(courseId);
        //在保存到数据库
        saveCoursePub(courseId, coursePub);
        //4.获取页面url
        String pageUrl = cmsPostPageResult.getPageUrl();
        //5.保存课程与媒资关联信息
        saveTeachplanMediaPub(courseId);
        return new CoursePublishResult(CommonCode.SUCCESS, pageUrl);
    }

    /**
     * 保存课程与媒资关联信息
     *      1.根据课程id删除TeachplanMediaPub中的信息
     *      2.根据课程id查询TeachplanMedia表中的信息
     *      3.将TeachplanMedia表中的信息添加到TeachplanMediaPub中
     *      4.保存到数据库
     * @param courseId
     */
    public void saveTeachplanMediaPub(String courseId){
        //1.根据课程id删除TeachplanMediaPub中的信息
        teachplanMediaPubRepository.deleteByCourseId(courseId);
        //2.根据课程id查询TeachplanMedia表中的信息
        List<TeachplanMedia> teachplanMediaList = teachplanMediaRepository.findByCourseId(courseId);
        ArrayList<TeachplanMediaPub> teachplanMediaPubs = new ArrayList<>();
        for (TeachplanMedia teachplanMedia : teachplanMediaList) {
            TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
            //3.将TeachplanMedia表中的信息添加到TeachplanMediaPub中
            BeanUtils.copyProperties(teachplanMedia, teachplanMediaPub);
            //设置时间戳
            teachplanMediaPub.setTimestamp(new Date());
            //将teachplanMediaPub保存到teachplanMediaPubs集合中
            teachplanMediaPubs.add(teachplanMediaPub);
        }
        //4.保存到数据库
        teachplanMediaPubRepository.saveAll(teachplanMediaPubs);
    }
    /**
     * 准备页面信息
     * @param courseId
     * @return
     */
    private CmsPage cmsPage_information(String courseId){
        //查询课程基本信息
        CourseBase courseBase = this.findCourseBaseById(courseId);
        //准备页面信息
        CmsPage cmsPage = new CmsPage();
        cmsPage.setSiteId(publish_siteId);//站点id
        cmsPage.setDataUrl(publish_dataUrlPre+courseId);//数据模型url
        cmsPage.setPageName(courseId+".html");//页面名称
        cmsPage.setPageAliase(courseBase.getName());//页面别名，就是课程名称
        cmsPage.setPagePhysicalPath(publish_page_physicalpath);//页面物理路径
        cmsPage.setPageWebPath(publish_page_webpath);//页面webpath
        cmsPage.setTemplateId(publish_templateId);//页面模板id
        return cmsPage;
    }

    /**
     * 更新课程的发布为"已发布"=202002
     * @param courseId
     * @return
     */
    private CourseBase saveCoursePubState(String courseId){
        CourseBase courseBase = this.findCourseBaseById(courseId);
        courseBase.setStatus("202002");
        courseBaseRepository.save(courseBase);
        return courseBase;
    }

    /**
     * 添加一个coursePub信息(base,pic,market,teachplanNode)
     * @return
     */
    private CoursePub addCoursePub(String courseId){
        CoursePub coursePub = new CoursePub();
        //课程基本信息
        Optional<CourseBase> baseOptional = courseBaseRepository.findById(courseId);
        if (baseOptional.isPresent()){
            CourseBase courseBase = baseOptional.get();
            //用工具类,将courseBase添加到CoursePub
            BeanUtils.copyProperties(courseBase, coursePub);
        }
        //查询课程图片
        Optional<CoursePic> picOptional = coursePicRepository.findById(courseId);
        if(picOptional.isPresent()){
            CoursePic coursePic = picOptional.get();
            BeanUtils.copyProperties(coursePic, coursePub);
        }
        //课程营销信息
        Optional<CourseMarket> marketOptional = courseMarketRepository.findById(courseId);
        if(marketOptional.isPresent()){
            CourseMarket courseMarket = marketOptional.get();
            BeanUtils.copyProperties(courseMarket, coursePub);
        }
        //课程计划信息
        TeachplanNode teachplanNode = teachplanMapper.selectList(courseId);
        //将课程计划信息转为json格式
        String jsonString = JSON.toJSONString(teachplanNode);
        //将课程计划信息json串保存到 course_pub中
        coursePub.setTeachplan(jsonString);
        return coursePub;
    }

    /**
     * 将CoursePub信息保存到数据库(有就更新,没有添加)
     * @return
     */
    private CoursePub saveCoursePub(String courseId, CoursePub coursePub){
        CoursePub coursePubNew = null;
        //根据课程id查询coursePub
        Optional<CoursePub> coursePubOptional = coursePubRepository.findById(courseId);
        if(coursePubOptional.isPresent()){
            //获取并更新
            coursePubNew = coursePubOptional.get();
            //设置id
            coursePubNew.setId(courseId);
            //设置时间戳,给logstach使用
            coursePubNew.setTimestamp(new Date());
            //设置发布时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
            //将日期格式转为字符串
            String stringDate = simpleDateFormat.format(new Date());
            coursePubNew.setPubTime(stringDate);
        }else{
            //添加
            coursePubNew = new CoursePub();
        //将coursePub对象中的信息保存到coursePubNew中
        BeanUtils.copyProperties(coursePub, coursePubNew);
        }
        //保存coursePubNew信息
        coursePubRepository.save(coursePubNew);
        return coursePubNew;
    }

    /**
     * 课程计划和媒资文件关联关系
     *      1.校验课程计划是否是3级
     *      2.根据课程计划id查询TeachplanMedia信息
     *      3.给teachplanMedia1设置值,并将其保存到数据库中
     * @param teachplanMedia
     * @return
     */
    public ResponseResult savemedia(TeachplanMedia teachplanMedia) {
        if(teachplanMedia == null){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        //1.校验课程计划是否是3级
        //根据获取课程计划id
        String teachplanId = teachplanMedia.getTeachplanId();
        //根据id查询课程计划信息
        Optional<Teachplan> teachplanOptional = teachplanRepository.findById(teachplanId);
        if (!teachplanOptional.isPresent()){
            ExceptionCast.cast(CommonCode.INVALID_PARAM);
        }
        Teachplan teachplan = teachplanOptional.get();
        //获取课程等级
        String grade = teachplan.getGrade();
        if (StringUtils.isEmpty(grade) || !grade.equals("3")){
            //只允许选择第三级的课程计划关联视频
            ExceptionCast.cast(CourseCode.COURSE_MEDIA_TEACHPLAN_GRADEERROR);
        }
        //2.根据课程计划id查询TeachplanMedia信息
        Optional<TeachplanMedia> teachplanMediaOptional = teachplanMediaRepository.findById(teachplanId);
        TeachplanMedia teachplanMedia1 = null;
        if (teachplanMediaOptional.isPresent()){
            teachplanMedia1 = teachplanMediaOptional.get();
        }else {
            teachplanMedia1 = new TeachplanMedia();
        }
        //3.给teachplanMedia1设置值,并将其保存到数据库中
        teachplanMedia1.setCourseId(teachplan.getCourseid());//课程id
        teachplanMedia1.setTeachplanId(teachplanId);//课程计划id
        teachplanMedia1.setMediaId(teachplanMedia.getMediaId());//媒资文件id
        teachplanMedia1.setMediaFileOriginalName(teachplanMedia.getMediaFileOriginalName());//媒资文件原始名
        teachplanMedia1.setMediaUrl(teachplanMedia.getMediaUrl());//媒资文件的url
        teachplanMediaRepository.save(teachplanMedia1);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     *
     * @param companyId 公司id
     * @param page
     * @param size
     * @param courseListRequest 查询条件
     * @return
     */
    public QueryResponseResult<CourseInfo> findCourseList(String companyId, int page, int size, CourseListRequest courseListRequest) {
        if(courseListRequest == null){
            courseListRequest = new CourseListRequest();
        }
        //将公司id参数传入dao
        courseListRequest.setCompanyId(companyId);
        if(page<=0){
            page = 1;
        }
        if(size<=0){
            size = 10;
        }
        //分页
        PageHelper.startPage(page, size);
        //调用dao
        Page<CourseInfo> courseListPage = courseMapper.findCourseListPage(courseListRequest);
        List<CourseInfo> list = courseListPage.getResult();
        long total = courseListPage.getTotal();
        QueryResult<CourseInfo> courseIncfoQueryResult = new QueryResult<>();
        courseIncfoQueryResult.setList(list);
        courseIncfoQueryResult.setTotal(total);
        return new QueryResponseResult<CourseInfo>(CommonCode.SUCCESS,courseIncfoQueryResult);
    }
}
