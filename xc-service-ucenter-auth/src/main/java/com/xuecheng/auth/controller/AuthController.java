package com.xuecheng.auth.controller;

import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 用户认证
 */
@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi {

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     * @param loginRequest
     * @return
     */
    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        //1.申请jwt令牌
        AuthToken authToken = authService.login(loginRequest.getUsername(), loginRequest.getPassword(), clientId, clientSecret);
        //获取身份令牌
        String jti_token = authToken.getJti_token();
        //将令牌存储到cookie
        this.saveCookie(jti_token);

        return new LoginResult(CommonCode.SUCCESS,jti_token);
    }

    /**
     * 将令牌存储到cookie
     * @param token
     */
    private void saveCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, cookieMaxAge, false);
    }

    /**
     * 查询用户的jwt令牌
     * @return
     */
    @Override
    @GetMapping("/userjwt")
    public JwtResult userjwt() {
        //1.从cookie中取出身份令牌
        String uid = getTokenFormCookie();
        if (uid == null){
            return new JwtResult(CommonCode.FAIL, null);
        }
        //2.拿身份令牌从redis中查询jwt令牌
        AuthToken userToken = authService.getUserToken(uid);
        if (userToken != null){
            //3.将查询到的令牌返回给用户
            String access_token = userToken.getAccess_token();
            return new JwtResult(CommonCode.SUCCESS, access_token);
        }
        return null;
    }

    /**
     * 从cookie中取出身份令牌
     * @return
     */
    private String getTokenFormCookie(){
        //request对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        //从cookie中取出身份令牌
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        if (map != null && map.get("uid") != null){
            String uid = map.get("uid");
            return uid;
        }
        return null;
    }

    /**
     * 用户退出
     * @return
     */
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        //1.获取身份令牌
        String uid = this.getTokenFormCookie();
        //2.从redis中删除令牌
        authService.delToken(uid);
        //3.从cookie中清除令牌
        this.clearCookie(uid);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 从cookie中清除令牌
     * @param token
     */
    private void clearCookie(String token){
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        //HttpServletResponse response,String domain,String path, String name, String value, int maxAge,boolean httpOnly
        CookieUtil.addCookie(response, cookieDomain, "/", "uid", token, 0, false);
    }
}
