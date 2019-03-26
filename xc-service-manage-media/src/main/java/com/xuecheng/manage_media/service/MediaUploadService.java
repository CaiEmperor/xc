package com.xuecheng.manage_media.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.domain.media.MediaFile;
import com.xuecheng.framework.domain.media.response.CheckChunkResult;
import com.xuecheng.framework.domain.media.response.MediaCode;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_media.config.RabbitMQConfig;
import com.xuecheng.manage_media.dao.MediaFileRepository;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
public class MediaUploadService {

    @Value("${xc-service-manage-media.upload-location}")
    private String upload_location;
    @Value("${xc-service-manage-media.mq.routingkey-media-video}")
    private String routingkey_media_video;
    @Autowired
    private MediaFileRepository mediaFileRepository;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    //根据文件md5得到文件路径
    //规则：
    //一级目录：md5的第一个字符
    //二级目录：md5的第二个字符
    //三级目录：md5
    //文件名：md5+文件扩展名
    /**
     * 获取上传文件所属的目录路径
     * @param fileMd5
     * @return
     */
    private String getFileFolderPath(String fileMd5){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/";
    }

    /**
     * 获取上传文件的的路径
     * @param fileMd5
     * @param fileExt 文件拓展名
     * @return
     */
    private String getFilePath(String fileMd5, String fileExt){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/"+fileMd5+"."+fileExt;
    }

