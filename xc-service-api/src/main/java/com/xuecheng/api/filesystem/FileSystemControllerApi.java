package com.xuecheng.api.filesystem;

import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件系统服务接口
 */
@Api(value="文件系统服务管理接口",description = "文件系统服务管理接口，提供文件的增、删、改、查")
public interface FileSystemControllerApi {

    /**
     *
     * @param multipartFile 文件(SpringMVC文件上传)
     * @param filetag 文件标签，由于文件系统服务是公共服务，文件系统服务会为使用文件系统服务的子系统分配文件标签，用于标识此文件来自哪个系统
     * @param businesskey 业务key,文件系统服务为其它子系统提供的一个业务标识字段，各子系统根据自己的需求去使用
     * @param metadata 文件相关的元信息。
     * @return
     */
    @ApiOperation("上传文件")
    UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata);
}
