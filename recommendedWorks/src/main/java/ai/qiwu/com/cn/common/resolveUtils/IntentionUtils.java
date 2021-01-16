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
import org.springframework.ui.ModelMap;

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
    /**
     * 手表推荐之推荐
     * @return
     */
    public static String recommenda(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //System.out.println("请求推荐作品接口，返回所有作品时间："+(endTime-startTime));
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        List<WorksPojo> works = dataResponse.getWorks();
        //判断是否有作品
        if(works.size()<=0){
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
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //筛选不含有禁用标签的作品
        List<WorksPojo> worksPojos = FilterWorksUtils.nonProhibitedWorks(dataResponse.getWorks(), semantics, strings);
        if (worksPojos.size()>0){
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPojos);
            //将作品按照分数排序,返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.scoreSort(worksPojos);
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
        List<String> strings = GetWorksUtils.disableLabel(channelId);
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
        List<WorksPojo> worksPoJos = FilterWorksUtils.authorWorks(dataResponse.getWorks(), semantics);
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
        List<String> strings = GetWorksUtils.disableLabel(channelId);
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
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //获取所有作品
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
                    return ResultUtils.packageResult(recommendName, recommendText);
                }
                String recommendText = "";
                String recommendName = gameName + "的作者是：" + authorName;
                return ResultUtils.packageResult(recommendName, recommendText);
            }
        }
        String recommendText = semantics + "";
        String recommendName = semantics + "没有" + semantics + "这个作品";
        return ResultUtils.packageResult(recommendName, recommendText);
    }

    /**
     * 手表推荐之作品编号查询
     * @return
     */
    public static String workNumber(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //获取所有作品
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
                    return ResultUtils.packageResult(recommendName, recommendText);
                }
                String recommendText = "";
                String recommendName = botAccount;
                return ResultUtils.packageResult(recommendName, recommendText);
            }
        }
        return null;
    }

    /**
     * 手表推荐之人群推荐
     * @return
     */
    public static String crowdRecommendation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //筛选指定人群类型的作品
        List<WorksPojo> worksPojos = FilterWorksUtils.crowdType(dataResponse, semantics);
        //判断是否有作品
        if (worksPojos.size() <= 0) {
            String workInformation = "对不起，暂时没有适合" + semantics + "的作品";
            String listOfWorks = "对不起，暂时没有适合" + semantics + "的作品";
            //将结果信息封装后返回
            return ResultUtils.packageResult(workInformation, listOfWorks);
        } else {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, dataResponse.getWorks());
            //将作品按照分数排序，返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.scoreSort(worksPojos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您推荐以上适合" + semantics + "的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之收藏最多的作品
     *
     * @return
     */
    public static String mostFavorites(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        if(dataResponse.getWorks().size()<=0){
            String workInformation = "暂无作品";
            String listOfWorks = "暂无作品";
            //将结果信息封装后返回
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }else{
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, dataResponse.getWorks());
            //将作品按照收藏人数排序，返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.numberOfCollections(dataResponse.getWorks(),semantics);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您推荐以上收藏最多的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐值作品简介查询
     * @return
     */
    public static String introduction(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //获取works将map转对象
        List<WorksPojo> works = dataResponse.getWorks();
        if (works.size() <= 0) {
            String recommendText = "暂无作品";
            String recommendName = "没有" + semantics + "这个作品哦";
            return ResultUtils.packageResult(recommendName, recommendText);
        }
        //循环所有作品，
        for (WorksPojo work : works) {
            String name = work.getName();
            if (name.equals(semantics)) {
                String intro = work.getIntro();
                if (intro.equals("") || intro == null) {
                    String recommendText = "";
                    String recommendName = "对不起，暂时没有" + name + "的作品简介";
                    return ResultUtils.packageResult(recommendName, recommendText);
                }
                String recommendText = "";
                String recommendName = name + "的作品简介：" + intro;
                return ResultUtils.packageResult(recommendName, recommendText);
            }
        }
        return null;
    }

    /**
     * 手表推荐之系列推荐
     * @return
     */
    public static String seriesRecommendation(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //更具系列筛选作品
        List<WorksPojo> worksPojos = FilterWorksUtils.seriesScreening(dataResponse.getWorks(), semantics);
        if(dataResponse.getWorks().size()<=0){
            String workInformation = "没有" + semantics + "系列的作品，要不试试其他系列吧";
            String listOfWorks ="暂无作品";
            //将结果信息封装后返回
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }else{
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPojos);
            //将作品分数排序，返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.scoreSort(worksPojos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "为您推荐以上作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之类型
     * @param redisTemplate
     * @return
     */
    public static String type(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //从接口中获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //根据uid获取用户已经查寻的类型
        List<String> range = redisTemplate.opsForList().range(uid + "labels", 0, -1);
        //筛选所有类型
        List<String> stringList = FilterWorksUtils.typeSelection(dataResponse.getWorks(), strings);
        if(stringList.size()>0){
            //返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.returnType(redisTemplate,uid,stringList,range);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "作品的类型有：" + work + "你试试对我说：推荐" + returnedMessages.getWorksName().get(0)+"类型的作品给我";
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }else{
            String workInformation = "对不起暂时没有作品类型";
            String listOfWorks ="对不起暂时没有作品类型";
            //将结果信息封装后返回
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }

    /**
     * 手表推荐之系列查询
     * @param redisTemplate
     * @return
     */
    public static String seriesQuery(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //根据uid获取用户已经查寻的类型
        List<String> range = redisTemplate.opsForList().range(uid, 0, -1);
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //筛选所有系列
        List<String> seriesName = FilterWorksUtils.allSeries(dataResponse.getWorks());
        if(seriesName.size()>0){
            //返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.seriesScreening(redisTemplate,uid,seriesName,range);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "作品的类型有：" + work + "你试试对我说：推荐" + returnedMessages.getWorksName().get(0)+"系列的作品给我";
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }else{
            String workInformation = "对不起暂时没有作品类型";
            String listOfWorks ="对不起暂时没有作品类型";
            //将结果信息封装后返回
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }

    /**
     * 手表推荐之作者推荐
     * @return
     */
    public static String recommendedWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(map.get("data")), DataResponse.class);
        //获取所有作品的作者
        List<String> typeList = new ArrayList<>();
        for (WorksPojo work : dataResponse.getWorks()) {
            typeList.add(work.getAuthorName());
        }
        if(typeList.size()<=0){
            String recommendText ="暂无作者";
            String recommendName="暂无作者";
            return ResultUtils.packageResult(recommendName,recommendText);
        }else{
            //将作品按照作者的作品数量排序,返回信息
            ReturnedMessages returnedMessages = WorkExtractionUtils.numberOfAuthorSWorks(typeList);
            String workInformation = returnedMessages.getWorkInformation();
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }
    }
}
