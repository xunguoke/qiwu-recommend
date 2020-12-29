package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.BotConfig;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.pojo.connectorPojo.WorkInformation;
import ai.qiwu.com.cn.service.handleService.WatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 手表推荐返回数据
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
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);


        //获取返回信息
        String text = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";

        //创建一个集合用于存储排序后的游戏名和编号
        HashMap<String, String> game = new HashMap<>();
        //创建以个map集合用于存储免费游戏名和编号
        HashMap<String, String> freeGameNumber = new HashMap<>();
        //创建以个map集合用于存储收费游戏名和编号
        HashMap<String, String> paidGameNumber = new HashMap<>();
        //创建一个map集合用于免费游戏
        HashMap<String, Double> gameFree = new HashMap<>();
        //创建以个map集合用于收费游戏
        HashMap<String, Double> gameCharges = new HashMap<>();
        //获取works
        List<WorksPojo> works = dataResponse.getWorks();

        //判断是否有作品
        if (works.size() <= 0) {
            String recommendText = "暂无作品！";
            String recommendName = "暂无作品！";
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

        for (WorksPojo work : works) {
            //获取是否收费信息
            List<String> synopsis = work.getLabels();
            for (String s : synopsis) {
                if (s.equals("New") || s.equals("免费")) {
                    //获取游戏名
                    String gameName = work.getName();
                    //获取游戏分数
                    Double fraction = work.getScore();
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
                    //存入免费游戏编号集合
                    freeGameNumber.put(gameName, botAccount);
                    //存入免费游戏集合
                    gameFree.put(gameName, fraction);
                } else if (s.equals("VIP") || s.equals("付费")) {
                    //获取游戏名
                    String gameName = work.getName();
                    //获取游戏分数
                    Double fraction = work.getScore();
                    //获取游戏编号
                    String botAccount = work.getBotAccount();
                    //存入收费游戏编号集合
                    paidGameNumber.put(gameName, botAccount);
                    //存入收费游戏集合
                    gameCharges.put(gameName, fraction);
                }
            }

        }

        //对免费游戏集合按分数进行降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameFree.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        //对收费游戏集合按分数进行降序排序
        List<Map.Entry<String, Double>> list2 = new ArrayList<Map.Entry<String, Double>>(gameCharges.entrySet());
        Collections.sort(list2, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        log.warn("list:{}", list);

        //判断集合长度
        if (list.size() > list2.size()) {
            for (int i = 0; i < list.size(); i++) {

                //判断是否最后
                if (i == list.size() - 1) {
                    //获取免费游戏名
                    String freeName = list.get(i).getKey();
                    //获取免费游戏名编号
                    String number = freeGameNumber.get(freeName);
                    text += number + "+" + freeName;

                    titleList.add(freeName);

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
                    game.put(freeName, number);
                    String recommendText = "☛推荐" + text + "☚";
                    String recommendName = "为您推荐以上作品：" + titleText + "你可以说：打开" + titleList.get(0);
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }

                //获取免费游戏名
                String freeName = list.get(i).getKey();
                //获取免费游戏名编号
                String number = freeGameNumber.get(freeName);
                text += number + "+" + freeName + ",";
                titleList.add(freeName);
                game.put(freeName, number);
                if (list2.size() > i) {
                    //获取收费游戏名
                    String freeGameName2 = list2.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text += number2 + "+" + freeGameName2 + ",";
                    titleList.add(freeGameName2);
                    game.put(freeGameName2, number2);
                }
            }
        } else {
            for (int i = 0; i < list2.size(); i++) {


                if (list.size() >= i) {
                    //获取免费游戏名
                    String freeName = list.get(i).getKey();
                    //获取免费游戏名编号
                    String number = freeGameNumber.get(freeName);
                    text += number + "+" + freeName + ",";
                    titleList.add(freeName);
                    game.put(freeName, number);
                }
                if (i == list2.size() - 1) {
                    //获取收费游戏名
                    String freeGameName2 = list.get(i).getKey();
                    //获取收费游戏名编号
                    String number2 = paidGameNumber.get(freeGameName2);
                    text += number2 + "+" + freeGameName2;

                    titleList.add(freeGameName2);

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
                    game.put(freeGameName2, number2);
                    String recommendText = "☛推荐" + text + "☚";
                    String recommendName = "为您推荐以上作品：" + titleText + "你可以说：打开" + titleList.get(0);
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }
                //获取收费游戏名
                String freeGameName2 = list2.get(i).getKey();
                //获取收费游戏名编号
                String number2 = paidGameNumber.get(freeGameName2);
                text += number2 + "+" + freeGameName2 + ",";

                titleList.add(freeGameName2);
                game.put(freeGameName2, number2);
            }
        }

        return null;
    }


    /**
     * 手表推荐之类型推荐
     *
     * @return
     */
    public static String typeRecommendation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取渠道id
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        List<WorksPojo> worksList = new ArrayList<WorksPojo>();

        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        String title = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();

        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();

        //调用接口获取设备中的数据
        List<BotConfig> botConfig = TypeRecommendation.getBotConfig();
        //判断是否有作品
        if (works.size() <= 0) {
            String recommendText = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
            String recommendName = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }

        //循环渠道设备
        for (BotConfig config : botConfig) {
            //获取渠道id
            String recommendBotAccount = config.getRecommendBotAccount();
            //判断渠道id
            if (recommendBotAccount.equals(channelId)) {
                //获取禁用标签
                String labelBlacklist = config.getLabelBlacklist();
                //去除空格
                String replace = labelBlacklist.replace(" ", "");
                //根据中文或英文逗号进行分割
                String regex = ",|，";
                String[] blacklist = replace.split(regex);
                List<String> asList = Arrays.asList(blacklist);
                for (String s : asList) {
                    if (s.equals(semantics)) {
                        String recommendText = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                        String recommendName = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    }
                }
            }
        }

        //循环所有作品，
        for (WorksPojo work : works) {
            //获取类型列表
            List<String> labels = work.getLabels();
            for (String label : labels) {
                if (label.equals(semantics)) {
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
                    //如果该作品属于该类型
                    worksList.add(work);
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
log.warn("list:{}",list);
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
                log.warn("准备返回数据");
                String recommendText = "☛推荐" + text + "☚";
                String recommendName = "为您推荐以上" + semantics + "类型作品：" + titleText + "你可以说：打开" + titleList.get(0);
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
     * 手表推荐之最新推荐
     * @return
     */
    public static String latestCreation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);


        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        String title = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏上线时间
        HashMap<String, String> gameLaunchTime = new HashMap<>();

        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();

        //判断是否有作品
        if (works.size() <= 0) {
            String recommendText = "暂无作品！";
            String recommendName = "暂无作品！";
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
        log.warn("list:{}", list);
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
     * 手表推荐之类似作品推荐
     * @return
     */
    public static String similarWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取语义
        String semantics = intent.getWorks();
        List<WorksPojo> worksList=new ArrayList<>();

        //用于存储临时数据
        List<WorkInformation> listWork = new ArrayList<WorkInformation>();
        //用于存储用户说的作品类型列表
        List<String> typeList = null;
        //定义一个String类型的变量用于存储筛选的游戏
        String text = "";
        String title = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";

        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();

        //获取作品类型列表
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName1 = work.getName();
            //判断游戏名是否和语义相同
            if (gameName1.equals(semantics)) {
                //获取类型列表
                typeList = work.getLabels();
            }
        }

        //获取相似作品
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
            log.warn("gameName2:{}", gameName);
            if (!gameName.equals(semantics)) {
                worksList.add(work);
                //获取游戏分数
                Double fraction = work.getScore();
                log.warn("fraction:{}", fraction);
                //获取游戏编号
                String botAccount = work.getBotAccount();
                log.warn("botAccount:{}", botAccount);
                //获取类型列表
                List<String> labels = work.getLabels();
                labels.retainAll(typeList);
                //判断有相同标签才存入集合
                int size = labels.size();
                log.warn("size:{}", size);
                log.warn("labels:{}", labels);
                if (size > 0) {
                    //创建对象传入参数
                    WorkInformation information = new WorkInformation(gameName, botAccount, fraction, size);
                    listWork.add(information);
                }

            }
        }
        if (listWork.size() <= 0) {
            String recommendText = "对不起，暂时没有和《" + semantics + "》相似的作品";
            String recommendName = "对不起，暂时没有和《" + semantics + "》相似的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }

        //清空上一轮推荐的作品缓存
        while (redisTemplate.opsForList().size("worksList")>0){
            redisTemplate.opsForList().leftPop("worksList");
        }
        //将作品信息添加到缓存
        redisTemplate.opsForList().rightPushAll("worksList",worksList);
        //设置过期时间
        redisTemplate.expire("worksList",3, TimeUnit.MINUTES);

        //循环集合按照Size倒序排序，size相同时按照评分倒序
        Collections.sort(listWork, new Comparator<WorkInformation>() {
            @Override
            public int compare(WorkInformation o1, WorkInformation o2) {
                Integer s1 = o1.getSize();
                Integer s2 = o2.getSize();

                int temp = s2.compareTo(s1);

                if (temp != 0) {
                    return temp;
                }

                double m1 = o1.getFraction();
                double m2 = o2.getFraction();

                BigDecimal data1 = new BigDecimal(m1);
                BigDecimal data2 = new BigDecimal(m2);

                return data2.compareTo(data1);
            }
        });
        log.warn("listWork:{}", listWork);
        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < listWork.size(); i++) {
            //判断是否是最后
            if (i == listWork.size() - 1) {
                //获取游戏名
                String gameName2 = listWork.get(i).getGameName();
                //获取收费游戏名编号
                String number2 = listWork.get(i).getBotAccount();
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
                String recommendName = "为您找到和《" + semantics + "》类似的作品：" + titleText + "快对我说：打开" + titleList.get(0);
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }

            //获取游戏名
            String gameName2 = listWork.get(i).getGameName();
            //获取收费游戏名编号
            String number2 = listWork.get(i).getBotAccount();
            text += number2 + "+" + gameName2 + ",";
            titleList.add(gameName2);
        }
        return null;
    }

    /**
     * 手表推荐之某作者的作品推荐
     * @return
     */
    public static String authorWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
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
            String authorName = work.getAuthorName();
            if (authorName.equals(semantics)) {
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



        //将游戏按照评分降序排序
        List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        if (list.size() <= 0) {
            String recommendText = "对不起，暂时没有" + semantics + "的作品";
            String recommendName = "对不起，暂时没有" + semantics + "的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
//清空上一轮推荐的作品缓存
        while (redisTemplate.opsForList().size("worksList")>0){
            redisTemplate.opsForList().leftPop("worksList");
        }
        //将作品信息添加到缓存
        redisTemplate.opsForList().rightPushAll("worksList",worksList);
        //设置过期时间
        redisTemplate.expire("worksList",3, TimeUnit.MINUTES);
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
                String recommendName = "为您找到" + semantics + "的作品：" + titleText + "快对我说：打开" + titleList.get(0);
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
     * 手表推荐之作品类型查询
     * @return
     */
    public static String typeOfWork(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //请求推荐作品接口，返回所有作品
        Map map = TypeRecommendation.getWorks();
        //查询数据库中渠道id相同的
        List<Watch> watches = TypeRecommendation.channelJudgment(intent, watchService);
        //获取交集
        DataResponse dataResponse = ExtractUtils.intersectionWorks(watches, map);
        //获取语义
        String semantics = intent.getWorks();

        //定义一个String类型的变量用于存储筛选的游戏
        String title = "";
        List<String> jy = new ArrayList<>();
        jy.add("New");
        jy.add("VIP");
        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();

        //调用接口获取设备中的数据
        List<BotConfig> botConfig = TypeRecommendation.getBotConfig();

        //循环所有作品
        for (WorksPojo work : works) {
            //获取游戏名
            String gameName = work.getName();
            //获取作品类型
            List<String> labels = work.getLabels();
            labels.removeAll(jy);
            //判断是否是用户所说的作品
            if (gameName.equals(semantics)) {
                //循环所有设备
                for (BotConfig config : botConfig) {
                    //获取渠道id
                    String recommendBotAccount = config.getRecommendBotAccount();
                    //获取作品渠道id
                    String botAccount = work.getBotAccount();
                    //判断作品渠道id是否等于设备渠道id
                    if (botAccount.equals(recommendBotAccount)) {
                        //获取禁用标签
                        String labelBlacklist = config.getLabelBlacklist();
                        log.warn("labelBlacklist:{}",labelBlacklist);
                        //去除空格
                        String replace = labelBlacklist.replace(" ", "");
                        //根据中文或英文逗号进行分割
                        String regex = ",|，";
                        String[] blacklist = replace.split(regex);
                        List<String> asList = Arrays.asList(blacklist);
                        labels.removeAll(asList);
                        if (labels.size() > 0) {
                            for (int x = 0; x < labels.size(); x++) {
                                if (x == labels.size()-1) {
                                    title += labels.get(x);
                                }else {
                                    title += labels.get(x) + ",";
                                }
                            }
                            String recommendText = "";
                            String recommendName = "清新传没有作品类型哦";
                            return TypeRecommendation.packageResult(recommendName, recommendText);
                        }
                    }
                }
                if (labels.size() > 0) {
                    for (int x = 0; x < labels.size(); x++) {
                        if (x == labels.size()-1) {
                            title += labels.get(x);
                        }else {
                            title += labels.get(x) + "、";
                        }

                    }
                }
                String recommendText = "";
                String recommendName = gameName + "的类型是：" + title;
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }


        }

        String recommendText = "";
        String recommendName = "没有清新传这个作品";
        return TypeRecommendation.packageResult(recommendName, recommendText);
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
