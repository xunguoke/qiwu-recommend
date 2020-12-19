package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * 根据用户信息选择相对应的方法返回推荐作品
 * @author hjd
 */
@Slf4j
public class JudgmentIntention {
    /**
     * 判断用户意图
     *
     *
     *
     * @param channelId
     * @param redisTemplate
     * @param watches
     * @param intention 用户意图信息
     * @param uid
     * @return
     */
    public static String methodOfChoosing(String channelId, RedisTemplate redisTemplate, List<Watch> watches, DataResponse dataResponse, String intention, String semantics, String uid) {
        log.warn("数据:{}",intention);
        //判断用户具体信息
        if(intention.equals("手表推荐之推荐")){
            log.warn("到这里");
            return IntentionUtils.recommenda(dataResponse, semantics);
        }else if(intention.equals("手表推荐之类型推荐")){
            return IntentionUtils.typeRecommendation(dataResponse,semantics);
        }else if(intention.equals("手表推荐之最新推荐")){
            return IntentionUtils.latestCreation(dataResponse,semantics);
        }else if(intention.equals("手表推荐之类似作品推荐")){
            return IntentionUtils.similarWorks(dataResponse,semantics);
        }else if(intention.equals("手表推荐之某作者的作品推荐")){
            return IntentionUtils.authorWorks(dataResponse,semantics);
        }else if(intention.equals("手表推荐之作品类型查询")){
            return IntentionUtils.typeOfWork(dataResponse,semantics);
        }else if(intention.equals("手表推荐之作者查询")){
            return IntentionUtils.authorQuery(dataResponse,semantics);
        }else if(intention.equals("手表推荐之作品编号查询")){
            return IntentionUtils.workNumber(dataResponse,semantics);
        }else if(intention.equals("手表推荐之人群推荐")){
            return IntentionUtils.crowdRecommendation(dataResponse,semantics);
        }else if(intention.equals("手表推荐之收藏最多的作品")){
            return IntentionUtils.mostFavorites(dataResponse,semantics);
        }else if(intention.equals("手表推荐之作品简介查询")){
            return IntentionUtils.introduction(dataResponse,semantics);
        }else if(intention.equals("手表推荐之系列推荐")) {
            return IntentionUtils.seriesRecommendation(dataResponse, semantics);
        }else if(intention.equals("手表推荐之类型")){
                return IntentionUtils.type(channelId,dataResponse,semantics,uid,redisTemplate);
        }else if(intention.equals("手表推荐之系列查询")){
            log.warn("到这里");
            return IntentionUtils.seriesQuery(channelId,dataResponse,semantics,uid,redisTemplate);
        }else {
            log.warn("到这里2");
            return null;
        }

    }
}
