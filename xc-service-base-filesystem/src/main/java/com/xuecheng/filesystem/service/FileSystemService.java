package com.xuecheng.filesystem.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.filesystem.dao.FileSystemRepository;
import com.xuecheng.framework.domain.filesystem.FileSystem;
import com.xuecheng.framework.domain.filesystem.response.FileSystemCode;
import com.xuecheng.framework.domain.filesystem.response.UploadFileResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import org.apache.commons.lang3.StringUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * 文件系统服务的业务层
 */
@Service
public class FileSystemService {

    //从配置文件中获取信息
    @Value("${xuecheng.fastdfs.tracker_servers}")
    String tracker_servers;
    @Value("${xuecheng.fastdfs.connect_timeout_in_seconds}")
    int connect_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.network_timeout_in_seconds}")
    int network_timeout_in_seconds;
    @Value("${xuecheng.fastdfs.charset}")
    String charset;

    @Autowired
    private FileSystemRepository fileSystemRepository;
    /**
     * 上传文件
     * @param multipartFile
     * @param filetag
     * @param businesskey
     * @param metadata
     * @return
     */
    public UploadFileResult upload(MultipartFile multipartFile, String filetag, String businesskey, String metadata){

        if(multipartFile ==null){
            //上传文件为空
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_FILEISNULL);
        }
        //1.将文件上传到fastDFS,得到一个文件id
        String fileId = upload_fastdfs(multipartFile);
        if (StringUtils.isEmpty(fileId)){
            //文件上传服务器失败
            ExceptionCast.cast(FileSystemCode.FS_UPLOADFILE_SERVERFAIL);
        }
        //2.将得到的文件id及文件的其他信息存储到mongoDB数据库
        FileSystem fileSystem = new FileSystem();
        fileSystem.setFileId(fileId);
        fileSystem.setFilePath(fileId);
        fileSystem.setFiletag(filetag);
        fileSystem.setBusinesskey(businesskey);
        fileSystem.setFileName(multipartFile.getOriginalFilename());
        fileSystem.setFileType(multipartFile.getContentType());
        if (StringUtils.isNotEmpty(metadata)){
            //将字符串转为map集合
            Map map = JSON.parseObject(metadata, Map.class);
            fileSystem.setMetadata(map);
        }
        //保存到数据库
        fileSystemRepository.save(fileSystem);
        return new UploadFileResult(CommonCode.SUCCESS, fileSystem);
    }

    /**
     * 1.将文件上传到fastDFS,得到一个文件id
     * @param multipartFile
     * @return fileId
     */
    private String upload_fastdfs(MultipartFile multipartFile){
        //初始化fastDFS的环境
        initFastdfsConfig();
        try {
            //创建trackerClient
            TrackerClient trackerClient = new TrackerClient();
            //连接trackerServer
            TrackerServer trackerServer = trackerClient.getConnection();
            //获取storageServer
            StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
            //创建storageClient1
            StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
            //上传文件到fastDFS
            //获取上传文件字节形式
            byte[] multipartFileBytes = multipartFile.getBytes();
            //得到文件的原始名称
            String originalFilename = multipartFile.getOriginalFilename();
            //得到文件的拓展名
            String file_ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            //开始上传,参数1:文件的字节,参数2:文件的拓展名,参数3:文件的信息
            String fileId = storageClient1.upload_file1(multipartFileBytes, file_ext, null);
            return fileId;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 初始化fastDFS的环境
     */
    private void initFastdfsConfig(){

        try {
            ClientGlobal.initByTrackers(tracker_servers);
            ClientGlobal.setG_charset(charset);
            ClientGlobal.setG_network_timeout(network_timeout_in_seconds);
            ClientGlobal.setG_connect_timeout(connect_timeout_in_seconds);
        } catch (Exception e) {
            e.printStackTrace();
            //初始化fastDFS的环境失败
            ExceptionCast.cast(FileSystemCode.FS_INITFDFSERROR);
        }
    }
}
