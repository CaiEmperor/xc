package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * CmsPage持久层接口
 */
public interface CmsPageRepository extends MongoRepository<CmsPage, String> {

    /**
     * 查询页面(根据站点id,页面名称,页面访问路径)
     * @param siteId
     * @param pageName
     * @param pageWebPath
     * @return
     */
    CmsPage findBySiteIdAndPageNameAndPageWebPath(String siteId, String pageName, String pageWebPath);
}
