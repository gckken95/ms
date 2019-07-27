package com.zju.controller;

import com.zju.domain.MiaoshaUser;
import com.zju.redis.RedisService;
import com.zju.result.Result;
import com.zju.service.GoodsService;
import com.zju.service.MiaoshaUserService;
import com.zju.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;



    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model, MiaoshaUser user){
        model.addAttribute("user",user);
        return Result.success(user);
    }




}
