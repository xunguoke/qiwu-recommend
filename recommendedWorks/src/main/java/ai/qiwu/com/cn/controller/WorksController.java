package ai.qiwu.com.cn.controller;

import ai.qiwu.com.cn.service.RecommendService;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 齐悟作品推荐
 * @author hjd
 */
@RestController
@Slf4j
@RequestMapping("/recommenda")
public class WorksController {
    private final HttpServletRequest request;
    private final WatchService watchService;
    private final RecommendService recommendaService;
    private final RedisTemplate redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    @Autowired(required = false)
    public WorksController(StringRedisTemplate stringRedisTemplate,RedisTemplate redisTemplate,WatchService watchService, RecommendService recommendaService, HttpServletRequest request) {
        this.request=request;
        this.recommendaService=recommendaService;
        this.watchService=watchService;
        this.redisTemplate=redisTemplate;
        this.stringRedisTemplate=stringRedisTemplate;
    }

    /**
     * 推荐作品
     * @return
     */
    @PostMapping("/watch")
    public String works(){
        long startTime=System.currentTimeMillis();
        String recommendations = recommendaService.getRecommendations(request, watchService, redisTemplate,stringRedisTemplate);
        long endTime=System.currentTimeMillis();
        System.out.println("程序运行时长："+(endTime-startTime));
        return recommendations;
    }
}
