package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
            return IntentionUtils.recommenda(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之类型推荐")){
            log.warn("到这里");
            return IntentionUtils.typeRecommendation(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之最新推荐")){
            return IntentionUtils.latestCreation(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之类似作品推荐")){
            return IntentionUtils.similarWorks(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之某作者的作品推荐")){
            return IntentionUtils.authorWorks(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之作品类型查询")){
            return IntentionUtils.typeOfWork(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之作者查询")){
            return IntentionUtils.authorQuery(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之作品编号查询")){
            return IntentionUtils.workNumber(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之人群推荐")){
            return IntentionUtils.crowdRecommendation(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之收藏最多的作品")){
            return IntentionUtils.mostFavorites(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之作品简介查询")){
            return IntentionUtils.introduction(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之系列推荐")) {
            return IntentionUtils.seriesRecommendation(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之类型")){
                return IntentionUtils.type(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之系列查询")){
            return IntentionUtils.seriesQuery(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之作者推荐")){
            return IntentionUtils.recommendedWorks(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之历史记录类型查询")){
            return IntentionTool.historyTypeQuery(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之历史记录时间段查询")){
            return IntentionTool.timePeriodQuery(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之类型推荐+联立查询意图")){
            return IntentionTool.typeCombination(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之某作者的作品推荐+联立查询意图")){
            return IntentionTool.authorJoint(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之最新推荐+联立查询意图")){
            return IntentionTool.theLatestJoint(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之时间段最新推荐")){
            return IntentionTool.latestTime(intent, watchService,redisTemplate);
        }else if(intention.equals("手表推荐之多类型或者推荐")){
            return IntentionTool.orType(intent, watchService,redisTemplate);
        }else{
            log.warn("没有查询到合适的意图");
            return null;
        }
    }


    /**
     * 解析语义
     * @param semantics 用户信息关键词
     * @return
     */
    public static List<String> getDate(String semantics) {
        log.warn("时间");
        //判断字符串式否是数字
        boolean b = StringUtils.HasDigit(semantics);
        if(b==true){
            log.warn("包含数字");
            //如果包含数字,判断是否包含指定的关键字
            if(semantics.contains("最近")&&semantics.contains("天")){
                //获取数字
                int numbers = Integer.parseInt(StringUtils.getNumbers(semantics));
                return DateUtil.dayStart(numbers);

            }else if(semantics.contains("最近")&&semantics.contains("周")){
                //获取数字
                int numbers = Integer.parseInt(StringUtils.getNumbers(semantics));
                return DateUtil.weekStart(numbers);
            }else if(semantics.contains("最近")&&semantics.contains("月")){
                //获取数字
                int numbers = Integer.parseInt(StringUtils.getNumbers(semantics));
                return DateUtil.monthStart(numbers);
            }else if(semantics.contains("最近")&&semantics.contains("年")){
                //获取数字
                int numbers = Integer.parseInt(StringUtils.getNumbers(semantics));
                return DateUtil.yearStart(numbers);
            }else if(semantics.contains("月")){
                //获取数字
                int numbers = Integer.parseInt(StringUtils.getNumbers(semantics));
                return DateUtil.someMonth(numbers);
            }else{
                return null;
            }
        }else if(semantics.equals("今天")){
            return DateUtil.getToday();
        }else if(semantics.equals("昨天")){
            return DateUtil.getYesterday();
        }else if(semantics.equals("前天")){
            return DateUtil.getDayBeforeYesterday();
        }else if(semantics.equals("这周")){
            return DateUtil.getCurrentWeek();
        }else if(semantics.equals("上周")){
            log.warn("上周:{}");
            return DateUtil.getLastWeek();
        }else if(semantics.equals("这个月")){
            return DateUtil.getCurrentMonth();
        }else if(semantics.equals("上个月")){
            return DateUtil.getLastMonth();
        }else{
            return null;
        }
    }
}
