package com.example.redis_practice.service.impl;

import com.example.redis_practice.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    private final StringRedisTemplate redisTemplate;

    public SeckillServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean doSeckill(String userId, String prodId) {
        //1. 参数非空判断
        if (userId == null || prodId == null) return false;
        //2. 拼接key
        //库存
        String storeKey = "sk:" + prodId + ":store";
        //秒杀成功用户
        String userKey = "sk:" + prodId + ":user";

        //3. 获取库存，如为null，则秒杀还未开始，如果<=0，秒杀结束
        String storeNum = redisTemplate.opsForValue().get(storeKey);
        if (storeNum == null) {
            log.debug("秒杀还未开始");
            return false;
        }
        if (Integer.parseInt(storeNum) <= 0) {
            log.debug("秒杀已结束");
            return false;
        }
        //4. 获取秒杀成功用户，防止重复秒杀
        Boolean isSucceed = redisTemplate.opsForSet().isMember(userKey, userId);
        if (Boolean.TRUE.equals(isSucceed)) {
            log.debug("重复秒杀，退回");
            return false;
        }

        List<Object> execRes = redisTemplate.execute(new SessionCallback<List<Object>>() {
            public List<Object> execute(RedisOperations operations) throws DataAccessException {
                //给库存key添加乐观锁
                operations.watch(storeKey);
                //5. 库存-1，秒杀成功用户添加
                operations.multi();
                operations.opsForValue().decrement(storeKey);
                operations.opsForSet().add(userKey, userId);
                return operations.exec();
            }
        });


        log.debug(userId+" result:"+execRes);
        return true;
    }
}
