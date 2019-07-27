package com.zju.rabbitmq;

import com.zju.domain.MiaoshaOrder;
import com.zju.service.MiaoshaService;
import com.zju.domain.MiaoshaUser;
import com.zju.domain.OrderInfo;
import com.zju.redis.RedisService;
import com.zju.service.GoodsService;
import com.zju.service.OrderService;
import com.zju.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class MQReceiver {

    private static Logger log= LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    AmqpTemplate amqpTemplate;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    OrderService orderService;

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        log.info("receive message:"+message);
        MiaoshaMessage mm=redisService.stringToBean(message,MiaoshaMessage.class);
        MiaoshaUser user=mm.getUser();
        long goodsId=mm.getGoodsId();
        GoodsVo goods=goodsService.getGoodsVoById(goodsId);
        int stock=goods.getStockCount();
        if(stock<=0){
            return;
        }
        MiaoshaOrder order=orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if(order!=null){
            return;
        }
        OrderInfo orderInfo=miaoshaService.miaosha(user,goods);
    }


//    @RabbitListener(queues = MQConfig.QUEUE)
//    public void receive(String message){
//        log.info("receive message:"+message);
//    }
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
//    public void receiveTopic1(String message){
//        log.info("receive topic queue1 message:"+message);
//    }
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
//    public void receiveTopic2(String message){
//        log.info("receive topic queue2 message:"+message);
//    }
//
//    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
//    public void receiveHeaderQueue(byte[] message){
//        log.info("receive header queue message:"+new String(message));
//    }


}
