package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.BotConfig;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.ReturnedMessages;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.pojo.connectorPojo.TemporaryWorks;
import ai.qiwu.com.cn.pojo.connectorPojo.WorkInformation;
import ai.qiwu.com.cn.service.handleService.WatchService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 手表推荐已做部分
 * @author hjd
 */
@Slf4j
@Service
public class IntentionUtils {
    @Autowired
    RedisTemplate redisTemplate;
    /**
     * 手表推荐之推荐
     * @return
     */
    public static String recommenda(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //判断是否有作品
        if(dataResponse.getWorks().size()<=0){
            String workInformation = "暂无作品";
            String listOfWorks = "暂无作品";
            //将结果信息封装后返回
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }else{
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, dataResponse.getWorks());
            //将作品按照分数排序，且免费收费作品交替出现，返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.fractionalCharge(dataResponse);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您推荐以上作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之类型推荐
     *
     * @return
     */
    public static String typeRecommendation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //获取语义
        String semantics = intent.getWorks();
        //从接口中获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //筛选不含有禁用标签的作品
        DataResponse dataResponses = FilterWorksUtils.nonProhibitedWorks(dataResponse, semantics, strings);
        //获取筛选后的最终作品
        List<WorksPojo> worksPoJos = dataResponses.getWorks();
        if (worksPoJos.size()>0){
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //将作品按照分数排序,返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.scoreSort(dataResponses.getWorks());
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您推荐以上作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }else{
            String workInformation = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
            String listOfWorks = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }


    /**
     * 手表推荐之最新推荐
     * @return
     */
    public static String latestCreation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        if (dataResponse.getWorks().size()>0){
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, dataResponse.getWorks());
            //将作品按照时间排序,返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.timeOrder(dataResponse);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您推荐以上作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }else{
            String workInformation = "暂无作品";
            String listOfWorks = "暂无作品";
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }


