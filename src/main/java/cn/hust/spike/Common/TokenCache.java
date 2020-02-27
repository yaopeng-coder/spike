package cn.hust.spike.Common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @program: spike
 * @author: yaopeng
 * @create: 2020-02-27 16:24
 **/
@Slf4j
public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    public static final String TOKEN_PREFIX = "token_";

    //Guava cache就是一个具有控制内存大小，缓存过期策略的ConcurrentHashmap
    private static LoadingCache<String,Object> localCache = CacheBuilder.newBuilder()
            //设置缓存容器的初始容量为10
            .initialCapacity(10)
            //设置缓存中最大可以存储100个KEY,超过100个之后会按照LRU的策略移除缓存项
            .maximumSize(100)
            //设置写缓存后60秒过期 因为是热点数据，而且本地缓存对脏读极其不敏感，所以设置时间短， 因为redis脏数据可以手动清除，但是本地缓存不太好处理
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build(new CacheLoader<String, Object>() {
                //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                @Override
                public Object load(String s) throws Exception {
                    return null;
                }
            });

    public static void setKey(String key,Object value){
        localCache.put(key,value);
    }

    public static Object getKey(String key){
        Object value = null;
        try {

            return localCache.getIfPresent(key);
        }catch (Exception e){
            logger.error("localCache get error",e);
        }
        return null;
    }

}
