package com.xuecheng.govern.gateway.service;

import com.xuecheng.framework.utils.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Service
public class LoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 从cookie查询用户身份令牌
     * @param request
     * @return
     */
    public String getTokenFromCookie(HttpServletRequest request) {
        //获取所有cookie
        Map<String, String> map = CookieUtil.readCookie(request, "uid");
        //取出uid的cookie
        String jti_token = map.get("uid");
        if (StringUtils.isEmpty(jti_token)){
            return null;
        }
        return jti_token;
    }

    /**
     * 从http header查询jwt令牌
     * @param request
     * @return
     */
    public String getJwtFromHeader(HttpServletRequest request) {
        //取出头信息
        String authorization = request.getHeader("Authorization");
        if(StringUtils.isEmpty(authorization)){
            return null;
        }
        if(!authorization.startsWith("Bearer ")){
            return null;
        }
        //获取jwt令牌
        String access_token = authorization.substring(7);
        return access_token;
    }

    /**
     * 从Redis查询user_token令牌过期时间
     * @param jti_token
     * @return
     */
    public long getExpire(String jti_token) {
        String key = "user_token:" + jti_token;
        //查询令牌的过期时间
        Long expire = stringRedisTemplate.getExpire(key);
        return expire;
    }
}
