package com.xuecheng.api.auth;

import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * 用户的认证接口(密码模式)
 */
@Api(value = "用户认证接口",description = "用户认证接口")
public interface AuthControllerApi {

    @ApiOperation("用户登录")
    LoginResult login(LoginRequest loginRequest);

    @ApiOperation("用户登录")
    ResponseResult logout();

    @ApiOperation("查询用户的jwt令牌")
    JwtResult userjwt();
}