    /**
     * 获取块文件所属目录路径
     * @param fileMd5
     * @return
     */
    private String getChunkFileFolderPath(String fileMd5){
        return upload_location+fileMd5.substring(0,1)+"/"+fileMd5.substring(1,2)+"/"+fileMd5+"/chunks/";
    }
    /**
     * 文件上传前的注册，检查文件是否存在
     *      1.检查上传文件在磁盘上是否存在
     *          1.获取上传文件所属的目录路径,判断其是否存在,不存在则创建
     *          2.获取上传文件路径,判断上传文件是否存在
     *      2.检查文件信息在mongodb中是否存在
     *          1.根据fileMd5查询数据库中上传文件是否存在,存在则提示上传文件已存在
     * @param fileMd5 文件md5值
     * @param fileExt 文件扩展名
     * @return 文件路径
     */
    public ResponseResult register(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1.检查上传文件在磁盘上是否存在
        //获取上传文件所属的目录路径
        String fileFolderPath = this.getFileFolderPath(fileMd5);
        //文件不存在时作一些准备工作，检查文件所在目录是否存在，如果不存在则创建
        File fileFolder = new File(fileFolderPath);
        if (!fileFolder.exists()){
            fileFolder.mkdirs();
        }
        //得到上传文件的路径
        String filePath = this.getFilePath(fileMd5, fileExt);
        File file = new File(filePath);
        //判断文件是否存在
        boolean exists = file.exists();
        //2.检查文件信息在mongodb中是否存在
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(fileMd5);
        if (exists && mediaFileOptional.isPresent()){
            //上传文件已存在
            ExceptionCast.cast(MediaCode.UPLOAD_FILE_REGISTER_EXIST);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 检验分块文件是否存在
     *      1.获取分块文件目录,判断分块文件是否存在
     * @param fileMd5
     * @param chunk 块文件下标
     * @param chunkSize 块文件大小
     * @return
     */
    public CheckChunkResult checkchunk(String fileMd5, Integer chunk, Integer chunkSize) {
        //检查分块文件是否存在
        //得到分块文件的所在目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //块文件
        File chunkfile = new File(chunkFileFolderPath+chunk);
        if (chunkfile.exists()){
            //分块文件已存在
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, true);
        }else {
            //分块文件不存在
            return new CheckChunkResult(MediaCode.CHUNK_FILE_EXIST_CHECK, false);
        }
    }

    /**
     * 上传分块文件
     *      1.获取上传分块的所属路径,分块文件的路径,获取得到分块文件
     *      2.获取上传分块文件的输入和输出流,将输入流拷贝到输出流中
     * @param file
     * @param fileMd5
     * @param chunk
     * @return
     */
    public ResponseResult uploadchunk(MultipartFile file, String fileMd5, Integer chunk) {
        //1.获取上传分块的所属路径
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //分块文件的路径
        String chunkFilePath = chunkFileFolderPath + chunk;
        //分块文件
        File chunkFileFolder = new File(chunkFileFolderPath);
        if (!chunkFileFolder.exists()){
            chunkFileFolder.mkdirs();
        }
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            //2.得到上传文件的输入流
            inputStream = file.getInputStream();
            //创建上传文件输出流
            fileOutputStream = new FileOutputStream(new File(chunkFilePath));
            //将输入流赋值到输出流
            IOUtils.copy(inputStream, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 合并分块文件
     *      1.合并所有分块文件
     *          1.获取分块文件的所属目录,得到分块文件列表
     *          2.创建一个合并文件
     *          3.进行合并,根据1,2条件进行排序,边读边写将1写入2
     *      2.检验文件的md5值是否与前端传过来的一致
     *          1.获取mergeFile的输入流
     *          2.获取文件的MD5值
     *          3.判断前端传入的md5值与文件md5值是否一样
     *      3.将文件的信息写入mongodb
     * @param fileMd5
     * @param fileName
     * @param fileSize
     * @param mimetype
     * @param fileExt
     * @return
     */
    public ResponseResult mergechunks(String fileMd5, String fileName, Long fileSize, String mimetype, String fileExt) {
        //1.合并所有分块文件
        //1.1.获取分块文件的所属目录
        String chunkFileFolderPath = this.getChunkFileFolderPath(fileMd5);
        //分块文件
        File chunkFileFolder = new File(chunkFileFolderPath);
        //获取分块列表
        File[] files = chunkFileFolder.listFiles();
        //将数组转化为集合
        List<File> chunkFileList = Arrays.asList(files);
        //1.2.创建一个合并文件
        String filePath = this.getFilePath(fileMd5, fileExt);
        File mergeFile = new File(filePath);
        //执行合并
        mergeFile = this.mergeFile(chunkFileList, mergeFile);
        if (mergeFile == null){
            //合并文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_FAIL);
        }

        //2.检验文件的md5值是否与前端传过来的一致
        boolean checkFileMd5 = this.checkFileMd5(mergeFile, fileMd5);
        if(!checkFileMd5){
            //校验文件失败
            ExceptionCast.cast(MediaCode.MERGE_FILE_CHECKFAIL);
        }
        //3、将文件的信息写入mongodb
        MediaFile mediaFile = new MediaFile();
        mediaFile.setFileId(fileMd5);
        mediaFile.setFileOriginalName(fileName);
        mediaFile.setFileName(fileMd5 + "." +fileExt);
        //文件路径保存相对路径
        String filePath1 = fileMd5.substring(0,1) + "/" + fileMd5.substring(1,2) + "/" + fileMd5 + "/";
        mediaFile.setFilePath(filePath1);
        mediaFile.setFileSize(fileSize);
        mediaFile.setUploadTime(new Date());
        mediaFile.setMimeType(mimetype);
        mediaFile.setFileType(fileExt);
        //状态为上传成功
        mediaFile.setFileStatus("301002");
        mediaFileRepository.save(mediaFile);
        //向MQ发送视频处理消息
        sendProcessVideoMsg(mediaFile.getFileId());

        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 检验文件的md5值是否与前端传过来的一致
     *       1.获取mergeFile的输入流
     *       2.获取文件的MD5值
     *       3.判断前端传入的md5值与文件md5值是否一样
     * @param mergeFile
     * @param fileMd5
     * @return
     */
    private boolean checkFileMd5(File mergeFile, String fileMd5){
        try {
            //创建文件输入流
            FileInputStream fileInputStream = new FileInputStream(mergeFile);
            //得到文件的md5
            String md5Hex = DigestUtils.md5Hex(fileInputStream);
            //与传入的md5忽略大小写比较
            if (md5Hex.equalsIgnoreCase(md5Hex)){
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }
    /**
     * 执行合并
     *      1.获取分块文件的所属目录,得到分块文件列表
     *      2.创建一个合并文件
     *      3.进行合并,根据1,2条件进行排序,边读边写将1写入2
     * @param chunkFileList 要合并文件的集合
     * @param mergeFile 新文件
     * @return
     */
    private File mergeFile(List<File> chunkFileList, File mergeFile){
        try {
            if (mergeFile.exists()) {
                mergeFile.delete();
            } else {
                mergeFile.createNewFile();
            }
            //对块文件进行排序
            Collections.sort(chunkFileList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (Integer.parseInt(o1.getName()) > Integer.parseInt(o2.getName())){
                        return 1;
                    }
                    return -1;
                }
            });
            //创建一个写对象
            RandomAccessFile raf_write = new RandomAccessFile(mergeFile, "rw");
            //字节款冲区
            byte[] bytes = new byte[1024];
            for (File chunkFile : chunkFileList) {
                //创建一个读对象
                RandomAccessFile raf_read = new RandomAccessFile(chunkFile,"r");
                int len = -1;
                while ((len = raf_read.read(bytes)) != -1){
                    raf_write.write(bytes, 0, len);
                }
                raf_read.close();
            }
            raf_write.close();
            return mergeFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 向MQ发送视频处理消息
     * @param mediaId
     * @return
     */
    public ResponseResult sendProcessVideoMsg(String mediaId){
        //根据mediaId查询MediaFile信息
        Optional<MediaFile> mediaFileOptional = mediaFileRepository.findById(mediaId);
        if(!mediaFileOptional.isPresent()){
            return new ResponseResult(CommonCode.FAIL);
        }
        MediaFile mediaFile = mediaFileOptional.get();
        //发送视频处理消息
        HashMap<String, String> mesMap = new HashMap<>();
        mesMap.put("mediaId", mediaId);
        //转为json发送消息
        String jsonString = JSON.toJSONString(mesMap);
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EX_MEDIA_PROCESSTASK, routingkey_media_video, jsonString);
        } catch (AmqpException e) {
            e.printStackTrace();
            return new ResponseResult(CommonCode.FAIL);
        }
        return new ResponseResult(CommonCode.SUCCESS);
    }
}
