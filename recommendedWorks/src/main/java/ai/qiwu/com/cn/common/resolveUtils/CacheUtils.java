package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 该类用于存储封装的缓存方法
 * @author hjd
 */
public class CacheUtils {
    /**
     * 将需要返回的作品信息保存到缓存中去
     * @param redisTemplate 操作缓存对象
     * @param works 所有作品信息
     */
    public static void cacheSave(RedisTemplate redisTemplate, List<WorksPojo> works) {
        //清空上一轮推荐的作品缓存
        while (redisTemplate.opsForList().size("worksList")>0){
            redisTemplate.opsForList().leftPop("worksList");
        }
        //将作品信息添加到缓存
        redisTemplate.opsForList().rightPushAll("worksList",works);
        //设置过期时间
        redisTemplate.expire("worksList",3, TimeUnit.MINUTES);
    }
}
