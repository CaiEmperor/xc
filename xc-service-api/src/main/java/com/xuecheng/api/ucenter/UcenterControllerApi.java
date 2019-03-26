package com.xuecheng.api.ucenter;

import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "用户中心管理接口",description = "用户中心管理")
public interface UcenterControllerApi {

    @ApiOperation("根据用户名查询用户的信息")
    XcUserExt getUserExt(String username);
}
