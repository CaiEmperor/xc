package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.request.QueryMediaFileRequest;
import com.xuecheng.framework.model.response.QueryResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 媒资文件管理接口
 */
@Api(value="媒资文件管理接口",description = "媒资文件管理管理接口，提供视频的处理")
public interface MediaFileControllerApi {

    @ApiOperation("分页查询媒资列表")
    QueryResponseResult<MediaFile> findList(int page, int size, QueryMediaFileRequest queryMediaFileRequest);

}
