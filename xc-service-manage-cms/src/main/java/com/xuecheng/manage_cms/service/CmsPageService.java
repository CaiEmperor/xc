package com.xuecheng.manage_cms.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.domain.cms.response.CmsPostPageResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.CmsTemplateRepository;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


/**
 * CmsCageService实现类,此处省略service接口
 */
@Service
public class CmsPageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Autowired
    private CmsConfigRepository cmsConfigRepository;

    @Autowired
    private CmsTemplateRepository cmsTemplateRepository;

    /**
     * GridFS是MongoDB提供的用于持久化存储文件的模块，CMS使用MongoDB存储数据，使用GridFS可以快速集成
     开发。
     它的工作原理是：
     在GridFS存储文件是将文件分块存储，文件会按照256KB的大小分割成多个块进行存储，GridFS使用两个集合
     （collection）存储文件，一个集合是chunks, 用于存储文件的二进制数据；一个集合是files，用于存储文件的元数
     据信息（文件名称、块大小、上传时间等信息）。
     从GridFS中读取文件要对文件的各各块进行组装、合并。
     */
    @Autowired
    //操作GridFS的类
    private GridFsTemplate gridFsTemplate;

    @Autowired
    //用于打开下载流对象
    private GridFSBucket gridFSBucket;
    @Autowired
    //从远程访问http接口
    private RestTemplate restTemplate;
    @Autowired
    //操作rabbitMQ的类
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    /**
     * 条件分页查询
     * @param page 当前页码
     * @param size 每页的记录条数
     * @param queryPageRequest 查询条件
     * @return
     */
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest) {

        //判断查询条件是否为空
        if (queryPageRequest == null){
            queryPageRequest = new QueryPageRequest();
        }
        //查询条件
        //1.定义条件匹配器
        //页面名称模糊查询，需要自定义字符串的匹配器实现模糊查询, .contains() 包含
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().withMatcher("pageAliase", ExampleMatcher.GenericPropertyMatchers.contains());
        //2.定义条件值
        CmsPage cmsPage = new CmsPage();
        //站点id
        //判断站点id是否为空
        if (StringUtils.isNotEmpty(queryPageRequest.getSiteId())){
            //将站点id添加到cmsPage对象
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //页面别名(模糊查询)
        //判断页面别名是否为空
        if (StringUtils.isNotEmpty(queryPageRequest.getPageAliase())){
            //将页面别名添加到cmsPage对象
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //3.创建条件实例
        //static <T> Example<T> of(T probe, ExampleMatcher matcher),参数1:条件值,参数2:条件匹配器
        Example<CmsPage> example = Example.of(cmsPage, exampleMatcher);

        //分页参数
        //判断页码是否小于0
        if (page <= 0){
            //默认为1
            page = 1;
        }
        page = page -1;//为了适应mongodb的接口将页码减1
        //判断记录条数是否小于0
        if (size <= 0){
            //默认为10
            size = 10;
        }
        //创建分页pageable对象,PageRequest.of,new PageRequest都可以
        Pageable pageable = PageRequest.of(page,size);
        //Pageable pageable = new PageRequest(page,size);
        //调用dao的条件分页查询方法
        // findAll(Example<S> example, Pageable pageable);参数1:封装的条件,参数2:分页对象
        Page<CmsPage> cmsPages = cmsPageRepository.findAll(example, pageable);
        //定义PageQueryResult对象
        QueryResult<CmsPage> cmsPageQueryResult = new QueryResult<>();
        cmsPageQueryResult.setList(cmsPages.getContent());//设置数据列表
        cmsPageQueryResult.setTotal(cmsPages.getTotalElements());//设置数据总记录数
        //定义QueryResponseResult对象
        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,cmsPageQueryResult);
        //返回结果
        return queryResponseResult;
    }

    /**
     * 新增页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult add(CmsPage cmsPage){
        //校验cmsPage是否为空
        if (cmsPage != null){

        }
        //根据条件查询要添加的页面
        CmsPage cmsPage1 = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());
        //判断要添加的页面是否存在
        if (cmsPage1 != null){
            //页面已存在,抛出异常
            ExceptionCast.cast(CmsCode.CMS_ADDPAGE_EXISTSNAME);
        }
            //设置PageId为空(添加页面主键由spring data 自动生成)
            cmsPage.setPageId(null);
            //调用保存(添加)方法,添加页面
            cmsPageRepository.save(cmsPage);
        //创建CmsPageResult对象,返回添加成功
        CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS, cmsPage);
        return cmsPageResult;
    }

    /**
     * 根据id查询页面
     * @param id
     * @return
     */
    public CmsPage findById(String id){
        //Optional是jdk1.8引入的类型，Optional是一个容器对象，它包括了我们需要的对象，使用isPresent方法判断所包
            //含对象是否为空，isPresent方法返回false则表示Optional包含对象为空，否则可以使用get()取出对象进行操作。
                //Optional的优点是：
                //1、提醒你非空判断。
                //2、将对象非空检测标准化。
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (!optional.isPresent()){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出CmsPage对象
        return optional.get();
    }

    /**
     * 修改页面(先查询在修改)
     * @param id
     * @param cmsPage
     * @return
     */
    public CmsPageResult update(String id, CmsPage cmsPage){
        //根据id查询页面
        CmsPage cmsPage1 = findById(id);
        if (cmsPage1 != null){
            //更新模板id
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            //更新所属站点
            cmsPage1.setSiteId(cmsPage.getSiteId());
            //更新页面别名
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
            //更新页面名称
            cmsPage1.setPageName(cmsPage.getPageName());
            //更新访问路径
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            //更新物理路径
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            //更新dataurl
            cmsPage1.setDataUrl(cmsPage.getDataUrl());
            //执行更新
            CmsPage cmsPage2 = cmsPageRepository.save(cmsPage1);
            if (cmsPage2 != null){
                //返回成功
                CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS, cmsPage2);
                return cmsPageResult;
            }
        }
        //返回失败
        return new CmsPageResult(CommonCode.FAIL, null);
    }

    /**
     * 删除页面
     * @param id
     * @return
     */
    public ResponseResult delete(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if (optional.isPresent()){
           cmsPageRepository.deleteById(id);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    /**
     * 根据id查询CmsConfig配置管理信息
     * @param id
     * @return
     */
    public CmsConfig getConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if (optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    //页面静态化方法
    /**
     * 静态化程序获取页面的DataUrl
     * 静态化程序远程请求DataUrl获取数据模型。
     * 静态化程序获取页面的模板信息
     * 执行页面静态化
     */
    public String getPageHtml(String pageId){

        //获取数据模型
        Map model = getModelByPageId(pageId);
        if (model == null){
            //数据模型获取不到
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAISNULL);
        }
        //获取页面模板信息
        String template = getTemplateByPageId(pageId);
        if (StringUtils.isEmpty(template)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //执行页面静态化
        String html = generateHtml(template, model);

        return html;
    }

    /**
     * 获取数据模型
     * @param pageId
     * @return
     */
    private Map getModelByPageId(String pageId){
        //根据id获取页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出dataurl
        String dataUrl = cmsPage.getDataUrl();
        if (StringUtils.isEmpty(dataUrl)){
            //页面dataurl为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_DATAURLISNULL);
        }
        //通过restTemplate请求dataUrl获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        //获取数据体
        Map map = forEntity.getBody();
        return map;
    }

    /**
     * 获取页面模板信息
     * @param pageId
     * @return
     */
    private String getTemplateByPageId(String pageId){
        //根据页面id获取页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //根据页面信息获取该页面的模板id
        String templateId = cmsPage.getTemplateId();
        if (StringUtils.isEmpty(templateId)){
            //页面模板为空
            ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL);
        }
        //根据模板id查询模板信息
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if (optional.isPresent()){
            //获取模板
            CmsTemplate cmsTemplate = optional.get();
            //获取模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //从GridFS中取模板文件内容
            //根据模板文件id查询模板文件
            GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开一个下载流对象
            GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource对象，获取流
            GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
            //从流中取数据
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf-8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 页面静态化(模板信息,数据模型)
     * @param templateContent
     * @param model
     * @return
     */
    private String generateHtml(String templateContent, Map model){
        //根据版本,创建配置对象
        Configuration configuration = new Configuration(Configuration.getVersion());
        //创建模板加载器
        StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
        //设置模板名及内容
        stringTemplateLoader.putTemplate("template", templateContent);
        //向configuration配置模板加载器
        configuration.setTemplateLoader(stringTemplateLoader);
        //获取模板

        try {
            Template template = configuration.getTemplate("template");
            //调用api进行静态化
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return content;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 页面发布
         * 1、管理员进入管理界面点击“页面发布”，前端请求cms页面发布接口。
          2、cms页面发布接口执行页面静态化，并将静态化页面存储至GridFS中。
          3、静态化成功后，向消息队列发送页面发布的消息。
             1） 获取页面的信息及页面所属站点ID。
             2） 设置消息内容为页面ID。（采用json格式，方便日后扩展）
             3） 发送消息给ex_cms_postpage交换机，并将站点ID作为routingKey。
     * @param pageId
     * @return
     */
    public ResponseResult postPage(String pageId){

        //页面静态化
        String pageHtml = this.getPageHtml(pageId);
        if (StringUtils.isEmpty(pageHtml)){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //保存静态化文件
        CmsPage cmsPage = this.saveHtml(pageId, pageHtml);
        //给交换机发送消息
        this.sendPostPage(pageId);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 保存静态化文件
     * @param pageId
     * @param content
     * @return
     */
    public CmsPage saveHtml(String pageId, String content){
        //根据id查询页面信息
        CmsPage cmsPage = this.findById(pageId);
        //存储静态化文件前先删除(根据htmlFileId)
        String htmlFileId = cmsPage.getHtmlFileId();
        if (StringUtils.isNotEmpty(htmlFileId)){
            //删除页面的htmlFileId删除已经存在的静态化文件
            gridFsTemplate.delete(Query.query(Criteria.where("_id").is(htmlFileId)));
        }
        //保存html文件到gridFs
        InputStream inputStream = IOUtils.toInputStream(content);
        ObjectId objectId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        //获取静态化文件id
        String fileId = objectId.toString();
        //将文件id存入CmsPage中
        cmsPage.setHtmlFileId(fileId);
        //保存cmsPage
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }

    /**
     * 给交换机发送消息
     * @param pageId
     */
    public void sendPostPage(String pageId){
        //获取页面信息
        CmsPage cmsPage = this.findById(pageId);
        if (cmsPage == null){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //map集合,存在发送的消息
        Map<String, String> map = new HashMap<>();
        map.put("pageId", pageId);
        //转为json格式
        //消息内容
        String jsonString = JSON.toJSONString(map);
        //获取routingKey(siteId)
        String siteId = cmsPage.getSiteId();
        //发布消息
        this.rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE, siteId, jsonString);
    }

    /**
     * 保存页面
     * @param cmsPage
     * @return
     */
    public CmsPageResult save(CmsPage cmsPage) {
        //根据页面名称、站点Id、页面webpath查询,判断页面是否存在
        CmsPage cmsPage1 = cmsPageRepository.findBySiteIdAndPageNameAndPageWebPath(cmsPage.getSiteId(), cmsPage.getPageName(), cmsPage.getPageWebPath());
        if (cmsPage1 == null){
            //添加
            return this.add(cmsPage);
        }else {
            //更新
            return this.update(cmsPage1.getPageId(), cmsPage);
        }
    }

    /**
     * 课程详情页面一键发布
     *      1.保存页面信息,获取页面id
     *      2.进行页面发布
     *      3.拼接页面url
     * @param cmsPage
     * @return
     */
    public CmsPostPageResult postPageQuick(CmsPage cmsPage) {
        //保存页面信息
        CmsPageResult saveCmsPage = this.save(cmsPage);
        if (!saveCmsPage.isSuccess()){
            //保存失败
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //获取页面信息
        CmsPage cmsPageSave = saveCmsPage.getCmsPage();
        //根据页面信息获取页面的pageId
        String pageId = cmsPageSave.getPageId();
        //进行页面发布
        ResponseResult post = this.postPage(pageId);
        if (!post.isSuccess()){
            //发布失败
            ExceptionCast.cast(CommonCode.FAIL);
        }
        //拼接页面url=cmsSite.siteDomain+cmsSite.siteWebPath+ cmsPage.pageWebPath + cmsPage.pageName
        //获取站点id
        String siteId = cmsPageSave.getSiteId();
        //根据站点id查询站点信息
        CmsSite cmsSite = findCmsSiteById(siteId);
        //拼接页面url
        String pageUrl = cmsSite.getSiteDomain() + cmsSite.getSiteWebPath() + cmsPageSave.getPageWebPath() + cmsPageSave.getPageName();
        return new CmsPostPageResult(CommonCode.SUCCESS, pageUrl);
    }

    /**
     * 根据站点id查询站点信息
     * @param siteId
     * @return
     */
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }
}
