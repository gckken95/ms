package com.zju.controller;

import com.zju.domain.User;
import com.zju.rabbitmq.MQReceiver;
import com.zju.rabbitmq.MQSender;
import com.zju.redis.RedisService;
import com.zju.redis.UserKey;
import com.zju.result.CodeMsg;
import com.zju.result.Result;
import com.zju.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/demo")
public class DemoController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    @Autowired
    MQReceiver mqReceiver;


    //1.rest api json输出 2.页面
    @RequestMapping("/")
    @ResponseBody
    String home() {
        return "Hello World!";
    }

//    @RequestMapping("/mq")
//    @ResponseBody
//    public Result<String> mq() {
//
//        mqSender.send("hello,imooc");
//        return Result.success("hello,immoc");
//    }
//
//    @RequestMapping("/mq/topic")
//    @ResponseBody
//    public Result<String> mqTopic() {
//
//        mqSender.sendTopic("hello,imooc");
//        return Result.success("hello,immoc");
//    }
//
//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public Result<String> mqFanout() {
//
//        mqSender.sendFanout("hello,imooc");
//        return Result.success("hello,immoc");
//    }
//
//    @RequestMapping("/mq/header")
//    @ResponseBody
//    public Result<String> mqHeader() {
//
//        mqSender.sendHeader("hello,imooc");
//        return Result.success("hello,immoc");
//    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello() {
        return Result.success("hello,immoc");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError() {
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model) {
        model.addAttribute("name","Joshua");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> doGet() {
        User user=userService.getById(1);
        if(user==null){
            System.out.println("::::2");
        }
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx() {
        userService.tx();

        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet() {
        User user=redisService.get(UserKey.getById,""+1,User.class);

        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet() {
        User user=new User();
        user.setId(1);
        user.setName("1111111");
        Boolean ret=redisService.set(UserKey.getById,""+1,user);


        return Result.success(true);
    }

}
