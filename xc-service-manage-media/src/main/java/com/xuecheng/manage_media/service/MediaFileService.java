package com.xuecheng.manage_media.service;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MediaFileService {

    @Autowired
    private MediaFileRepository mediaFileRepository;

    /**
     * 分页查询所有媒资列表
     *      1.设置条件值对象
     *      2.设置条件匹配器
     *      3.定义example条件对象
     *      5.分页查询
     * @param page
     * @param size
     * @param queryMediaFileRequest 查询条件
     * @return
     */
    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        if(queryMediaFileRequest == null){
            queryMediaFileRequest = new QueryMediaFileRequest();
        }
        //1.设置条件值对象
        MediaFile mediaFile = new MediaFile();
        //设置标签
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getTag())){
            mediaFile.setTag(queryMediaFileRequest.getTag());
        }
        //设置原始名
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getFileOriginalName())){
            mediaFile.setFileOriginalName(queryMediaFileRequest.getFileOriginalName());
        }
        //设置处理状态
        if(StringUtils.isNotEmpty(queryMediaFileRequest.getProcessStatus())){
            mediaFile.setProcessStatus(queryMediaFileRequest.getProcessStatus());
        }
        //2.设置条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching().
                withMatcher("tag", ExampleMatcher.GenericPropertyMatchers.contains()).
                withMatcher("fileOriginalName", ExampleMatcher.GenericPropertyMatchers.contains());
                //withMatcher("processStatus", ExampleMatcher.GenericPropertyMatchers.exact());//processStatus不设置,默认为精确匹配
        //3.定义example条件对象
        Example<MediaFile> example = Example.of(mediaFile, exampleMatcher);
        //4.创建分页对象
        if(page<=0){
            page = 1;
        }
        page = page-1;
        if(size<=0){
            size = 5;
        }
        Pageable pageable = PageRequest.of(page, size);
        //5.分页查询
        Page<MediaFile> mediaFiles = mediaFileRepository.findAll(example, pageable);
        //获取总记录条数
        long totalElements = mediaFiles.getTotalElements();
        //获取数据列表
        List<MediaFile> mediaFilesContent = mediaFiles.getContent();
        //返回数据集合
        QueryResult<MediaFile> mediaFileQueryResult = new QueryResult<>();
        mediaFileQueryResult.setList(mediaFilesContent);
        mediaFileQueryResult.setTotal(totalElements);

        return new QueryResponseResult(CommonCode.SUCCESS, mediaFileQueryResult);
    }
}
