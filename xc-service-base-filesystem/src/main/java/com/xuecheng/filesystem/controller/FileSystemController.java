package com.xuecheng.filesystem.controller;

import com.xuecheng.api.filesystem.FileSystemControllerApi;
import com.xuecheng.filesystem.service.FileSystemService;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件系统服务控制器
 */
@RestController
@RequestMapping("/filesystem")
public class FileSystemController implements FileSystemControllerApi{

    @Autowired
    private FileSystemService fileSystemService;

    /**
     * 上传文件
     * @param multipartFile 文件(SpringMVC文件上传)
     * @param filetag 文件标签，由于文件系统服务是公共服务，文件系统服务会为使用文件系统服务的子系统分配文件标签，用于标识此文件来自哪个系统
     * @param businesskey 业务key,文件系统服务为其它子系统提供的一个业务标识字段，各子系统根据自己的需求去使用
     * @param metadata 文件相关的元信息。
     * @return
     */
    @Override
    @PostMapping("/upload")
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata) {
        return fileSystemService.upload(multipartFile, filetag, businesskey, metadata);
    }
}
