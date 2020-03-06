package cn.hust.spike.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-03-06 18:54
 **/
@Component
@Slf4j
public class RedisLock {

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @param key
     * @param value
     * @return
     */
    public boolean lock(String key, String value){

        //1.首先尝试获取锁
        if(redisTemplate.opsForValue().setIfAbsent(key,value)){
            return true;
        }

        //2.若未获取，则取判断当前的value有没有过期，value= 当前时间+有效时间
        String currentValue = redisTemplate.opsForValue().get(key);
        if(!StringUtils.isEmpty(currentValue) && Long.parseLong(currentValue) < System.currentTimeMillis()){
            //过期我就去获取上个锁，但是可能有多个线程同时来获取，所以还需要来判断
            String oldValue =  redisTemplate.opsForValue().getAndSet(key,value);
            if(StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)){
                return true;
            }
        }
        return false;
    }

    /**
     * 解锁
     */

    public void unlock(String key, String value){

        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if(!StringUtils.isEmpty(currentValue) && currentValue.equals(value)){
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        } catch (Exception e) {
            log.error("【redis解锁异常】,{}",e);
        }


    }
}
