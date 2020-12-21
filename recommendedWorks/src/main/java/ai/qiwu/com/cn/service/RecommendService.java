package ai.qiwu.com.cn.service;
import ai.qiwu.com.cn.common.resolveUtils.JudgmentIntention;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

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

        //调用方法返回结果信息
        String works = JudgmentIntention.judgmentIntention(request,watchService,redisTemplate);

        return works;


    }
}
