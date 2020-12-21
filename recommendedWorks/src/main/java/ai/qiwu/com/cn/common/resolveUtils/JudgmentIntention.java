package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 根据用户信息选择相对应的方法返回推荐作品
 * @author hjd
 */
@Slf4j
@Service
public class JudgmentIntention {
    /**
     * 判断用户意图
     * @return
     */
    public static String judgmentIntention(HttpServletRequest request, WatchService watchService, RedisTemplate redisTemplate) {
        //1.获取请求数据,提取用户需求信息
        IntentionRequest intent = TypeRecommendation.getIntent(request);
        String intention = intent.getIntention();
        //判断用户具体信息
        if(intention.equals("手表推荐之推荐")){
            log.warn("到这里");
            return IntentionUtils.recommenda(intent, watchService);
        }else if(intention.equals("手表推荐之类型推荐")){
            return IntentionUtils.typeRecommendation(intent, watchService);
        }else if(intention.equals("手表推荐之最新推荐")){
            return IntentionUtils.latestCreation(intent, watchService);
        }else if(intention.equals("手表推荐之类似作品推荐")){
            return IntentionUtils.similarWorks(intent, watchService);
        }else if(intention.equals("手表推荐之某作者的作品推荐")){
            return IntentionUtils.authorWorks(intent, watchService);
        }else if(intention.equals("手表推荐之作品类型查询")){
            return IntentionUtils.typeOfWork(intent, watchService);
        }else if(intention.equals("手表推荐之作者查询")){
            return IntentionUtils.authorQuery(intent, watchService);
        }else if(intention.equals("手表推荐之作品编号查询")){
            return IntentionUtils.workNumber(intent, watchService);
        }else if(intention.equals("手表推荐之人群推荐")){
            return IntentionUtils.crowdRecommendation(intent, watchService);
        }else if(intention.equals("手表推荐之收藏最多的作品")){
            return IntentionUtils.mostFavorites(intent, watchService);
        }else if(intention.equals("手表推荐之作品简介查询")){
            return IntentionUtils.introduction(intent, watchService);
        }else if(intention.equals("手表推荐之系列推荐")) {
            return IntentionUtils.seriesRecommendation(intent, watchService);
        }else if(intention.equals("手表推荐之类型")){
                return IntentionUtils.type(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之系列查询")){
            return IntentionUtils.seriesQuery(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之作者推荐")){
            return IntentionUtils.recommendedWorks(intent, watchService);
        }else if(intention.equals("手表推荐之历史记录类型查询")){
            return IntentionTool.historyTypeQuery(intent, watchService);
        }else if(intention.equals("手表推荐之历史记录时间段查询")){
            return IntentionTool.timePeriodQuery(intent, watchService);
        }else{
            log.warn("没有查询到合适的意图");
            return null;
        }

    }


}
