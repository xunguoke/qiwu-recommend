package ai.qiwu.com.cn.service;
import ai.qiwu.com.cn.common.resolveUtils.ExtractUtils;
import ai.qiwu.com.cn.common.resolveUtils.TypeRecommendation;
import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
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
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public String getRecommendations(HttpServletRequest request, WatchService watchService, RedisTemplate redisTemplate) {
        //1.获取请求数据,提取用户需求信息
        IntentionRequest intent = TypeRecommendation.getIntent(request);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //获取手表推荐之推荐意图
        String intention = intent.getIntention();
        log.warn("intention:{}",intention);
        //获取语义
        String semantics = intent.getWorks();

        //2.请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();

        //3.查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        log.warn("渠道数据:{}",intention);

        //4.根据渠道ID查询禁用标签
        List<String> labelBlacklist = TypeRecommendation.disableLabel(channelId);

        //4.调用方法返回数据
        String works = ExtractUtils.intersectionWorks(channelId,watches, map, intention, semantics,uid,redisTemplate);

        //返回封装信息
        return works;

    }
}
