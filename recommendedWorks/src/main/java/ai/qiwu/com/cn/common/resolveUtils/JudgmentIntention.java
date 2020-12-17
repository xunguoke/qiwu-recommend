package ai.qiwu.com.cn.common.resolveUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 根据用户信息选择相对应的方法返回推荐作品
 * @author hjd
 */
@Slf4j
public class JudgmentIntention {
    /**
     * 判断用户意图
     * @param map 所有作品信息
     * @param intent 用户意图信息
     * @return
     */
    public static String methodOfChoosing(Map map, String intent, String semantics) {
        log.warn("intent:{}",intent);
        String tj="手表推荐之推荐";
        //判断用户具体信息
        if(intent.equals(tj)){
            String recommend = IntentionUtils.recommenda(map, semantics);
            log.warn("recommend");
            return recommend;
        }else if(intent.equals("手表推荐之类型推荐")){
            log.warn("bbbb");
            return IntentionUtils.typeRecommendation(map,semantics);
        }else if(intent.equals("手表推荐之最新推荐")){
            return IntentionUtils.latestCreation(map,semantics);
        }else if(intent.equals("手表推荐之类似作品推荐")){
            return IntentionUtils.similarWorks(map,semantics);
        }else if(intent.equals("手表推荐之某作者的作品推荐")){
            return IntentionUtils.authorWorks(map,semantics);
        }else if(intent.equals("手表推荐之作品类型查询")){
            return IntentionUtils.typeOfWork(map,semantics);
        }else if(intent.equals("手表推荐之作者查询")){
            return IntentionUtils.authorQuery(map,semantics);
        }else if(intent.equals("手表推荐之作品编号查询")){
            return IntentionUtils.workNumber(map,semantics);
        }else if(intent.equals("手表推荐之人群推荐")){
            return IntentionUtils.crowdRecommendation(map,semantics);
        }else if(intent.equals("手表推荐之收藏最多的作品")){
            return IntentionUtils.mostFavorites(map,semantics);
        }else if(intent.equals("手表推荐值作品简介查询")){
            return IntentionUtils.introduction(map,semantics);
        }else if(intent.equals("手表推荐之系列推荐")) {
            return IntentionUtils.seriesRecommendation(map, semantics);


        }else if(intent.equals("手表推荐之类型")){
                return IntentionUtils.type(map,semantics);
        }else {
            return null;
        }

    }
}
