package com.xuecheng.auth.service;

import com.alibaba.fastjson.JSON;
import com.xuecheng.framework.client.XcServiceList;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.exception.ExceptionCast;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @version 1.0
 **/
@Service
public class AuthService {

    @Value("${auth.tokenValiditySeconds}")
    int tokenValiditySeconds;
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 用户认证申请令牌，将令牌存储到redis
     *      1.从eureka中远程请求spring security申请令牌
     *      2.获取用户身份令牌
     *      3.将用户身份令牌转为json字符串
     *      4.将令牌存储到redis
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    public AuthToken login(String username, String password, String clientId, String clientSecret) {

        //1.从eureka中远程请求spring security申请令牌
        AuthToken authToken = this.applyToken(username, password, clientId, clientSecret);
        if(authToken == null){
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        //2.获取用户身份令牌
        String jti_token = authToken.getJti_token();
        //3.将用户身份令牌转为json字符串
        String jsonString = JSON.toJSONString(authToken);
        //4.将令牌存储到redis
        boolean result = this.saveToken(jti_token, jsonString, tokenValiditySeconds);
        if (!result) {
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_TOKEN_SAVEFAIL);
        }
        return authToken;

    }

    /**
     * 从eureka中远程请求spring security申请令牌
     *      1.从eureka中获取认证服务的地址（因为spring security在认证服务中）
     *      2.此地址就是http://ip:port
     *      3.令牌申请的地址 http://localhost:40400/auth/oauth/token
     *      4.定义header
     *      5.定义body
     *      6.设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
     *      7.通过restTemplate请求url访问令牌
     *      8.申请获取令牌信息
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @return
     */
    private AuthToken applyToken(String username, String password, String clientId, String clientSecret){
        //1.从eureka中获取认证服务的地址（因为spring security在认证服务中）
        //从eureka中获取认证服务的一个实例的地址
        ServiceInstance serviceInstance = loadBalancerClient.choose(XcServiceList.XC_SERVICE_UCENTER_AUTH);
        //2.此地址就是http://ip:port
        URI uri = serviceInstance.getUri();
        //3.令牌申请的地址 http://localhost:40400/auth/oauth/token
        String authUrl = uri+ "/auth/oauth/token";
        //4.定义header
        LinkedMultiValueMap<String, String> header = new LinkedMultiValueMap<>();
        String httpBasic = getHttpBasic(clientId, clientSecret);
        header.add("Authorization",httpBasic);
        //5.定义body
        LinkedMultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","password");
        body.add("username",username);
        body.add("password",password);
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, header);
        //String url, HttpMethod method, @Nullable HttpEntity<?> requestEntity, Class<T> responseType, Object... uriVariables
        //6.设置restTemplate远程调用时候，对400和401不让报错，正确返回数据
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if(response.getRawStatusCode()!=400 && response.getRawStatusCode()!=401){
                    super.handleError(response);
                }
            }
        });
        //7.通过restTemplate请求url访问令牌
        ResponseEntity<Map> exchange = restTemplate.exchange(authUrl, HttpMethod.POST, httpEntity, Map.class);
        //8.申请获取令牌信息
        Map bodyMap = exchange.getBody();
        if(bodyMap == null || bodyMap.get("access_token") == null || bodyMap.get("refresh_token") == null || bodyMap.get("jti") == null){
            //获取spring security返回的错误信息
            String error_description = (String) bodyMap.get("error_description");
            if (StringUtils.isNotEmpty(error_description)){
                if (error_description.equals("坏的凭证")){
                    //账号或密码错误
                    ExceptionCast.cast(AuthCode.AUTH_CREDENTIAL_ERROR);
                }else if (error_description.indexOf("UserDetailsService returned null")>=0){
                    //账号不存在
                    ExceptionCast.cast(AuthCode.AUTH_ACCOUNT_NOTEXISTS);
                }
            }
            //申请令牌失败
            ExceptionCast.cast(AuthCode.AUTH_LOGIN_APPLYTOKEN_FAIL);
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccess_token((String) bodyMap.get("access_token"));//jwt令牌
        authToken.setRefresh_token((String) bodyMap.get("refresh_token"));//刷新令牌
        authToken.setJti_token((String) bodyMap.get("jti"));//用户身份令牌
        return authToken;
    }

    /**
     * 获取httpbasic的字符串
     * 什么是http Basic认证？
     * http协议定义的一种认证方式，将客户端id和客户端密码按照“客户端ID:客户端密码”的格式拼接，并用base64编码，放在header中请求服务端
     * Authorization：Basic WGNXZWJBcHA6WGNXZWJBcHA=
     * WGNXZWJBcHA6WGNXZWJBcHA= 是用户名:密码的base64编码。
     * @param clientId
     * @param clientSecret
     * @return
     */
    private String getHttpBasic(String clientId,String clientSecret){
        String string = clientId+":"+clientSecret;
        //将串进行base64编码
        byte[] encode = Base64Utils.encode(string.getBytes());
        return "Basic "+new String(encode);
    }

    /**
     *存储到令牌到redis
     * @param jti_token 用户身份令牌 key
     * @param content  内容就是AuthToken对象的内容 value
     * @param ttl 过期时间
     * @return
     */
    private boolean saveToken(String jti_token,String content,long ttl){
        String key = "user_token:" + jti_token;
        //进行存储到redis
        stringRedisTemplate.boundValueOps(key).set(content,ttl, TimeUnit.SECONDS);
        //获取存储的结果
        Long expire = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);
        return expire>0;
    }

    /**
     * 从redis中查询用户jwt令牌
     * @param jti_token
     * @return
     */
    public AuthToken getUserToken(String jti_token){
        String key = "user_token:" + jti_token;
        //从redis中查询用户jwt令牌信息(json)
        String value = stringRedisTemplate.opsForValue().get(key);
        //将令牌信息转为对象
        try {
            AuthToken authToken = JSON.parseObject(value, AuthToken.class);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从redis中删除令牌
     * @param jti_token
     * @return
     */
    public boolean delToken(String jti_token){
        String key = "user_token:" + jti_token;
        //从redis中删除令牌
        stringRedisTemplate.delete(key);
        return true;
    }
}
