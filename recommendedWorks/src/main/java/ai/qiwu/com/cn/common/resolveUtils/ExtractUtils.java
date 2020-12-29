package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.PublicData;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.ReturnedMessages;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.service.handleService.WatchService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 获取数据库和接口中交集作品
 * 该工具类为以后相同功能点扩展用
 * @author hjd
 */
@Service
@Slf4j
public class ExtractUtils {
    /**
     * 获取数据库渠道作品和所有作品接口中的交集作品
     * @param watches 数据库渠道表中的作品
     * @param map 所有作品接口中的作品
     * @return
     */
    public static DataResponse intersectionWorks(List<Watch> watches, Map map) {
        //定义两个List用于存储渠道作品名和接口作品名
        List<String> channels = new ArrayList<>();
        List<String> interfaceWorks=new ArrayList<>();
        List<WorksPojo> worksList = new ArrayList<>();
        PublicData publicData = new PublicData();
        //判断渠道中是否有作品
        if (watches.size() > 0) {
            //获取渠道中所有作品名
            for (Watch watch : watches) {
                channels.add(watch.getWorkname());
            }
            //获取接口中所有作品
            DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
            List<WorksPojo> works = dataResponse.getWorks();
            publicData.setLabels(dataResponse.getLabels());
            //循环遍历接口中的作品名
            for (WorksPojo work : works) {
                interfaceWorks.add(work.getName());
            }

            //获取交集获取作品名
            channels.retainAll(interfaceWorks);

            //循环遍历接口中的所有作品
            for (WorksPojo work : works) {
                String name = work.getName();
                for (String channel : channels) {
                    if(name.equals(channel)){
                        worksList.add(work);
                    }
                }
            }
            dataResponse.setWorks(worksList);

            //根据不同意图返回作品
            return dataResponse;
        }else{

            //获取接口中所有作品
            DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
            //从接口中筛选数据返回
            return dataResponse;
        }

    }


    /**
     * 数据库渠道作品与所有作品接口的交集
     * @param watches 数据库渠道表中的作品
     * @param map 所有作品接口中的作品
     * @return
     */
    public static DataResponse channelWorks(List<Watch> watches, Map map) {
        //定义两个List用于存储渠道作品名和接口作品名
        List<String> channels = new ArrayList<>();
        List<String> interfaceWorks=new ArrayList<>();

        List<WorksPojo> worksList = new ArrayList<>();
        PublicData publicData = new PublicData();

        //获取渠道中所有作品名
        for (Watch watch : watches) {
            channels.add(watch.getWorkname());
        }
        log.warn("channels:{}",channels);
        //获取接口中所有作品
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();
        publicData.setLabels(dataResponse.getLabels());
        //循环遍历接口中的作品名
        for (WorksPojo work : works) {
            interfaceWorks.add(work.getName());
        }

        //获取交集获取作品名
        channels.retainAll(interfaceWorks);

        //循环遍历接口中的所有作品
        for (WorksPojo work : works) {
            String name = work.getName();
            for (String channel : channels) {
                if(name.equals(channel)){
                    worksList.add(work);
                }
            }
        }
        dataResponse.setWorks(worksList);
        return dataResponse;
    }


    /**
     * 获取已玩作品和渠道作品以及数据库作品的交集
     * @param watches 数据库渠道表中的作品
     * @param map 所有作品接口中的作品
     * @return
     */
    public static DataResponse playedWorks(List<Watch> watches, Map map, String uid, WatchService watchService) {
        //查询用户玩过的作品
        List<UserHistory> byUid = TypeRecommendation.findByUid(uid, watchService);
        //获取已玩作品名
        List<String> channels = new ArrayList<>();
        //数据库中作品名
        List<String> interfaceWorks=new ArrayList<>();

        List<WorksPojo> worksList = new ArrayList<>();
        PublicData publicData = new PublicData();

        //获取已玩作品名
        for (UserHistory watch : byUid) {
            channels.add(watch.getWorkname());
        }
        //获取数据库作品和接口作品的交集
        DataResponse dataResponse = ExtractUtils.channelWorks(watches, map);
        List<WorksPojo> works = dataResponse.getWorks();
        publicData.setLabels(dataResponse.getLabels());
        //循环遍历接口中的作品名
        for (WorksPojo work : works) {
            interfaceWorks.add(work.getName());
        }
        log.warn("interfaceWorks:{}",interfaceWorks);
        log.warn("channels:{}",channels);
        //获取交集获取作品名
        channels.retainAll(interfaceWorks);

        //循环遍历接口中的所有作品
        for (WorksPojo work : works) {
            String name = work.getName();
            for (String channel : channels) {
                if(name.equals(channel)){
                    worksList.add(work);
                }
            }
        }
        dataResponse.setWorks(worksList);
        return dataResponse;
    }


