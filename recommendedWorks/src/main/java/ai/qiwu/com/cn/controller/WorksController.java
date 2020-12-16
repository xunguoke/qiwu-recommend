package ai.qiwu.com.cn.controller;

import ai.qiwu.com.cn.service.handleService.RecommendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 齐悟作品推荐
 * @author hjd
 */
@RestController
@Slf4j
@RequestMapping("/recommenda")
public class WorksController {
    @Autowired(required = false)
    private HttpServletRequest request;
    @Autowired(required = false)
    private HttpServletResponse response;
    @Autowired
    private RecommendService recommendaService;

    /**
     * 推荐作品
     * @return
     */
    @PostMapping("/watch")
    public String works(){
        //log.warn("请求:{}",request);
        return recommendaService.getRecommendations(request);
    }
}
