package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
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
     * @param intent 用户意图信息
     * @param map 所有作品信息
     * @return
     */
    public static String methodOfChoosing(IntentionRequest intent, Map map) {
        String recommend=null;
        //判断用户具体信息
            switch (intent.getWorks()){
                case "作品":
                    recommend = IntentionUtils.recommend(map);
                    log.warn("返回信息:{}",recommend);
                    break;
                default:
                    break;
            }


        return recommend;
    }
}
