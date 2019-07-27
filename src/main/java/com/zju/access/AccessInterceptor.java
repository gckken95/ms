package com.zju.access;

import com.alibaba.fastjson.JSON;
import com.zju.domain.MiaoshaUser;
import com.zju.redis.AccessKey;
import com.zju.redis.RedisService;
import com.zju.result.CodeMsg;
import com.zju.result.Result;
import com.zju.service.MiaoshaUserService;
import jdk.internal.org.objectweb.asm.Handle;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod) {
            MiaoshaUser user= getUser(request,response);
            UserContext.setUser(user);

            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit=hm.getMethodAnnotation(AccessLimit.class);
            if(accessLimit==null){
                return true;
            }
            int seconds=accessLimit.seconds();
            int maxCount=accessLimit.maxCount();
            boolean needLogin=accessLimit.needLogin();
            String key=request.getRequestURI();
            if(needLogin){
                if(user==null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key += "_"+user.getId();
            }
            AccessKey accessKey=AccessKey.withExpire(seconds);
            Integer count=redisService.get(accessKey,key,Integer.class);
            if(count==null){
                redisService.set(accessKey,key,1);
            }else if(count<maxCount){
                redisService.incr(accessKey,key);
            }else{
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }

        }
        return  true;
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws Exception{
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out=response.getOutputStream();
        String str=JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }

    private MiaoshaUser getUser(HttpServletRequest request,HttpServletResponse response){

        String paramToken=request.getParameter(MiaoshaUserService.COOKI_NAME_TOKEN);
        String cookieToken=getCookieValue(request,MiaoshaUserService.COOKI_NAME_TOKEN);
        if(StringUtils.isEmpty(cookieToken)&& StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token=StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return miaoshaUserService.getByToken(token,response);
    }

    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[] cookies=request.getCookies();
        if(cookies==null || cookies.length<=0){
            return null;
        }
        for (Cookie cookie:cookies){
            if (cookie.getName().equals(cookiName)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
