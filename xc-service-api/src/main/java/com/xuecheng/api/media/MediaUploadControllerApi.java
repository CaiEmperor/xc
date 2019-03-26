package com.xuecheng.api.media;

import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口
 */
@Api(value="文件上传管理接口",description = "文件上传管理接口，提供视频的上传,分块,合并")
public interface MediaUploadControllerApi {

    /**
     * 上传文件前准备工作,检验文件是否存在
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    @ApiOperation("文件上传注册")
    ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

    /**
     * 检验分块文件是否存在
     * @param fileMd5
     * @param chunk
     * @param chunkSize
     * @return
     */
    @ApiOperation("检验分块文件是否存在")
    CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize);

    /**
     * 上传分块
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    @ApiOperation("上传分块")
    ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk);

    /**
     * 合并分块
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    @ApiOperation("合并分块")
    ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt);

}
