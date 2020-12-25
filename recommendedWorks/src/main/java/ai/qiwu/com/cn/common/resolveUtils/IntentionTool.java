package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.ReturnedMessages;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

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
     * @param watchService 数据库类对象
     * @param redisTemplate
     * @return
     */
    public static String historyTypeQuery(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
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

        //清空上一轮推荐的作品缓存
        while (redisTemplate.opsForList().size("worksList")>0){
            redisTemplate.opsForList().leftPop("worksList");
        }
        //将作品信息添加到缓存
        redisTemplate.opsForList().rightPushAll("worksList",works);
        //设置过期时间
        redisTemplate.expire("worksList",3, TimeUnit.MINUTES);

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
     * @param watchService 数据库类对象
     * @param redisTemplate
     * @return
     */
    public static String timePeriodQuery(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //获取用户历史作品
        List<UserHistory> byUidOfDate = TypeRecommendation.findByUidOfDate(uid, watchService, semantics);
        //获取作品名，时间集合
        List<Map.Entry<String, Date>> workTime = ExtractUtils.workTime(byUidOfDate);
        //获取交集(此时已经按照时间降序排序)
        DataResponse dataResponses = ExtractUtils.workResult(watches, maps,workTime);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        //将作品存到缓存中去
        ExtractUtils.cacheSave(redisTemplate,works);
        if(works.size()>0) {
            //调用方法返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.listOfWorks(works);
            //封装返回结果信息
            return TypeRecommendation.packageResult(returnedMessages.getWorkInformation(),returnedMessages.getWorksList());
        }else{
            String recommendText = "您"+semantics+"没有体验过作品";
            String recommendName = "您"+semantics+"没有体验过作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }

    /**
     * 手表推荐之类型推荐+联立查询意图
     * @param intent 用户请求信息
     * @param watchService 数据库类对象
     * @param redisTemplate 操作缓存类对象
     * @return
     */
    public static String typeCombination(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取缓存中的所有作品
        List<WorksPojo> works = redisTemplate.opsForList().range("worksList", 0, -1);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //判断禁用标签是否包含意图
        if(strings.contains(semantics)){
            //包含
            String recommendText = "列表中没有"+semantics+"类型的作品";
            String recommendName = "列表中没有"+semantics+"类型的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else{
            //不包含，根据类型筛选出作品
            List<WorksPojo> worksPoJos = ExtractUtils.typeSelection(works, semantics);
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,worksPoJos);
            //判断作品列表是否为空
            if(worksPoJos==null){
                String recommendText = "列表中没有"+semantics+"类型的作品";
                String recommendName = "列表中没有"+semantics+"类型的作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }else {
                //跟具作品分数进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation="列表中" + semantics + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
            }
        }
    }

    /**
     * 手表推荐之某作者的作品推荐+联立查询意图
     * @param intent 用户请求信息
     * @param watchService 数据库类对象
     * @param redisTemplate 操作缓存类对象
     * @return
     */
    public static String authorJoint(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取缓存中的所有作品
        List<WorksPojo> works = redisTemplate.opsForList().range("worksList", 0, -1);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //筛选不包含渠道禁用标签的作品
        List<WorksPojo> worksPoJos = ExtractUtils.filterDisabled(works, strings);
        //判断作品列表是否为空
        if(worksPoJos==null){
            String recommendText = "暂无"+semantics+"类型的作品";
            String recommendName = "暂无"+semantics+"类型的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else {
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,worksPoJos);
            //跟具作品分数进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation="列表中" + semantics + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之最新推荐+联立查询意图
     * @param intent 用户请求信息
     * @param watchService 数据库类对象
     * @param redisTemplate 操作缓存类对象
     * @return
     */
    public static String theLatestJoint(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取缓存中的所有作品
        List<WorksPojo> works = redisTemplate.opsForList().range("worksList", 0, -1);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //判断作品列表是否为空
        if(works==null){
            String recommendText = "列表中没有作品";
            String recommendName = "列表中没有作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else {
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,works);
            //跟具作品时间进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.timeOrder(works);
            String work = returnedMessages.getWorkInformation();
            String workInformation="列表中最新上线的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之时间段最新推荐
     * @param intent 用户请求信息
     * @param watchService 数据库类对象
     * @param redisTemplate 操作缓存类对象
     * @return
     */
    public static String latestTime(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, maps);
        //获取指定时间范围的作品
        DataResponse dataResponses = ExtractUtils.latestTime(dataResponse, semantics);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        //将作品存到缓存中去
        ExtractUtils.cacheSave(redisTemplate,works);
        if(works.size()>0) {
            //根据时间排序获取作品列表以及返回信息
            ReturnedMessages returnedMessages = ExtractUtils.timeOrder(works);
            String work = returnedMessages.getWorkInformation();
            String workInformation=semantics + "新上线的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
        }else{
            String recommendText = "这个月没有上线新的作品";
            String recommendName = "这个月没有上线新的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }


    /**
     * 手表推荐之多类型或者推荐
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String orType(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义
        String semantics = intent.getWorks();
        //解析语义
        String[] split = semantics.split("[+]");
        //转list
        List<String> asList = Arrays.asList(split);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponses = ExtractUtils.channelWorks(watches, maps);
        //获取交集作品
        List<WorksPojo> works1 = dataResponses.getWorks();
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //筛选不包含渠道禁用标签的作品
        List<WorksPojo> worksPoJos = ExtractUtils.multiConditionScreening(works1, strings,semantics);
        //将作品存到缓存中去
        ExtractUtils.cacheSave(redisTemplate,worksPoJos);
        if(worksPoJos.size()>0) {
            //调用方法返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
            //封装返回结果信息
            return TypeRecommendation.packageResult(returnedMessages.getWorkInformation(),returnedMessages.getWorksList());
        }else{
            String recommendText = "暂无"+asList.get(0)+"或"+asList.get(1)+"类型的作品，要不试试其他类型吧";
            String recommendName = "暂无"+asList.get(0)+"或"+asList.get(1)+"类型的作品，要不试试其他类型吧";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }

    }

    /**
     * 手表推荐之多类型推荐
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String multipleTypes(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义
        String semantics = intent.getWorks();
        //解析语义
        String[] split = semantics.split("[+]");
        //转list
        List<String> asList = Arrays.asList(split);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponses = ExtractUtils.channelWorks(watches, maps);
        //获取交集作品
        List<WorksPojo> works1 = dataResponses.getWorks();
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //筛选不包含渠道禁用标签的作品且满足所有意图的作品
        List<WorksPojo> worksPoJos = ExtractUtils.allIntentions(works1, strings,semantics);
        //将作品存到缓存中去
        ExtractUtils.cacheSave(redisTemplate,worksPoJos);
        if(worksPoJos.size()>0) {
            //调用方法返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
            //封装返回结果信息
            return TypeRecommendation.packageResult(returnedMessages.getWorkInformation(),returnedMessages.getWorksList());
        }else{
            String recommendText = "暂无"+asList.get(0)+"或"+asList.get(1)+"类型的作品，要不试试其他类型吧";
            String recommendName = "暂无"+asList.get(0)+"或"+asList.get(1)+"类型的作品，要不试试其他类型吧";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }

    /**
     * 手表推荐之类型推荐+联立查询意图
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String typeIntent(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取缓存中的所有作品
        List<WorksPojo> works = redisTemplate.opsForList().range("worksList", 0, -1);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //获取所有满足意图的作品
        List<WorksPojo> worksPoJos = ExtractUtils.collectionAndPaymentScreening(works, strings, semantics);
        //判断作品列表是否为空
        if(worksPoJos==null){
            String recommendText = "列表中没有"+semantics+"的作品";
            String recommendName = "列表中没有"+semantics+"的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else {
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,worksPoJos);
            //跟具作品分数进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation="列表中"+semantics+"的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之某作者最新作品推荐
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String authorSLatest(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, maps);
        //获取指定作者的作品
        List<WorksPojo> worksPoJos = ExtractUtils.authorWorks(dataResponse, semantics);
        //将作品存到缓存中去
        ExtractUtils.cacheSave(redisTemplate,worksPoJos);
        if(worksPoJos.size()>0) {
            //根据时间排序获取作品列表以及返回信息
            ReturnedMessages returnedMessages = ExtractUtils.timeOrder(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation=semantics + "新上线的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
        }else{
            String recommendText = "暂无"+semantics+"的作品";
            String recommendName = "暂无"+semantics+"的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }

    /**
     * 手表推荐之某类型最新作品推荐
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String latestType(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义
        String semantics = intent.getWorks();
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, maps);
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //判断禁用标签是否包含意图
        if(strings.contains(semantics)){
            //包含
            String recommendText = "暂无"+semantics+"类型的作品，要不试试其他类型吧";
            String recommendName = "暂无"+semantics+"类型的作品，要不试试其他类型吧";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else{
            //不包含，根据类型筛选出作品
            List<WorksPojo> worksPoJos = ExtractUtils.typeSelection(works, semantics);
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,worksPoJos);
            //判断作品列表是否为空
            if(worksPoJos==null){
                String recommendText = "暂无"+semantics+"类型的作品，要不试试其他类型吧";
                String recommendName = "暂无"+semantics+"类型的作品，要不试试其他类型吧";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }else {
                //跟具作品分数进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = ExtractUtils.timeOrder(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation= semantics + "类型的新作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
            }
        }
    }

    /**
     * 手表推荐之判断作品是否付费
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String whetherToPay(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义
        String semantics = intent.getWorks();
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, maps);
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //获取指定作品
        WorksPojo worksPojo = ExtractUtils.designatedWorks(works, semantics);
        //判断是否有该作品
        if(worksPojo==null){
            String recommendText = "没有这个作品哦";
            String recommendName = "没有这个作品哦";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
        //获取作品类型
        List<String> labels = worksPojo.getLabels();
        //判断作品是否包含收付费信息
        if(labels.contains("免费")||labels.contains("New")||labels.contains("付费")||labels.contains("VIP")){
            //获取作品是否收费
            List<String> labelsList = ExtractUtils.chargeJudgment(labels, strings);
            //判断标签长度
            if(labelsList.size()>1){
                //判断禁用标签是否包含
                if (strings.contains(labelsList.get(0))&&strings.contains(labelsList.get(1))){
                    String recommendText = "暂无"+semantics+"的资费信息";
                    String recommendName = "暂无"+semantics+"的资费信息";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }else{
                    String recommendText = "";
                    String recommendName = semantics+"是"+labelsList.get(0)+"类型的作品";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }
            }else{
                //判断禁用标签是否包含
                if (strings.contains(labelsList.get(0))){
                    String recommendText = "暂无"+semantics+"的资费信息";
                    String recommendName = "暂无"+semantics+"的资费信息";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }else{
                    if(labelsList.get(0).equals("免费")||labelsList.get(0).equals("New")){
                        String recommendText = "";
                        String recommendName = semantics+"是免费类型的作品";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    }else{
                        String recommendText = "";
                        String recommendName = semantics+"是收费类型的作品";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    }
                }
            }
        }else{
            String recommendText = "暂无"+semantics+"的资费信息";
            String recommendName = "暂无"+semantics+"的资费信息";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }

    /**
     * 手表推荐之历史时间段和类型查询
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String historyType(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //先查询我上周玩了那些游戏
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义（时间）
        String semantics1 = intent.getHistoryTypeOne();
        //获取语义（类型）
        String semantics2 = intent.getHistoryTypeTwo();
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //获取用户历史作品
        List<UserHistory> byUidOfDate = TypeRecommendation.findByUidOfDate(uid, watchService, semantics1);
        //获取作品名，时间集合
        List<Map.Entry<String, Date>> workTime = ExtractUtils.workTime(byUidOfDate);
        //获取交集(此时已经按照时间降序排序)
        DataResponse dataResponses = ExtractUtils.workResult(watches, maps,workTime);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();

        //更具类型筛选游戏
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //判断禁用标签是否包含意图
        if(strings.contains(semantics2)){
            //包含
            String recommendText = "您"+semantics1+"没有体验过"+semantics2+"类型的作品";
            String recommendName = "您"+semantics1+"没有体验过"+semantics2+"类型的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else{
            //不包含，根据类型筛选出作品
            List<WorksPojo> worksPoJos = ExtractUtils.typeSelection(works, semantics2);
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,worksPoJos);
            //判断作品列表是否为空
            if(worksPoJos==null){
                String recommendText = "您"+semantics1+"没有体验过"+semantics2+"类型的作品";
                String recommendName = "您"+semantics1+"没有体验过"+semantics2+"类型的作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }else {
                //跟具作品时间进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = ExtractUtils.timeOrder(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation="您"+semantics1+"体验过以上"+semantics2+"类型的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
            }
        }
    }

    /**
     * 手表推荐之某作者某类型推荐
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String authorType(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //根据作者筛选作品
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义（作者）
        String semantics1 = intent.getHistoryTypeOne();
        //获取语义（类型）
        String semantics2 = intent.getHistoryTypeTwo();
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, maps);
        //获取指定作者的作品
        List<WorksPojo> worksPoJo = ExtractUtils.authorWorks(dataResponse, semantics1);

        //根据类型筛选作品
        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);

        //判断禁用标签是否包含意图
        if(strings.contains(semantics2)){
            //包含
            String recommendText = "暂无"+semantics1+"的"+semantics2+"类型作品";
            String recommendName = "暂无"+semantics1+"的"+semantics2+"类型作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else{
            //不包含，根据类型筛选出作品
            List<WorksPojo> worksPoJos = ExtractUtils.typeSelection(worksPoJo, semantics2);
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,worksPoJos);
            //判断作品列表是否为空
            if(worksPoJos==null){
                String recommendText = "暂无"+semantics1+"的"+semantics2+"类型作品";
                String recommendName = "暂无"+semantics1+"的"+semantics2+"类型作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }else {
                //跟具作品分数进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation= semantics1 + "的"+semantics2+"类型作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
            }
        }
    }

    /**
     * 手表推荐之某作者时间段最新作品推荐
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String authorSLatestWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义（时间）
        String semantics1 = intent.getHistoryTypeOne();
        //获取语义（作者）
        String semantics2 = intent.getHistoryTypeTwo();
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, maps);
        //获取指定时间范围的作品
        DataResponse dataResponses = ExtractUtils.latestTime(dataResponse, semantics1);

        //获取指定作者的作品
        List<WorksPojo> worksPoJos = ExtractUtils.authorWorks(dataResponses, semantics2);

        //将作品存到缓存中去
        ExtractUtils.cacheSave(redisTemplate,worksPoJos);
        //判断作品列表是否为空
        if(worksPoJos==null){
            String recommendText = semantics2+semantics1+"没有上线新的作品";
            String recommendName = semantics2+semantics1+"没有上线新的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else {
            //跟具作品分数进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation=semantics2+semantics1+"上线新的新作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之某类型时间段最新的作品推荐
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String typeLatest(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        DataResponse data=new DataResponse();
        //请求推荐作品接口，返回所有作品
        Map maps = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的作品
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取语义（类型）
        String semantics1 = intent.getHistoryTypeOne();
        //获取语义（时间）
        String semantics2 = intent.getHistoryTypeTwo();
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        log.warn("到达这里");
        //数据库渠道作品与所有作品接口的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, maps);
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();

        //获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //判断禁用标签是否包含意图
        if(strings.contains(semantics1)){
            //包含
            String recommendText = semantics2+"没有上线"+semantics1+"类型的作品";
            String recommendName = semantics2+"没有上线"+semantics1+"类型的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }else{
            //不包含，根据类型筛选出作品
            List<WorksPojo> worksPoJo = ExtractUtils.typeSelection(works, semantics1);
            data.setWorks(worksPoJo);
            data.setLabels(dataResponse.getLabels());
            //获取指定时间范围的作品
            DataResponse dataResponses = ExtractUtils.latestTime(data, semantics2);
            //获取所有作品
            List<WorksPojo> worksPoJos = dataResponses.getWorks();
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate,worksPoJos);
            //判断作品列表是否为空
            if(worksPoJos==null){
                String recommendText = semantics2+"没有上线"+semantics1+"类型的作品";
                String recommendName = semantics2+"没有上线"+semantics1+"类型的作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }else {
                //跟具作品分数进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = ExtractUtils.scoreScreening(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation=semantics2+"新上线的" + semantics1 + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation,returnedMessages.getWorksList());
            }
        }
    }

}

