package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsConfig;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * CmsConfig持久层接口
 */
public interface CmsConfigRepository extends MongoRepository<CmsConfig, String> {
}
