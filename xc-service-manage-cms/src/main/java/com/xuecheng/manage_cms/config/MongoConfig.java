package com.xuecheng.manage_cms.config;


import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Mongodb的配置类
 */
@Configuration
public class MongoConfig {
    //从配置文件中取数据库
    @Value("${spring.data.mongodb.database}")
    String db;

    @Bean
    //GridFSBucket用于打开下载流对象
    public GridFSBucket getGridFSBucket(MongoClient mongoClient){
        MongoDatabase database = mongoClient.getDatabase(db);
        GridFSBucket bucket = GridFSBuckets.create(database);
        return bucket;
    }
}
