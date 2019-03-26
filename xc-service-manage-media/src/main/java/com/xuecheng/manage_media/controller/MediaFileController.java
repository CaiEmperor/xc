package com.xuecheng.manage_media.controller;

import com.xuecheng.api.media.MediaFileControllerApi;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.manage_media.service.MediaFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 媒资文件管理
 */
@RestController
@RequestMapping("/media/file")
public class MediaFileController implements MediaFileControllerApi{

    @Autowired
    MediaFileService mediaFileService;

    /**
     * 分页查询所有媒资列表
     * @param page
     * @param size
     * @param queryMediaFileRequest
     * @return
     */
    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest) {
        return mediaFileService.findList(page, size, queryMediaFileRequest);
    }
}