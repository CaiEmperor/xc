package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsSite;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * CmsSite持久层接口
 */
public interface CmsSiteRepository extends MongoRepository<CmsSite, String> {
}
