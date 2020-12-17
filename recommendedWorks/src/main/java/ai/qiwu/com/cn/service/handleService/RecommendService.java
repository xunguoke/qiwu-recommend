package ai.qiwu.com.cn.service.handleService;
import ai.qiwu.com.cn.common.resolveUtils.IntentionUtils;
import ai.qiwu.com.cn.common.resolveUtils.JudgmentIntention;
import ai.qiwu.com.cn.common.resolveUtils.TypeRecommendation;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 解析意图返回推荐作品
 * @author hjd
 */
@Service
@Slf4j
public class RecommendService {
    /**
     * 根据意图返回推荐结果
     * @param request
     * @return
     */
    public String getRecommendations(HttpServletRequest request) {
        //获取请求数据,提取用户需求信息
        IntentionRequest intent = TypeRecommendation.getIntent(request);
        //获取手表推荐之推荐意图
        String intention = intent.getIntention();
        //获取语义
        String semantics = intent.getWorks();

        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();

        //根据意图调用指定方法返回数据
        String aa = JudgmentIntention.methodOfChoosing(map, intention, semantics);
        log.warn("aa",aa);

        //返回封装信息
        return aa;

    }
}