    /**
     * 所有作品，渠道作品，用户历史作品三者的交集作品（返回的作品信息已经按照时间排序）
     * @param workTime 用户玩过的作品时间集合（已经降序排序）
     * @param watches 数据库渠道表中的作品
     * @param maps 所有作品接口中的作品
     * @param workTime
     * @return
     */
    public static DataResponse workResult(List<Watch> watches, Map maps, List<Map.Entry<String, Date>> workTime) {
        //定义两个List用于存储渠道作品名和接口作品名
        List<String> channels = new ArrayList<>();
        List<String> interfaceWorks=new ArrayList<>();
        List<String> historicalWorks=new ArrayList<>();
        List<WorksPojo> worksList = new ArrayList<>();
        List<Date> gameTime=new ArrayList<>();
        PublicData publicData = new PublicData();

        //todo 1.获取数据库渠道表中的作品
        for (Watch watch : watches) {
            channels.add(watch.getWorkname());
        }
        //todo 2.获取接口中的所有作品
        //获取接口中所有作品
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();
        publicData.setLabels(dataResponse.getLabels());
        //循环遍历接口中的作品名
        for (WorksPojo work : works) {
            interfaceWorks.add(work.getName());
        }
        //todo 3.获取用户玩过的所有作品
        for (int i = 0; i < workTime.size(); i++) {
            historicalWorks.add(workTime.get(i).getKey());
        }
        log.warn("historicalWorks:{}",historicalWorks);
        //todo 4.取交集
        channels.retainAll(interfaceWorks);
        historicalWorks.retainAll(channels);
        log.warn("historicalWorks:{}",historicalWorks);
        //todo 5.封装作品
        //循环遍历接口中的所有作品
        for (int j = 0; j < historicalWorks.size(); j++) {
            for (WorksPojo work : works) {
                String name = work.getName();
                if(name.equals(historicalWorks.get(j))){
                    worksList.add(work);
                }
            }
        }
        dataResponse.setWorks(worksList);

        //根据不同意图返回作品
        return dataResponse;
    }

