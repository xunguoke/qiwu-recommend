package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 *手表推荐未做部分
 * @author hjd
 */
@Service
@Slf4j
public class IntentionTool {
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 手表推荐之历史记录类型查询
     * @param intent 用户请求信息
     * @param watchService 数据库才做类对象
     * @return
     */
    public static String historyTypeQuery(IntentionRequest intent, WatchService watchService) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        //获取交集
        DataResponse dataResponses = ExtractUtils.playedWorks(watches, maps,uid,watchService);


        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        String title = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏上线时间
        HashMap<String, String> gameLaunchTime = new HashMap<>();

        //获取作品
        List<WorksPojo> works = dataResponses.getWorks();
        log.warn("works:{}",works);

        if(works.size()<=0){
            String recommendText = "您还没有体验过科幻类型的作品";
            String recommendName = "您还没有体验过科幻类型的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }

        //获取禁用标签
        List<String> labelBlacklist = TypeRecommendation.disableLabel(channelId);

        for (String s : labelBlacklist) {
            if (s.equals(semantics)){
                String recommendText = "您还没有体验过科幻类型的作品";
                String recommendName = "您还没有体验过科幻类型的作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }
        }


        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
            //获取游戏上线时间
            String gmtApply = work.getGmtApply();
            //将时间转换成指定格式
            String timeOnline = CommonlyUtils.dealDateFormat(gmtApply);
            //获取游戏编号
            String botAccount = work.getBotAccount();
            //存入游戏编号集合
            gameNumber.put(gameName, botAccount);
            //存入游戏上线时间集合
            gameLaunchTime.put(gameName, timeOnline);

        }

        //将游戏上线时间降序排序
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(gameLaunchTime.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < list.size(); i++) {
            //判断是否是最后
            if (i == list.size() - 1) {
                //获取游戏名
                String gameName2 = list.get(i).getKey();
                //获取收费游戏名编号
                String number2 = gameNumber.get(gameName2);
                text += number2 + "+" + gameName2;
                titleList.add(gameName2);

                if (titleList.size() >= 3) {
                    for (int j = 0; j < 3; j++) {

                        if (j == 2) {
                            titleText += "《" + titleList.get(j) + "》，";
                        }else {
                            titleText += "《" + titleList.get(j) + "》、";
                        }
                    }
                } else {
                    for (int y = 0; y < titleList.size(); y++) {
                        if (y == titleList.size() - 1) {
                            titleText += "《" + titleList.get(y) + "》，";
                        }else {
                            titleText += "《" + titleList.get(y) + "》、";
                        }
                    }
                }
                String recommendText = "☛推荐" + text + "☚";
                String recommendName = "为您推荐以上最新的作品：" + titleText + "快对我说：打开" + titleList.get(0);
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }

            //获取游戏名
            String gameName2 = list.get(i).getKey();
            //获取游戏名编号
            String number2 = gameNumber.get(gameName2);
            text += number2 + "+" + gameName2 + ",";
            titleList.add(gameName2);

        }
        return null;
    }


    /**
     * 收表推荐之历史记录时间段查询
     * @param intent 用户请求信息
     * @param watchService 数据库才做类对象
     * @return
     */
    public static String timePeriodQuery(IntentionRequest intent, WatchService watchService) {
        return null;
    }
}
