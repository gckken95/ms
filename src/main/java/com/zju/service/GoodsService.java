package com.zju.service;

import com.zju.dao.GoodsDao;
import com.zju.dao.UserDao;
import com.zju.domain.Goods;
import com.zju.domain.MiaoshaGoods;
import com.zju.domain.User;
import com.zju.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GoodsService {
    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoById(long goodsId) {
        return goodsDao.getGoodsVoById(goodsId);
    }

    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g=new MiaoshaGoods();
        g.setGoodsId(goods.getId());

        int ret=goodsDao.reduceStock(g);
        return ret>0;
    }
}
