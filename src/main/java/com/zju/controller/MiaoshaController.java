package com.zju.controller;

import com.zju.access.AccessLimit;
import com.zju.domain.*;
import com.zju.rabbitmq.MQSender;
import com.zju.rabbitmq.MiaoshaMessage;
import com.zju.redis.GoodsKey;
import com.zju.redis.RedisService;
import com.zju.result.CodeMsg;
import com.zju.result.Result;
import com.zju.service.GoodsService;
import com.zju.service.MiaoshaService;
import com.zju.service.MiaoshaUserService;
import com.zju.service.OrderService;
import com.zju.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    MQSender mqSender;

    /*
    * 系统初始化
    * */

    private Map<Long,Boolean> localOverMap=new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList=goodsService.listGoodsVo();
        if(goodsList==null){
            return;
        }
        for(GoodsVo goods:goodsList){
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),goods.getStockCount());
            localOverMap.put(goods.getId(),false);
        }
    }



    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doMiaosha(MiaoshaUser user,
                            @RequestParam("goodsId")long goodsId,
                                     @PathVariable("path")String path){
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean check=miaoshaService.checkPath(user,goodsId,path);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        //内存标记，减少redis访问
        boolean over=localOverMap.get(goodsId);
        if(over){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //预减库存
        long stock=redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if(stock<0) {
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //判断是否已经秒杀到
        MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }
        //入队
        MiaoshaMessage mm=new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(mm);
        return Result.success(0);
    }

    @AccessLimit(seconds=5,maxCount=10,needLogin=true)
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> result(MiaoshaUser user,
                                     @RequestParam("goodsId")long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result=miaoshaService.getMiaoshaResult(user.getId(),goodsId);
        return Result.success(result);
    }

    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> path(HttpServletRequest request,MiaoshaUser user,
                               @RequestParam("goodsId")long goodsId,
                               @RequestParam(value = "verifyCode")int verifyCode) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        boolean check=miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        String path=miaoshaService.createMiaoshaPath(user,goodsId);

        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response,MiaoshaUser user,
                                               @RequestParam("goodsId")long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        BufferedImage image=miaoshaService.createVerifyCode(user,goodsId);
        try {
            OutputStream out=response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }



    }
}
