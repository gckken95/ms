package com.zju.controller;

import com.zju.domain.MiaoshaUser;
import com.zju.domain.OrderInfo;
import com.zju.redis.RedisService;
import com.zju.result.CodeMsg;
import com.zju.result.Result;
import com.zju.service.GoodsService;
import com.zju.service.MiaoshaUserService;
import com.zju.service.OrderService;
import com.zju.vo.GoodsVo;
import com.zju.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;


    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> detail(MiaoshaUser user,
                                         @RequestParam("orderId")long orderId){
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        OrderInfo order=orderService.getOrderById(orderId);
        if(order==null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId=order.getGoodsId();
        GoodsVo goods=goodsService.getGoodsVoById(goodsId);

        OrderDetailVo vo=new OrderDetailVo();
        vo.setGoods(goods);
        vo.setOrder(order);
        return Result.success(vo);
    }




}
