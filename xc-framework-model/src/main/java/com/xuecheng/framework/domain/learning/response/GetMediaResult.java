package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
/**
 * 返回值:
 *      1.成功与否,
 *      2.媒资文件播放地址
 */
public class GetMediaResult extends ResponseResult{
    //媒资文件播放地址
    private String fileUrl;
    public GetMediaResult(ResultCode resultCode, String fileUrl){
        super(resultCode);
        this.fileUrl = fileUrl;
    }
}