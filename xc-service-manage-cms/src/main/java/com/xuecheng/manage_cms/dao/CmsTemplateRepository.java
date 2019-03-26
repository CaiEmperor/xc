package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * CmsTemplate持久层接口
 */
public interface CmsTemplateRepository extends MongoRepository<CmsTemplate, String> {
}