    /**
     * 获取已玩作品的作品名时间集合
     * @param byUidOfDate 指定时间段内用户玩过的作品
     * @return
     */
    public static List<Map.Entry<String, Date>> workTime(List<UserHistory> byUidOfDate) {
        //定义一个map集合用于存储已玩作品名和最后一次玩的时间
        HashMap<String , Date> hashMap = new HashMap<>();
        for (UserHistory userHistory : byUidOfDate) {
            hashMap.put(userHistory.getWorkname(),userHistory.getGmtmodified());
        }
        //将集合按照时间降序排序
        List<Map.Entry<String, Date>> list = new ArrayList<Map.Entry<String, Date>>(hashMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Date>>() {
            @Override
            public int compare(Map.Entry<String, Date> o1, Map.Entry<String, Date> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        return list;
    }

    /**
     * 获取作品名以及作品编号的集合
     * @param dataResponses
     * @return
     */
    public static HashMap<String, String> workNumber(DataResponse dataResponses) {
        //定义一个map集合用于存储已玩作品名和最后一次玩的时间
        HashMap<String , String> hashMap = new HashMap<>();
        //获取作品
        List<WorksPojo> works = dataResponses.getWorks();
        for (WorksPojo work : works) {
            hashMap.put(work.getName(),work.getBotAccount());
        }
        return hashMap;
    }

    /**
     * 获取作品列表以及返回信息
     * @param works
     * @return
     */
    public static ReturnedMessages listOfWorks(List<WorksPojo> works) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //定义一个String类型的变量用于存储筛选的作品
        String text = "";
        List<String> titleList = new ArrayList<>();
        String titleText = "";
        //循环遍历所有作品
        for (int i = 0; i < works.size(); i++) {
            //判断是否是最后一个作品
            if(i==works.size()-1){
                text+=works.get(i).getName()+ "+" +works.get(i).getBotAccount()+",";
                //循环获取返回信息
                for (int j = 0; j < titleList.size(); j++) {
                    if(j==2){
                        titleText += "《" + titleList.get(j) + "》，";
                    }
                    titleText += "《" + titleList.get(j) + "》、";
                }
                //封装对象后返回
                messages.setWorksList(text);
                messages.setWorkInformation(titleText);
                return messages;
            }
            text+=works.get(i).getName()+ "+" +works.get(i).getBotAccount()+",";
            if(i<3) {
                titleList.add(works.get(i).getName());
            }
        }
        return null;
    }

    /**
     * 将需要返回的作品信息保存到缓存中去
     * @param redisTemplate 操作缓存对象
     * @param works 所有作品信息
     */
    public static void cacheSave(RedisTemplate redisTemplate, List<WorksPojo> works) {
        //清空上一轮推荐的作品缓存
        while (redisTemplate.opsForList().size("worksList")>0){
            redisTemplate.opsForList().leftPop("worksList");
        }
        //将作品信息添加到缓存
        redisTemplate.opsForList().rightPushAll("worksList",works);
        //设置过期时间
        redisTemplate.expire("worksList",3, TimeUnit.MINUTES);
    }

    /**
     * 根据类型筛选作品
     * @param works 作品信息
     * @param semantics 筛选条件
     * @return
     */
    public static List<WorksPojo> typeSelection(List<WorksPojo> works, String semantics) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //循环遍历所有作品
        for (WorksPojo work : works) {
            //获取作品类型列表
            List<String> labels = work.getLabels();
            //判断是否包含指定的类型
            if(labels.contains(semantics)){
                //包含
                worksList.add(work);
            }
        }
        return worksList;
    }

    /**
     * 根据分数排序获取作品列表以及返回信息
     * @param works 作品信息
     * @return
     */
    public static ReturnedMessages scoreScreening(List<WorksPojo> works) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //最后返回的作品列表
        String listWorks="";
        //定义一个String类型的变量用于存储筛选的游戏
        String listOfWorks = "";
        List<String> titleList = new ArrayList<>();
        String workInformation = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, Double> gameRating = new HashMap<>();
        //循环所有作品，
        for (WorksPojo work : works) {
            //获取作品名
            String gameName = work.getName();
            log.warn("gameName:{}", gameName);
            //获取作品分数
            Double fraction = work.getScore();
            log.warn("fraction:{}", fraction);
            //获取作品编号
            String botAccount = work.getBotAccount();
            log.warn("botAccount:{}", botAccount);
            //存入游戏编号集合
            gameNumber.put(gameName, botAccount);
            //存入游戏评分集合
            gameRating.put(gameName, fraction);
        }
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
            //获取作品名
            String workName = list.get(i).getKey();
            //获取作品编号
            String botAccount = gameNumber.get(workName);
            //判断是否是最后一个元素
            if(i==list.size()-1){
                //设置返回作品列表
                listOfWorks += botAccount + "+" + workName;
                //设置最后返回的作品列表
                listWorks="☛推荐" + listOfWorks + "☚";
                //将作品名添加到集合中去
                titleList.add(workName);
                //判断作品是否大于三个
                if (titleList.size()>=3){
                    //循环获取作品列表信息
                    for (int j = 0; j < 3; j++) {
                        //判断是否是最后一个元素
                        if(j==2){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }else{
                    //循环获取作品列表信息
                    for (int j = 0; j < titleList.size(); j++) {
                        //判断是否是最后一个元素
                        if(j==titleList.size()-1){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }

            }
            //设置返回作品列表
            listOfWorks += botAccount + "+" + workName + ",";
            //将作品名添加到集合中去
            titleList.add(workName);
        }
        return null;
    }

    /**
     * 筛选不包含渠道禁用标签的作品
     * @param works 所有作品
     * @param strings 禁用标签
     * @return
     */
    public static List<WorksPojo> filterDisabled(List<WorksPojo> works, List<String> strings1) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //循环遍历所有作品
        for (WorksPojo work : works) {
            //获取作品类型
            List<String> asList1 = work.getLabels();
            //取交集判断是否含有相同的标签
            //取差集
            List<String> asList = new ArrayList<>(asList1);
            if(strings1!=null){
                List<String> strings = new ArrayList<>(strings1);
                asList.retainAll(strings);
                if(asList.size()<=0){
                    worksList.add(work);
                }
            }else{
                worksList.add(work);
            }
        }
        return worksList;
    }

    /**
     * 根据历史玩过的时间排序获取作品列表以及返回信息
     * @param works
     * @return
     */
    public static ReturnedMessages historicalTimeSequence(List<WorksPojo> works) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //最后返回的作品列表
        String listWorks="";
        //最后返回的作品信息
        String returnedMessages="";
        //定义一个String类型的变量用于存储筛选的游戏
        String listOfWorks = "";
        List<String> titleList = new ArrayList<>();
        String workInformation = "";


        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < works.size(); i++) {
            //获取作品名
            String workName = works.get(i).getName();
            //获取作品编号
            String botAccount = works.get(i).getBotAccount();
            //判断是否是最后一个元素
            if(i==works.size()-1){
                //设置返回作品列表
                listOfWorks += botAccount + "+" + workName;
                //设置最后返回的作品列表
                listWorks="☛推荐" + listOfWorks + "☚";
                //将作品名添加到集合中去
                titleList.add(workName);
                //判断作品是否大于三个
                if (titleList.size()>=3){
                    //循环获取作品列表信息
                    for (int j = 0; j < 3; j++) {
                        //判断是否是最后一个元素
                        if(j==2){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }else{
                    //循环获取作品列表信息
                    for (int j = 0; j < titleList.size(); j++) {
                        //判断是否是最后一个元素
                        if(j==titleList.size()-1){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }

            }
            //设置返回作品列表
            listOfWorks += botAccount + "+" + workName + ",";
            //将作品名添加到集合中去
            titleList.add(workName);
        }
        return null;
    }

    /**
     * 根据时间排序获取作品列表以及返回信息
     * @param works
     * @return
     */
    public static ReturnedMessages timeOrder(List<WorksPojo> works) {
        //创建返回信息对象
        ReturnedMessages messages = new ReturnedMessages();
        //最后返回的作品列表
        String listWorks="";
        //最后返回的作品信息
        String returnedMessages="";
        //定义一个String类型的变量用于存储筛选的游戏
        String listOfWorks = "";
        List<String> titleList = new ArrayList<>();
        String workInformation = "";
        //创建一个集合用于存储游戏名，游戏编号
        HashMap<String, String> gameNumber = new HashMap<>();
        //创建一个集合用于存储游戏名，游戏评分
        HashMap<String, String> gameRating = new HashMap<>();
        //循环所有作品，
        for (WorksPojo work : works) {
            //获取作品名
            String gameName = work.getName();
            log.warn("gameName:{}", gameName);
            //获取作品上线时间
            String fraction = work.getGmtApply();
            log.warn("fraction:{}", fraction);
            //获取作品编号
            String botAccount = work.getBotAccount();
            log.warn("botAccount:{}", botAccount);
            //存入游戏编号集合
            gameNumber.put(gameName, botAccount);
            //存入游戏时间集合
            gameRating.put(gameName, fraction);
        }
        //将游戏按照时间降序排序
        List<Map.Entry<String, String>> list = new ArrayList<Map.Entry<String, String>>(gameRating.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        //循环遍历集合，提取游戏名游戏编号
        for (int i = 0; i < list.size(); i++) {
            //获取作品名
            String workName = list.get(i).getKey();
            //获取作品编号
            String botAccount = gameNumber.get(workName);
            //判断是否是最后一个元素
            if(i==list.size()-1){
                //设置返回作品列表
                listOfWorks += botAccount + "+" + workName;
                //设置最后返回的作品列表
                listWorks="☛推荐" + listOfWorks + "☚";
                //将作品名添加到集合中去
                titleList.add(workName);
                //判断作品是否大于三个
                if (titleList.size()>=3){
                    //循环获取作品列表信息
                    for (int j = 0; j < 3; j++) {
                        //判断是否是最后一个元素
                        if(j==2){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }else{
                    //循环获取作品列表信息
                    for (int j = 0; j < titleList.size(); j++) {
                        //判断是否是最后一个元素
                        if(j==titleList.size()-1){
                            workInformation+="《"+titleList.get(j)+"》，";
                            //封装对象后返回
                            messages.setWorksList(listWorks);
                            messages.setWorkInformation(workInformation);
                            messages.setWorksName(titleList);
                            return messages;
                        }
                        //设置返回作品信息
                        workInformation+="《"+titleList.get(j)+"》、";
                    }
                }

            }
            //设置返回作品列表
            listOfWorks += botAccount + "+" + workName + ",";
            //将作品名添加到集合中去
            titleList.add(workName);
        }
        return null;
    }


    /**
     * 获取指定时间范围内的作品
     * @param dataResponses 所有作品
     * @param semantics 时间条件
     * @return
     */
    public static DataResponse latestTime(DataResponse dataResponses, String semantics) {
        List<WorksPojo> worksList = new ArrayList<>();
        //定义一个数组接收作品名
        List<String> interfaceWorks=new ArrayList<>();
        //获取开始时间
        String startingTime;
        //获取结束时间
        String endTime;
        //调用方法解析语义，获取时间
        List<String> date = JudgmentIntention.getDate(semantics);
        if(date.size()>1){
            //获取开始时间
            startingTime = date.get(0);
            log.warn("startingTime:{}",startingTime);
            //获取结束时间
            endTime = date.get(1);
            log.warn("endTime:{}",endTime);
        }else{
            //获取开始时间
            startingTime = date.get(0);
            log.warn("startingTime:{}",startingTime);
            //获取当前时间
            endTime = DateUtil.currentTimes();
            log.warn("endTime:{}",endTime);
        }
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        //循环所有作品
        for (WorksPojo work : works) {
            //获取作品上线时间
            String r = work.getGmtApply();
            //将作品上线时间以及时间区间范围转成时间格式后比较大小
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            try {
                Date parse1 = dateFormat.parse(startingTime);
                Date parse2 = dateFormat.parse(endTime);
                Date parse3 = formatter.parse(r);
                //根基条件判断合适的作品
                if(parse2.getTime()>=parse3.getTime()&& parse3.getTime()>=parse1.getTime()){
                    //满足条件获取作品名
                    interfaceWorks.add(work.getName());
                }
            } catch (ParseException e) {
                log.warn("时间转换失败");
                e.printStackTrace();
            }
        }

        //循环遍历所有作品
        for (WorksPojo work : works) {
            String name = work.getName();
            for (String channel : interfaceWorks) {
                if(name.equals(channel)){
                    worksList.add(work);
                }
            }
        }
        dataResponses.setWorks(worksList);
        return dataResponses;
    }

    /**
     * 用户多个意图，结合渠道警用标签筛选
     * @param works
     * @param strings
     * @param semantics
     * @return
     */
    public static List<WorksPojo> multiConditionScreening(List<WorksPojo> works, List<String> strings1, String semantics) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //解析语义
        String[] split = semantics.split("[+]");
        //转list
        List<String> asList1 = Arrays.asList(split);
        //取差集
        List<String> asList = new ArrayList<>(asList1);
        if(strings1!=null){
            List<String> strings = new ArrayList<>(strings1);
            //取差集
            asList.removeAll(strings);
        }
        if(asList.size()<=0){
            return null;
        }else{
            for (int i = 0; i < asList.size(); i++) {
                for (WorksPojo work : works) {
                    //获取作品类型
                    List<String> labels = work.getLabels();
                    if (labels.contains(asList.get(i))){
                        //包含
                        worksList.add(work);
                    }
                }
            }
            return worksList;
        }
    }

    /**
     * 筛选不包含渠道禁用标签的作品且满足所有意图的作品
     * @param works
     * @param strings
     * @param semantics
     * @return
     */
    public static List<WorksPojo> allIntentions(List<WorksPojo> works, List<String> strings1, String semantics) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //定义一个变量用记录用户意图个数
        int i;
        boolean flag=true;
        //解析语义
        String[] split = semantics.split("[+]");
        //转list
        List<String> asList1 = Arrays.asList(split);
        i=asList1.size();
        List<String> asList = new ArrayList<>(asList1);
        if(strings1!=null){
            List<String> strings = new ArrayList<>(strings1);
            //取差集
            asList.removeAll(strings);
        }
        //判断是否少了意图
        if(asList.size()==i) {
            //循环遍历所有作品
            for (WorksPojo work : works) {
                //获取所有作品类型
                List<String> labels = work.getLabels();
                //判断作品类型是否包含所有意图
                for (String s : asList) {
                    if(!labels.contains(s)){
                        flag=false;
                    }
                }
                //判断作品是否包含所有意图
                if (flag==true){
                    worksList.add(work);
                }
                flag=true;
            }
            return worksList;


        }else{
            return worksList;
        }

    }

    /**
     * 筛选收费或者免费作品
     * @param works
     * @param strings
     * @param semantics
     * @return
     */
    public static List<WorksPojo> collectionAndPaymentScreening(List<WorksPojo> works, List<String> strings, String semantics) {
        //创建一个集合用于存储免费付费作品关键词
        Map map = new HashMap();
        map.put("免费","New");
        map.put("付费","VIP");
        map.put("New","免费");
        map.put("VIP","付费");
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //判断标签是否被禁用
        if(strings.contains(semantics)&&strings.contains(map.get(semantics))){
            return worksList;
        }else if(strings.contains(semantics)){
            for (WorksPojo work : works) {
                //获取作品类型
                List<String> labels = work.getLabels();
                if(labels.contains(map.get(semantics))){
                    worksList.add(work);
                }
            }
            return worksList;
        }else if(strings.contains(map.get(semantics))){
            for (WorksPojo work : works) {
                //获取作品类型
                List<String> labels = work.getLabels();
                if(labels.contains(semantics)){
                    worksList.add(work);
                }
            }
            return worksList;
        }else{
            for (WorksPojo work : works) {
                //获取作品类型
                List<String> labels = work.getLabels();
                if(labels.contains(semantics)||labels.contains(map.get(semantics))){
                    worksList.add(work);
                }
            }
            return worksList;
        }


    }

    /**
     * 获取指定作者的作品
     * @param dataResponse
     * @param semantics
     * @return
     */
    public static List<WorksPojo> authorWorks(DataResponse dataResponse, String semantics) {
        //定义一个集合用于保存筛选后的作品
        List<WorksPojo> worksList=new ArrayList<>();
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();
        //循环所有作品
        for (WorksPojo work : works) {
            //判断作品是否属于指定作者
            if(work.getAuthorName().equals(semantics)){
                worksList.add(work);
            }
        }
        return worksList;
    }


    /**
     * 返回指定作品
     * @param works
     * @param semantics
     * @return
     */
    public static WorksPojo designatedWorks(List<WorksPojo> works, String semantics) {
        //循环所有作品
        for (WorksPojo work : works) {
            //判断作品名是否相同
            if (work.getName().equals(semantics)){
                //返回指定作品
                return work;
            }
        }
        return null;
    }

    /**
     * 判断作品免费还是付费
     * @param strings
     * @return
     */
    public static List<String> chargeJudgment(List<String> labels, List<String> strings) {
        Map map = new HashMap();
        map.put("免费","New");
        map.put("付费","VIP");
        map.put("New","免费");
        map.put("VIP","付费");
        //定义一个集合用于保存筛选后的类型
        List<String> labelsList=new ArrayList<>();
        //作品免费还是付费
        for (String label : labels) {
            if(label.contains("免费")){
                labelsList.add("免费");
            }else if(label.contains("New")){
                labelsList.add("New");
            }else if(label.contains("付费")){
                labelsList.add("付费");
            }else if(label.contains("VIP")){
                labelsList.add("VIP");
            }
        }
        return labelsList;
    }
}
