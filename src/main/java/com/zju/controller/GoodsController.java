package com.zju.controller;

import com.zju.domain.Goods;
import com.zju.domain.MiaoshaUser;
import com.zju.domain.User;
import com.zju.redis.GoodsKey;
import com.zju.redis.RedisService;
import com.zju.result.Result;
import com.zju.service.GoodsService;
import com.zju.service.MiaoshaUserService;
import com.zju.vo.GoodsDetailVo;
import com.zju.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/goods")
public class GoodsController {
    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;


    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;

    /*
    * qps:
    *
    * */

    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String toList(HttpServletRequest request,
            HttpServletResponse response, Model model, MiaoshaUser user){
        model.addAttribute("user",user);

        String html=redisService.get(GoodsKey.getGoodsList,"",String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        List<GoodsVo> goodsList=goodsService.listGoodsVo();

        model.addAttribute("goodsList",goodsList);

//        return "goods_list";
        //取缓存

        //手动渲染
        SpringWebContext ctx=new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        html=thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value = "/to_detail2/{goodsId}",produces = "text/html")
    @ResponseBody
    public String toDetail2(HttpServletRequest request,
                           HttpServletResponse response,Model model, MiaoshaUser user,
                           @PathVariable("goodsId")long goodsId){
        model.addAttribute("user",user);

        String html=redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        GoodsVo goods=goodsService.getGoodsVoById(goodsId);
        model.addAttribute("goods",goods);

        long startAt=goods.getStartDate().getTime();
        long endAt=goods.getEndDate().getTime();
        long now=System.currentTimeMillis();

        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){
            miaoshaStatus=0;
            remainSeconds=(int)(startAt-now)/1000;
        }else if(now>endAt){
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{
            miaoshaStatus=1;
            remainSeconds=0;
        }

        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);

//        return "goods_detail";

        //手动渲染
        SpringWebContext ctx=new SpringWebContext(request,response,
                request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
        html=thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
        if(!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }

    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request,
                                    HttpServletResponse response, MiaoshaUser user,
                                    @PathVariable("goodsId")long goodsId){


        GoodsVo goods=goodsService.getGoodsVoById(goodsId);


        long startAt=goods.getStartDate().getTime();
        long endAt=goods.getEndDate().getTime();
        long now=System.currentTimeMillis();

        int miaoshaStatus=0;
        int remainSeconds=0;
        if(now<startAt){
            miaoshaStatus=0;
            remainSeconds=(int)(startAt-now)/1000;
        }else if(now>endAt){
            miaoshaStatus=2;
            remainSeconds=-1;
        }else{
            miaoshaStatus=1;
            remainSeconds=0;
        }


//        return "goods_detail";
        GoodsDetailVo vo=new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setMiaoshaStatus(miaoshaStatus);
        vo.setRemainSeconds(remainSeconds);

        return Result.success(vo);
    }


}
