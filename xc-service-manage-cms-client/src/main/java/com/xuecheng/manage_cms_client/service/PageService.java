package com.xuecheng.manage_cms_client.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * PageService实现类
 */
@Service
public class PageService {

    @Autowired
    private CmsPageRepository cmsPageRepository;
    @Autowired
    private CmsSiteRepository cmsSiteRepository;
    @Autowired
    //操作GridFS的类
    private GridFsTemplate gridFsTemplate;
    @Autowired
    //用于打开下载流对象
    private GridFSBucket gridFSBucket;

    /**
     * 1.调用dao查询页面信息，获取到页面的物理路径，调用dao查询站点信息，得到站点的物理路径
         页面物理路径=站点物理路径+页面物理路径+页面名称。
         从GridFS查询静态文件内容，将静态文件内容保存到页面物理路径下。
       2.根据消息中的页面id,从数据库中查询页面下载到本地
     * @param pageId
     */
    public void savePageToServerPath(String pageId){

        /**
         * 1..调用dao查询页面信息，获取到页面的物理路径，调用dao查询站点信息，得到站点的物理路径
             页面物理路径=站点物理路径+页面物理路径+页面名称。
             从GridFS查询静态文件内容，将静态文件内容保存到页面物理路径下。
         */
        //根据页面id查询页面信息
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if (!optional.isPresent()){
            //页面不存在
            ExceptionCast.cast(CmsCode.CMS_PAGE_NOTEXISTS);
        }
        //取出页面信息
        CmsPage cmsPage = optional.get();
        //根据页面信息获取该页面的所属站点信息
        Optional<CmsSite> optional1 = cmsSiteRepository.findById(cmsPage.getSiteId());
        if (!optional1.isPresent()){
            //页面所属站点不存在
            ExceptionCast.cast(CmsCode.CMS_SITEPAGE_ISNULL);
        }
        //获取页面所属站点信息
        CmsSite cmsSite = optional1.get();
        //获取下载页面的保存的物理路径=页面所属站点的物理路径(绝对路径)+页面的物理路径(相对路径)+页面名称
        String pagePath = cmsSite.getSitePhysicalPath() + cmsPage.getPagePhysicalPath() + cmsPage.getPageName();

        //2.根据消息中的页面id,从数据库中查询页面下载到本地
        //获取页面的静态化文件的HtmlFileId
        String htmlFileId = cmsPage.getHtmlFileId();
        //根据静态化文件的HtmlFileId获取静态化文件的内容
        GridFSFile gridFSFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(htmlFileId)));
        //打开一个下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建GridFsResource对象，获取流
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream  = null;
        //获取输入流
        try {
            inputStream = gridFsResource.getInputStream();
            if (inputStream == null){
                //生成静态化页面为空
                ExceptionCast.cast(CmsCode.CMS_GENERATEHTML_HTMLISNULL);
            }
            //获取输出流
            fileOutputStream = new FileOutputStream(new File(pagePath));
            //将静态化文件内容保存到pagePath
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            //输入流
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //输出流
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
