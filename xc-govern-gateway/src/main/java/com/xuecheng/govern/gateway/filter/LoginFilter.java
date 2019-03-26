package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.govern.gateway.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 网关服务
 */
@Component//使用@Component标识为bean
public class LoginFilter extends ZuulFilter {

    @Autowired
    private LoginService loginService;

    /**
     *filterType：返回字符串代表过滤器的类型，如下
                 ​	pre：请求在被路由之前执行
                 ​	routing：在路由请求时调用
                 ​	post：在routing和errror过滤器之后调用
                 ​	error：处理请求时发生错误调用
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 此方法返回整型数值，通过此数值来定义过滤器的执行顺序，数字越小优先级越高。
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 返回一个Boolean值，判断该过滤器是否需要执行。返回true表示要执行此过虑器，否则不执行
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤器的业务逻辑
     * 1、从cookie查询用户身份令牌是否存在，不存在则拒绝访问
     *2、从http header查询jwt令牌是否存在，不存在则拒绝访问
     *3、从Redis查询user_token令牌是否过期，过期则拒绝访问
     * @return
     * @throws ZuulException
     */
    @Override
    public Object run() throws ZuulException {
        //1.上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        //2.请求对象
        HttpServletRequest request = requestContext.getRequest();
        //3.从cookie查询用户身份令牌
        String jti_token = loginService.getTokenFromCookie(request);
        if (StringUtils.isEmpty(jti_token)){
            //拒绝访问
            access_denied();
            return null;
        }
        //4.从http header查询jwt令牌
        String access_token = loginService.getJwtFromHeader(request);
        if (StringUtils.isEmpty(access_token)){
            //拒绝访问
            access_denied();
            return null;
        }
        //5.从Redis查询user_token令牌过期时间
        long expire = loginService.getExpire(jti_token);
        if (expire < 0){
            //拒绝访问
            access_denied();
            return null;
        }
        return null;
    }

    /**
     * 拒绝访问
     */
    private void access_denied(){
        //创建上下文对象
        RequestContext requestContext = RequestContext.getCurrentContext();
        //得到响应对象
        HttpServletResponse response = requestContext.getResponse();
        //拒绝访问
        requestContext.setSendZuulResponse(false);
        //设置响应内容
        ResponseResult responseResult =new ResponseResult(CommonCode.UNAUTHENTICATED);
        //将内容转为json
        String responseResultString  = JSON.toJSONString(responseResult);
        //设置状态码
        requestContext.setResponseStatusCode(200);
        //设置响应格式
        response.setContentType("application/json;charset=utf-8");
    }
}