    /**
     * 手表推荐之类似作品推荐
     * @return
     */
    public static String similarWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //获取语义
        String semantics = intent.getWorks();
        //从接口中获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //筛选与意图作品标签相同的作品
        TemporaryWorks temporaryWorks = FilterWorksUtils.scoreLabel(dataResponse, semantics, strings);
        if(temporaryWorks.getWorkInformations().size()>0){
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, temporaryWorks.getWorksPojos());
            //将作品按照时间,标签相似数量排序,返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.timeStamp(temporaryWorks);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您找到和《" + semantics + "》类似的作品：" + work + "快对我说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }else{
            String workInformation = "对不起，暂时没有和《" + semantics + "》相似的作品";
            String listOfWorks = "对不起，暂时没有和《" + semantics + "》相似的作品";
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }

    /**
     * 手表推荐之某作者的作品推荐
     * @return
     */
    public static String authorWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //筛选指定作者的作品
        List<WorksPojo> worksPoJos = FilterWorksUtils.authorWorks(dataResponse, semantics);
        if (worksPoJos.size()>0){
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //将作品按照时间分数排序,返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.scoreSort(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您找到" + semantics + "的作品：" + work + "快对我说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }else{
            String workInformation ="对不起，暂时没有" + semantics + "的作品";
            String listOfWorks = "对不起，暂时没有" + semantics + "的作品";
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }

    /**
     * 手表推荐之作品类型查询
     * @return
     */
    public static String typeOfWork(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //从接口中获取禁用标签
        List<String> strings = TypeRecommendation.disableLabel(channelId);
        //筛选指定作品的类型
        String type = FilterWorksUtils.designatedWorks(dataResponse, semantics, strings);
        if (type == "" || type == null) {
            String workInformation = "清新传没有作品类型哦";
            String listOfWorks = "清新传没有作品类型哦";
            return ResultUtils.packageResult(workInformation, listOfWorks);
        } else {
            String workInformation = type;
            String listOfWorks = "";
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }


    /**
     * 手表推荐之作者查询
     * @return
     */
    public static String authorQuery(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取语义
        String semantics = intent.getWorks();

        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        String title = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
            if (gameName.equals(semantics)) {
                //获取作者名字
                String authorName = work.getAuthorName();
                if (authorName == "" || authorName == null) {
                    String recommendText = "";
                    String recommendName = "对不起，暂时没有" + semantics + "的作者信息";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }
                String recommendText = "";
                String recommendName = gameName + "的作者是：" + authorName;
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }
        }
        String recommendText = semantics + "";
        String recommendName = semantics + "没有" + semantics + "这个作品";
        return TypeRecommendation.packageResult(recommendName, recommendText);
    }

    /**
     * 手表推荐之作品编号查询
     * @return
     */
    public static String workNumber(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取语义
        String semantics = intent.getWorks();
        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
            if (gameName.equals(semantics)) {
                String botAccount = work.getBotAccount();
                if (botAccount.equals("") || botAccount == null) {
                    String recommendText = "";
                    String recommendName = "无作品编号";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }
                String recommendText = "";
                String recommendName = botAccount;
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }
        }
        return null;
    }

    /**
     * 手表推荐之人群推荐
     * @return
     */
    public static String crowdRecommendation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取语义
        String semantics = intent.getWorks();
        List<WorksPojo> worksList =new ArrayList<>();
        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        String title = "";
        String name = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();

        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取作者名字
            List<String> suitCrowds = work.getSuitCrowds();
            for (int i = 0; i < suitCrowds.size(); i++) {
                name = suitCrowds.get(i);
                if (name.equals(semantics)) {
                    worksList.add(work);

                    //获取游戏名
                    String gameName = work.getName();
                    log.warn("gameName:{}", gameName);
                    //获取游戏分数
                    Double fraction = work.getScore();
                    log.warn("fraction:{}", fraction);
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
                    log.warn("botAccount:{}", botAccount);
                    //存入游戏编号集合
                    gameNumber.put(gameName, botAccount);
                    //存入游戏评分集合
                    gameRating.put(gameName, fraction);
                }
            }

        }

        //清空上一轮推荐的作品缓存
        while (redisTemplate.opsForList().size("worksList")>0){
            redisTemplate.opsForList().leftPop("worksList");
        }
        //将作品信息添加到缓存
        redisTemplate.opsForList().rightPushAll("worksList",worksList);
        //设置过期时间
        redisTemplate.expire("worksList",3, TimeUnit.MINUTES);

        //将游戏按照评分降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        log.warn("list:{}", list);
        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < list.size(); i++) {
            //判断是否是最后
            if (i == list.size() - 1) {
                //获取游戏名
                String gameName2 = list.get(i).getKey();
                //获取游戏名编号
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
                String recommendName = "为您推荐以上适合" + name + "作品：" + titleText + "你可以说：打开" + titleList.get(0) + "作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }

            //获取游戏名
            String gameName2 = list.get(i).getKey();
            //获取收费游戏名编号
            String number2 = gameNumber.get(gameName2);
            text += number2 + "+" + gameName2 + ",";

            titleList.add(gameName2);

        }
        String recommendText = "对不起，暂时没有适合" + semantics + "的作品";
        String recommendName = "对不起，暂时没有适合" + semantics + "的作品";
        return TypeRecommendation.packageResult(recommendName, recommendText);
    }

    /**
     * 手表推荐之收藏最多的作品
     *
     * @return
     */
    public static String mostFavorites(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);

        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        String title = "";
        String name = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏收藏人数
        HashMap<String, Integer> gameRating = new HashMap<>();

        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();
        if (works.size() <= 0) {
            String recommendText = "暂无作品";
            String recommendName = "暂无作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
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
            //获取作品收藏人数
            int plotCount = work.getPlotCount();
            //获取游戏名
            String gameName = work.getName();
            log.warn("gameName:{}", gameName);
            //获取游戏编号
            String botAccount = work.getBotAccount();
            log.warn("botAccount:{}", botAccount);
            //存入游戏编号集合
            gameNumber.put(gameName, botAccount);
            //存入游戏评分集合
            gameRating.put(gameName, plotCount);

        }


        //将游戏按照评分降序排序
        List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        log.warn("list:{}", list);
        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < list.size(); i++) {
            //判断是否是最后
            if (i == list.size()-1) {
                //获取游戏名
                String gameName2 = list.get(i).getKey();
                //获取游戏名编号
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
                String recommendName = "为您推荐以上收藏最多的作品：" + titleText + "你可以说：打开" + titleList.get(0) + "作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }

            //获取游戏名
            String gameName2 = list.get(i).getKey();
            //获取收费游戏名编号
            String number2 = gameNumber.get(gameName2);
            text += number2 + "+" + gameName2 + ",";
            titleList.add(gameName2);

        }
        return null;
    }

    /**
     * 手表推荐值作品简介查询
     * @return
     */
    public static String introduction(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取语义
        String semantics = intent.getWorks();
        //获取works将map转对象
        log.warn("打卡");
        List<WorksPojo> works = dataResponse.getWorks();
        if (works.size() <= 0) {
            String recommendText = "暂无作品";
            String recommendName = "没有" + semantics + "这个作品哦";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
        //循环所有作品，
        for (WorksPojo work : works) {
            String name = work.getName();
            if (name.equals(semantics)) {
                String intro = work.getIntro();
                if (intro.equals("") || intro == null) {
                    String recommendText = "";
                    String recommendName = "对不起，暂时没有" + name + "的作品简介";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }
                String recommendText = "";
                String recommendName = name + "的作品简介：" + intro;
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }

        }
        return null;
    }

    /**
     * 手表推荐之系列推荐
     * @return
     */
    public static String seriesRecommendation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取语义
        String semantics = intent.getWorks();
        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();
        List<WorksPojo> worksList=new ArrayList<>();

        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();
        if (works.size() <= 0) {
            String recommendText = "暂无作品";
            String recommendName = "没有" + semantics + "系列的作品，要不试试其他系列吧";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
        //循环所有作品，
        for (WorksPojo work : works) {
            //获取类型列表
            List<String> labels = work.getLabels();
            for (String label : labels) {
                if (label.equals(semantics)) {
                    //如果该作品属于该类型
                    worksList.add(work);
                    //获取游戏名
                    String gameName = work.getName();
                    log.warn("gameName:{}", gameName);
                    //获取游戏分数
                    Double fraction = work.getScore();
                    log.warn("fraction:{}", fraction);
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
                    log.warn("botAccount:{}", botAccount);
                    //存入游戏编号集合
                    gameNumber.put(gameName, botAccount);
                    //存入游戏评分集合
                    gameRating.put(gameName, fraction);
                }
            }
        }

        //清空上一轮推荐的作品缓存
        while (redisTemplate.opsForList().size("worksList")>0){
            redisTemplate.opsForList().leftPop("worksList");
        }
        //将作品信息添加到缓存
        redisTemplate.opsForList().rightPushAll("worksList",worksList);
        //设置过期时间
        redisTemplate.expire("worksList",3, TimeUnit.MINUTES);

        //将游戏按照评分降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
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
                String recommendName = "为您推荐以上作品：" + titleText + "你可以说：打开" + titleList.get(0);
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
     * 手表推荐之类型
     * @param redisTemplate
     * @return
     */
    public static String type(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //定义一个String类型的变量用于存储筛选的游戏
        List<String> typeList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        String titleText = "";
        List<String> jy = new ArrayList<>();
        jy.add("New");
        jy.add("VIP");

        //循环所有作品获取作品类型
        List<WorksPojo> works = dataResponse.getWorks();
        for (int a = 0; a < works.size(); a++) {
            List<String> multipleLabels = works.get(a).getLabels();
            //循环标签中的类型
            for (String multipleLabel : multipleLabels) {
                labels.add(multipleLabel);
            }
        }
        HashSet hashSet = new HashSet(labels);
        labels.clear();
        labels.addAll(hashSet);
        labels.removeAll(jy);
        log.warn("所有类型:{}", labels);
        //判断是否有类型
        if (labels.size() <= 0) {
            String recommendText = "";
            String recommendName = "对不起暂时没有作品类型";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }

        //根据uid获取用户已经查寻的类型
        List<String> range = redisTemplate.opsForList().range(uid + "labels", 0, -1);

        //调用接口获取设备中的数据
        List<BotConfig> botConfig = TypeRecommendation.getBotConfig();
        for (int z = 0; z < botConfig.size(); z++) {
            //获取渠道id
            String recommendBotAccount = botConfig.get(z).getRecommendBotAccount();
            //判断渠道id是否相同
            if (recommendBotAccount.equals(channelId)) {
                //获取禁用标签
                String labelBlacklist = botConfig.get(z).getLabelBlacklist();
                String replace = labelBlacklist.replace(" ", "");
                //根据中文或英文逗号进行分割
                String regex = ",|，";
                String[] blacklist = replace.split(regex);
                List<String> asList = Arrays.asList(blacklist);
                labels.removeAll(asList);
                log.warn("可以展示的类型:{}", labels);

                for (String label : labels) {
                    typeList.add(label);
                }
                log.warn("类型备份typeList:{}", typeList);
                //取差集
                labels.removeAll(range);
                log.warn("缓存中没有的类型:{}", labels);

                //判断取差集后的labels长度
                if (labels.size() <= 3) {
                    for (String s : labels) {
                        titleList.add(s);
                    }
                    log.warn("bbbbb:{}", titleList);
                    typeList.removeAll(labels);
                    log.warn("ccxcc:{}", typeList);
                    for (int j = 0; j < (3 - labels.size()); j++) {
                        titleList.add(typeList.get(j));
                    }
                    //删除Redis中所有的键
                    redisTemplate.delete(uid + "labels");
                    //将数据添加到redis
                    redisTemplate.opsForList().leftPushAll(uid + "labels", titleList);
                    for (int i = 0; i < titleList.size(); i++) {
                        if (i == titleList.size() - 1) {
                            titleText += titleList.get(i) + "，";
                            String recommendText = "";
                            String recommendName = "作品类型有：" + titleText + "你可以试试对我说：推荐" + titleList.get(0) + "类型的作品给我";
                            return TypeRecommendation.packageResult(recommendName, recommendText);
                        }
                        titleText += titleList.get(i) + "、";
                    }

                } else {
                    //循环遍历3个类型
                    for (int i = 0; i < 3; i++) {
                        range.add(labels.get(i));
                        titleList.add(labels.get(i));
                    }
                    for (int i = 0; i < titleList.size(); i++) {
                        if (i == titleList.size() - 1) {
                            titleText += titleList.get(i) + "，";
                            //删除Redis中所有的键
                            //redisTemplate.boundListOps(uid).remove(0);
                            redisTemplate.delete(uid + "labels");
                            //将数据添加到redis
                            redisTemplate.opsForList().leftPushAll(uid + "labels", range);
                            String recommendText = "";
                            String recommendName = "作品类型有：" + titleText + "你可以试试对我说：推荐" + titleList.get(0) + "类型的作品给我";
                            return TypeRecommendation.packageResult(recommendName, recommendText);
                        }
                        titleText += titleList.get(i) + "、";
                    }

                }

            }
        }
        //循环后没有相同返回所有类型

        for (String label : labels) {
            typeList.add(label);
        }
        log.warn("类型备份typeList:{}", typeList);
        //取差集
        labels.removeAll(range);
        log.warn("缓存中没有的类型:{}", labels);

        //判断取差集后的labels长度
        if (labels.size() <= 3) {
            for (String s : labels) {
                titleList.add(s);
            }
            log.warn("bbbbb:{}", titleList);
            typeList.removeAll(labels);
            log.warn("ccxcc:{}", typeList);
            for (int j = 0; j < (3 - labels.size()); j++) {
                titleList.add(typeList.get(j));
            }
            //删除Redis中所有的键
            redisTemplate.delete(uid + "labels");
            //将数据添加到redis
            redisTemplate.opsForList().leftPushAll(uid + "labels", titleList);
            for (String s : titleList) {
                titleText += s + ",";
            }
            String recommendText = "";
            String recommendName = "作品类型有：" + titleText + "你可以试试对我说：推荐" + titleList.get(0) + "类型的作品给我";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        } else {
            //循环遍历3个类型
            for (int i = 0; i < 3; i++) {
                range.add(labels.get(i));
                titleList.add(labels.get(i));
            }
            for (int i = 0; i < titleList.size(); i++) {
                if (i == titleList.size() - 1) {
                    titleText += titleList.get(i) + "，";
                    //删除Redis中所有的键
                    //redisTemplate.boundListOps(uid).remove(0);
                    redisTemplate.delete(uid + "labels");
                    //将数据添加到redis
                    redisTemplate.opsForList().leftPushAll(uid + "labels", range);
                    String recommendText = "";
                    String recommendName = "作品类型有：" + titleText + "你可以试试对我说：推荐" + titleList.get(0) + "类型的作品给我";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }
                titleText += titleList.get(i) + "、";
            }

        }
        return null;
    }



    /**
     * 手表推荐之系列查询
     * @param redisTemplate
     * @return
     */
    public static String seriesQuery(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //定义一个String类型的变量用于存储筛选的游戏
        List<String> typeList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        List<String> seriesName = new ArrayList<>();
        //List<String> range = new ArrayList<>();
        String titleText = "";

        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();
        for (WorksPojo work : works) {
            if(work.getSeriesName()!=null&&!work.getSeriesName().equals("")){
                seriesName.add(work.getSeriesName());
                log.warn("seriesName:{}",seriesName);
            }

        }
        HashSet hashSet=new HashSet(seriesName);
        seriesName.clear();
        seriesName.addAll(hashSet);
        log.warn("所有系列：{}",seriesName);

        //根据uid获取用户已经查寻的类型
        List<String> range = redisTemplate.opsForList().range(uid, 0, -1);
        log.warn("range:{}",range);

        if (seriesName.size()<=0){
            String recommendText ="";
            String recommendName="对不起，暂时没有作品系列";
            return TypeRecommendation.packageResult(recommendName,recommendText);
        }else{
            //取差集
            for (String s : seriesName) {
                typeList.add(s);
            }
            log.warn("aaaaa:{}",typeList);
            seriesName.removeAll(range);
            log.warn("ssss:{}",seriesName);
            //判断取差集后的labels长度
            if(seriesName.size()<=3){
                for (String s : seriesName) {
                    titleList.add(s);
                }
                log.warn("bbbbb:{}",titleList);
                typeList.removeAll(seriesName);
                log.warn("ccxcc:{}",typeList);
                for (int j=0;j<(3-seriesName.size());j++){
                    titleList.add(typeList.get(j));
                }
                //删除Redis中所有的键
                redisTemplate.delete(uid);
                //将数据添加到redis
                redisTemplate.opsForList().leftPushAll(uid,titleList);
                for (int i = 0; i < titleList.size(); i++) {
                    if(i==titleList.size()-1){
                        titleText+=titleList.get(i)+"，";
                        String recommendText ="";
                        String recommendName="目前作品系列有："+titleText+"你可以试试对我说：推荐"+titleList.get(0)+"系列给我";
                        return TypeRecommendation.packageResult(recommendName,recommendText);
                    }
                    titleText+=titleList.get(i)+"、";
                }

            }else{
                //循环遍历3个类型
                for(int i=0;i<3;i++){
                    range.add(seriesName.get(i));
                    titleList.add(seriesName.get(i));
                }
                for (int i = 0; i < titleList.size(); i++) {
                    if(i==titleList.size()-1){
                        titleText+=titleList.get(i)+"，";
                        //删除Redis中所有的键
                        //redisTemplate.boundListOps(uid).remove(0);
                        redisTemplate.delete(uid);
                        //将数据添加到redis
                        redisTemplate.opsForList().leftPushAll(uid,range);
                        String recommendText ="";
                        String recommendName="作品类型有："+titleText+"你可以试试对我说：推荐"+titleList.get(0)+"给我";
                        return TypeRecommendation.packageResult(recommendName,recommendText);
                    }
                    titleText+=titleList.get(i)+"、";
                }

            }
        }
        return null;
    }

    /**
     * 手表推荐之作者推荐
     * @return
     */
    public static String recommendedWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //定义一个String类型的变量用于存储筛选的游戏
        String text="";
        String title="";
        List<String> typeList = new ArrayList<>();
        List<String> titleList = new ArrayList<>();
        String titleText="";
        //获取作者作品数量集合
        Map<String, Integer> maps = new HashMap<>();

        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();

        //循环所有作品名去重
        for (WorksPojo work : works) {
            typeList.add(work.getAuthorName());
        }

        //判断是否有作者
        if(typeList.size()<=0){
            String recommendText ="";
            String recommendName="暂无作者";
            return TypeRecommendation.packageResult(recommendName,recommendText);
        }else {
            for (String s : typeList) {
                Integer count=maps.get(s);
                maps.put(s,(count==null)? 1:count+1);
            }
            //将游戏按照数量降序排序
            List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(maps.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
log.warn("list:{}",list);
            //循环获取作者名
            for(int i=0;i<3;i++){
                if(i==list.size()){
                    //获取作者名
                    titleText+=list.get(i).getKey()+"，";
                    String recommendText ="";
                    String recommendName="目前作者有："+titleText+"你可以说：推荐"+list.get(0).getKey()+"的作品给我";
                    return TypeRecommendation.packageResult(recommendName,recommendText);
                }else if(i==2){
                    titleText+=list.get(i).getKey()+"，";
                }else {
                    //获取作者名
                    titleText+=list.get(i).getKey()+"、";
                }

            }

            String recommendText ="";
            String recommendName="目前作者有："+titleText+"你可以说：推荐"+list.get(0).getKey()+"的作品给我";
            return TypeRecommendation.packageResult(recommendName,recommendText);
        }
    }
}
