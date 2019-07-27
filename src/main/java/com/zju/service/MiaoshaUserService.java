package com.zju.service;

import com.zju.dao.MiaoshaUserDao;
import com.zju.domain.MiaoshaUser;
import com.zju.exception.GlobalException;
import com.zju.redis.MiaoshaUserKey;
import com.zju.redis.RedisService;
import com.zju.result.CodeMsg;
import com.zju.util.MD5Util;
import com.zju.util.UUIDUtil;
import com.zju.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoshaUserService {

    public static final String COOKI_NAME_TOKEN="token";


    @Autowired
    MiaoshaUserDao miaoshaUserDao;

    @Autowired
    RedisService redisService;

    public MiaoshaUser getById(long id){
        MiaoshaUser user=redisService.get(MiaoshaUserKey.getById,""+id,MiaoshaUser.class);
        if(user!=null){
            return user;
        }

        user=miaoshaUserDao.getById(id);
        if(user!=null){
            redisService.set(MiaoshaUserKey.getById,""+id,user);
        }
        return user;
    }

    public boolean updatePassword(String token,long id,String passWordNew){
        MiaoshaUser user=getById(id);
        if(user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        MiaoshaUser toBeUpdate=new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(passWordNew,user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);


        redisService.delete(MiaoshaUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);

        return true;

    }



    public boolean login(HttpServletResponse response,LoginVo loginVo) {
        if(loginVo==null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile=loginVo.getMobile();
        String formPass=loginVo.getPassword();
        MiaoshaUser user=miaoshaUserDao.getById(Long.parseLong(mobile));
        if(user==null){
            throw new GlobalException( CodeMsg.MOBILE_NOT_EXIST);
        }
        String dbPass=user.getPassword();
        String dbsalt=user.getSalt();
        String calcPass= MD5Util.formPassToDBPass(formPass,dbsalt);
        if(!calcPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        String token= UUIDUtil.uuid();
        addCookie(user,token,response);
        return true;
    }

    public MiaoshaUser getByToken(String token,HttpServletResponse response) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user=redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        if(user!=null){
            addCookie(user,token,response);
        }
        return user;
    }

    private void addCookie(MiaoshaUser user,String token,HttpServletResponse response){

        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie=new Cookie(COOKI_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
