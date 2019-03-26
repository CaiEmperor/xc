package com.xuecheng.test.fastdfs;

import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * fastDFS测试
 *
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestFastDFS {

    /**
     * 上传文件
     * @throws IOException
     * @throws MyException
     */
    @Test
    public void uploadTest() throws IOException, MyException {
        //加载fastdfs-client.properties配置文件
        ClientGlobal.initByProperties("config/fastdfs-client.properties");
        //定义TrackerClient，用于请求TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //连接TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取StroageServer
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        //创建stroageClient
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
        //向stroage服务器上传本地文件
        //本地文件的路径
        String filePath = "E:/IMG_2656.JPG";
        //上传成功后拿到文件Id
        String fileId = storageClient1.upload_file1(filePath, "JPG", null);
        System.out.println(fileId);//group1/M00/00/01/wKgZmVwmECeAVoV9AAxGQtEfCQI586.JPG
    }

    /**
     * 下载文件
     * @throws IOException
     * @throws MyException
     */
    @Test
    public void downloadTest() throws IOException, MyException {
        //加载fastdfs-client.properties配置文件
        ClientGlobal.initByProperties("config/fastdfs-client.properties");
        //定义TrackerClient，用于请求TrackerServer
        TrackerClient trackerClient = new TrackerClient();
        //连接TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();
        //获取StroageServer
        StorageServer storageServer = trackerClient.getStoreStorage(trackerServer);
        //创建stroageClient
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, storageServer);
        //下载文件
        //文件id
        String fileId = "group1/M00/00/01/wKgZmVwmECeAVoV9AAxGQtEfCQI586.JPG";
        byte[] bytes = storageClient1.download_file1(fileId);
        //使用输出流保存文件
        FileOutputStream fileOutputStream = new FileOutputStream(new File("d:/IMG_2656.JPG"));
        fileOutputStream.write(bytes);
    }

}
