package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.PayControl;
import ai.qiwu.com.cn.pojo.SeriesPay;
import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.DataResponse;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.ReturnedMessages;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import ai.qiwu.com.cn.service.handleService.WatchService;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 手表推荐未做部分
 *
 * @author hjd
 */
@Service
@Slf4j
public class IntentionTool {

    /**
     * 手表推荐之历史记录类型查询
     * @param intent        用户请求信息
     * @param watchService  数据库类对象
     * @param redisTemplate
     * @return
     */
    public static String historyTypeQuery(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //查询用户历史表中的作品
        List<UserHistory> byUidOfDate = DatabaseUtils.findByUid(uid, watchService);
        //获取作品名，时间集合
        List<Map.Entry<String, Date>> workTime = FilterWorksUtils.workTime(byUidOfDate);
        //获取接口作品和熟路库历史表中作品交集(此时已经按照时间降序排序)
        DataResponse dataResponses = FilterWorksUtils.workResult(maps, workTime);
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        //获取语义
        String semantics = intent.getWorks();
        //判断禁用标签列表是否为空
        if (strings != null) {
            List<String> list2 = new ArrayList<>(strings);
            if (list2.contains(semantics)) {
                String recommendText = "您最近没有体验过" + semantics + "类型的作品呦，试试对我说推荐"+semantics+"类型的作品给我吧";
                String recommendName = "您最近没有体验过" + semantics + "类型的作品呦，试试对我说推荐"+semantics+"类型的作品给我吧";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            } else {
                if (works.size() > 0) {
                    //将作品存到缓存中去
                    ExtractUtils.cacheSave(redisTemplate, works);
                    //跟具作品时间进行排序返回作品列表和信息
                    ReturnedMessages returnedMessages = ExtractUtils.historicalTimeSequence(works);
                    String work = returnedMessages.getWorkInformation();
                    String workInformation = "您已经体验过以上" + semantics + "类型的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                    //封装返回结果信息
                    return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
                } else {
                    String recommendText = "您最近没有体验过" + semantics + "类型的作品呦，试试对我说推荐"+semantics+"类型的作品给我吧";
                    String recommendName = "您最近没有体验过" + semantics + "类型的作品呦，试试对我说推荐"+semantics+"类型的作品给我吧";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                }
            }
        } else {
            if (works.size() > 0) {
                //将作品存到缓存中去
                ExtractUtils.cacheSave(redisTemplate, works);
                //跟具作品时间进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = ExtractUtils.historicalTimeSequence(works);
                String work = returnedMessages.getWorkInformation();
                String workInformation = "您已经体验过以上" + semantics + "类型的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
            } else {
                String recommendText = "您最近没有体验过" + semantics + "类型的作品呦，试试对我说推荐"+semantics+"类型的作品给我吧";
                String recommendName = "您最近没有体验过" + semantics + "类型的作品呦，试试对我说推荐"+semantics+"类型的作品给我吧";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            }
        }
    }

    /**
     * 收表推荐之历史记录时间段查询
     * @param intent        用户请求信息
     * @param watchService  数据库类对象
     * @param redisTemplate
     * @return
     */
    public static String timePeriodQuery(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //查询用户历史表中的作品
        List<UserHistory> byUidOfDate = FilterWorksUtils.findByUidOfDate(uid, watchService, semantics);
        //获取作品名，时间集合
        List<Map.Entry<String, Date>> workTime = FilterWorksUtils.workTime(byUidOfDate);
        //获取接口作品和数据库历史表中作品交集(此时已经按照时间降序排序)
        DataResponse dataResponses = FilterWorksUtils.workResult(maps, workTime);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        if (works.size() > 0) {
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate, works);
            //跟具作品时间进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.historicalTimeSequence(works);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "您" + semantics + "体验过以上作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        } else {
            String recommendText = "您" + semantics + "没有体验过作品呦，试试对我说推荐作品给我吧";
            String recommendName = "您" + semantics + "没有体验过作品呦，试试对我说推荐作品给我吧";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }

    /**
     * 手表推荐之类型推荐+联立查询意图
     * @param intent  用户请求信息
     * @param watchService  数据库类对象
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
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //判断禁用标签是否包含意图
        if (strings != null) {
            List<String> list2 = new ArrayList<>(strings);
            if (list2.contains(semantics)) {
                //包含
                String recommendText = "列表中没有" + semantics + "类型的作品";
                String recommendName = "列表中没有" + semantics + "类型的作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            } else {
                //不包含，根据类型筛选出作品
                List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(works, semantics);
                //判断作品列表是否为空
                if (worksPoJos.size() <= 0) {
                    String recommendText = "列表中没有" + semantics + "类型的作品";
                    String recommendName = "列表中没有" + semantics + "类型的作品";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //将作品存到缓存中去
                    CacheUtils.cacheSave(redisTemplate, worksPoJos);
                    //跟具作品分数进行排序返回作品列表和信息
                    ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
                    String work = returnedMessages.getWorkInformation();
                    String workInformation = "列表中" + semantics + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                    //封装返回结果信息
                    return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
                }
            }
        } else {
            List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(works, semantics);
            //判断作品列表是否为空
            if (worksPoJos.size() <= 0) {
                String recommendText = "列表中没有" + semantics + "类型的作品";
                String recommendName = "列表中没有" + semantics + "类型的作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            } else {
                //将作品存到缓存中去
                CacheUtils.cacheSave(redisTemplate, worksPoJos);
                //跟具作品分数进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation = "列表中" + semantics + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
            }
        }

    }

    /**
     * 手表推荐之某作者的作品推荐+联立查询意图
     *
     * @param intent        用户请求信息
     * @param watchService  数据库类对象
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
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //筛选不包含渠道禁用标签的作品
        //List<WorksPojo> worksPoJos = FilterWorksUtils.filterDisabled(works, strings);
        List<WorksPojo> worksPoJos = FilterWorksUtils.authorWorks(works, semantics);
        //判断作品列表是否为空
        if (worksPoJos.size() <= 0) {
            String recommendText = "暂无" + semantics + "类型的作品";
            String recommendName = "暂无" + semantics + "类型的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        } else {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //跟具作品分数进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "列表中" + semantics + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之最新推荐+联立查询意图
     *
     * @param intent        用户请求信息
     * @param watchService  数据库类对象
     * @param redisTemplate 操作缓存类对象
     * @return
     */
    public static String theLatestJoint(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取缓存中的所有作品
        List<WorksPojo> works = redisTemplate.opsForList().range("worksList", 0, -1);
        //判断作品列表是否为空
        if (works.size() <= 0) {
            String recommendText = "列表中没有作品";
            String recommendName = "列表中没有作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        } else {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, works);
            //跟具作品时间进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = FilterWorksUtils.timeOrder(works);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "列表中最新上线的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        }
    }

    /**
     * 手表推荐之时间段最新推荐
     *
     * @param intent        用户请求信息
     * @param watchService  数据库类对象
     * @param redisTemplate 操作缓存类对象
     * @return
     */
    public static String latestTime(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        //获取指定时间范围的作品
        DataResponse dataResponses = FilterWorksUtils.latestTime(dataResponse, semantics);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        if (works.size() > 0) {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, works);
            //根据时间排序获取作品列表以及返回信息
            ReturnedMessages returnedMessages = FilterWorksUtils.timeOrder(works);
            String work = returnedMessages.getWorkInformation();
            String workInformation = semantics + "新上线的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        } else {
            String recommendText = semantics+"没有上线新的作品";
            String recommendName = semantics+"没有上线新的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }


    /**
     * 手表推荐之多类型或者推荐
     *
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String orType(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //解析语义
        String[] split = semantics.split("[+]");
        //log.warn("semantics:{}",semantics);
        //转list
        List<String> asList = Arrays.asList(split);
        //将map封装成作品对象
        DataResponse dataResponses = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        //获取交集作品
        List<WorksPojo> works1 = dataResponses.getWorks();
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //筛选不包含渠道禁用标签的作品
        List<WorksPojo> worksPoJos = FilterWorksUtils.multiConditionScreening(works1, strings, semantics);
        if (worksPoJos.size() > 0) {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //根据时间排序获取作品列表以及返回信息
            ReturnedMessages returnedMessages = FilterWorksUtils.timeOrder(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = semantics + "新上线的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        } else {
            String recommendText = "暂无" + asList.get(0) + "" + asList.get(1) + "类型的作品，要不试试其他类型吧";
            String recommendName = "暂无" + asList.get(0) + "" + asList.get(1) + "类型的作品，要不试试其他类型吧";
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
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //获取语义
        String semantics = intent.getWorks();
        log.warn("semantics:{}",semantics);
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //解析语义
        String[] split = semantics.split("[+]");
        //转list且去重
        List<String> asList = Arrays.asList(split).stream().distinct().collect(Collectors.toList());
        log.warn("asList:{}",asList);
        //将map封装成作品对象
        DataResponse dataResponses = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        //获取交集作品
        List<WorksPojo> works1 = dataResponses.getWorks();
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //筛选不包含渠道禁用标签的作品且满足所有意图的作品
        List<WorksPojo> worksPoJos = FilterWorksUtils.allIntentions(works1, strings, semantics);
        if (worksPoJos.size() > 0) {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //跟具作品时间进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = FilterWorksUtils.historicalTimeSequence(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "您已经体验过以上" + semantics + "类型的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        } else {
            String recommendText = "暂无" + asList.get(0) + "" + asList.get(1) + "类型的作品，要不试试其他类型吧";
            String recommendName = "暂无" + asList.get(0) + "" + asList.get(1) + "类型的作品，要不试试其他类型吧";
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
        //String uid="119";
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //获取所有满足意图的作品
        List<WorksPojo> worksPoJos = FilterWorksUtils.collectionAndPaymentScreening(works, strings, semantics);
        //判断作品列表是否为空
        if (worksPoJos.size() <= 0) {
            String recommendText = "列表中没有" + semantics + "的作品";
            String recommendName = "列表中没有" + semantics + "的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        } else {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //跟具作品分数进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "列表中" + semantics + "的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
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
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        //获取指定作者的作品
        List<WorksPojo> worksPoJos = FilterWorksUtils.authorWorks(dataResponse.getWorks(), semantics);
        if (worksPoJos.size() > 0) {
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //根据时间排序获取作品列表以及返回信息
            ReturnedMessages returnedMessages = FilterWorksUtils.timeOrder(worksPoJos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = semantics + "新上线的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        } else {
            String recommendText = "暂无" + semantics + "的作品";
            String recommendName = "暂无" + semantics + "的作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }

    /**
     * 手表推荐之某类型最新作品推荐
     *
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String latestType(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //获取语义
        String semantics = intent.getWorks();
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //判断禁用标签是否包含意图
        if (strings != null) {
            List<String> list2 = new ArrayList<>(strings);
            if (list2.contains(semantics)) {
                //包含
                String recommendText = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                String recommendName = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            } else {
                //不包含，根据类型筛选出作品
                List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(works, semantics);
                //将作品存到缓存中去
                CacheUtils.cacheSave(redisTemplate, worksPoJos);
                //判断作品列表是否为空
                if (worksPoJos.size() <= 0) {
                    String recommendText = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                    String recommendName = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //跟具作品分数进行排序返回作品列表和信息
                    ReturnedMessages returnedMessages = FilterWorksUtils.timeOrder(worksPoJos);
                    String work = returnedMessages.getWorkInformation();
                    String workInformation = semantics + "类型的新作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                    //封装返回结果信息
                    return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
                }
            }
        } else {
            //不包含，根据类型筛选出作品
            List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(works, semantics);
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPoJos);
            //判断作品列表是否为空
            if (worksPoJos.size() <= 0) {
                String recommendText = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                String recommendName = "暂无" + semantics + "类型的作品，要不试试其他类型吧";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            } else {
                //跟具作品分数进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = FilterWorksUtils.timeOrder(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation = semantics + "类型的新作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
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
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //获取语义
        String semantics = intent.getWorks();
        //log.warn("semantics:{}",semantics);
        //请求推荐作品接口，返回所有作品
        Map maps = GetWorksUtils.getInterfaceWorks(channelId);
        //将map封装成作品对象
        DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
        //获取所有作品
        List<WorksPojo> works = dataResponse.getWorks();
        //获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //获取指定作品
        WorksPojo worksPojo = FilterWorksUtils.designatedWork(works, semantics);
        //判断是否有该作品
        if (worksPojo == null) {
            String recommendText = "没有这个作品哦";
            String recommendName = "没有这个作品哦";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
        //获取作品类型
        List<String> labels = worksPojo.getLabels();
        //判断作品是否包含收付费信息
        if (labels.contains("免费") || labels.contains("New") || labels.contains("付费") || labels.contains("VIP")) {
            //获取作品是否收费
            List<String> labelsList = FilterWorksUtils.chargeJudgment(labels, strings);
            //判断标签长度
            if (labelsList.size() > 1) {
                if (strings != null) {
                    List<String> list2 = new ArrayList<>(strings);
                    //判断禁用标签是否包含
                    if (list2.contains(labelsList.get(0)) && list2.contains(labelsList.get(1))) {
                        String recommendText = "暂无" + semantics + "的资费信息";
                        String recommendName = "暂无" + semantics + "的资费信息";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    } else {
                        String recommendText = "";
                        String recommendName = semantics + "是" + labelsList.get(0) + "类型的作品";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    }
                } else {
                    //判断禁用标签是否包含
                    String recommendText = "";
                    String recommendName = semantics + "是" + labelsList.get(0) + "类型的作品";
                    return TypeRecommendation.packageResult(recommendName, recommendText);

                }

            } else {
                if (strings != null) {
                    List<String> list3 = new ArrayList<>(strings);
                    //判断禁用标签是否包含
                    if (list3.contains(labelsList.get(0))) {
                        String recommendText = "暂无" + semantics + "的资费信息";
                        String recommendName = "暂无" + semantics + "的资费信息";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    } else {
                        if (labelsList.get(0).equals("免费") || labelsList.get(0).equals("New")) {
                            String recommendText = "";
                            String recommendName = semantics + "是免费类型的作品";
                            return TypeRecommendation.packageResult(recommendName, recommendText);
                        } else {
                            String recommendText = "";
                            String recommendName = semantics + "是收费类型的作品";
                            return TypeRecommendation.packageResult(recommendName, recommendText);
                        }
                    }

                } else {
                    if (labelsList.get(0).equals("免费") || labelsList.get(0).equals("New")) {
                        String recommendText = "";
                        String recommendName = semantics + "是免费类型的作品";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    } else {
                        String recommendText = "";
                        String recommendName = semantics + "是收费类型的作品";
                        return TypeRecommendation.packageResult(recommendName, recommendText);

                    }
                }
            }

            }else{
                String recommendText = "暂无" + semantics + "的资费信息";
                String recommendName = "暂无" + semantics + "的资费信息";
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
        public static String historyType (IntentionRequest intent, WatchService watchService, RedisTemplate
        redisTemplate){
            //获取语义（时间）
            String semantics1 = intent.getHistoryTypeOne();
            //获取语义（类型）
            String semantics2 = intent.getHistoryTypeTwo();
            //获取语义
            String channelId = intent.getChannelId();
            //获取用户id
            String uid = intent.getUid();
            //String uid="119";

            //先查询我上周玩了那些游戏
            //请求推荐作品接口，返回所有作品
            Map maps = GetWorksUtils.getInterfaceWorks(channelId);
            //获取用户历史作品
            List<UserHistory> byUidOfDate = FilterWorksUtils.findByUidOfDate(uid, watchService, semantics1);
            //获取作品名，时间集合
            List<Map.Entry<String, Date>> workTime = ExtractUtils.workTime(byUidOfDate);
            //获取交集(此时已经按照时间降序排序)
            DataResponse dataResponses = FilterWorksUtils.workResult(maps, workTime);
            //获取所有作品
            List<WorksPojo> works = dataResponses.getWorks();
            //更具类型筛选游戏
            //获取禁用标签
            List<String> strings = GetWorksUtils.disableLabel(channelId);
            if (strings != null) {
                List<String> list2 = new ArrayList<>(strings);
                //判断禁用标签是否包含意图
                if (list2.contains(semantics2)) {
                    //包含
                    String recommendText = "您" + semantics1 + "没有体验过" + semantics2 + "类型的作品呦，试试对我说推荐"+semantics2+"类型的作品吧";
                    String recommendName = "您" + semantics1 + "没有体验过" + semantics2 + "类型的作品呦，试试对我说推荐"+semantics2+"类型的作品吧";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //不包含，根据类型筛选出作品
                    List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(works, semantics2);
                    //判断作品列表是否为空
                    if (worksPoJos.size() <= 0) {
                        String recommendText = "您" + semantics1 + "没有体验过" + semantics2 + "类型的作品呦，试试对我说推荐"+semantics2+"类型的作品吧";
                        String recommendName = "您" + semantics1 + "没有体验过" + semantics2 + "类型的作品呦，试试对我说推荐"+semantics2+"类型的作品吧";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    } else {
                        //将作品存到缓存中去
                        CacheUtils.cacheSave(redisTemplate, worksPoJos);
                        //跟具作品时间进行排序返回作品列表和信息
                        ReturnedMessages returnedMessages = FilterWorksUtils.historicalTimeSequence(works);
                        String work = returnedMessages.getWorkInformation();
                        String workInformation = "您" + semantics1 + "体验过以上" + semantics2 + "类型的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                        //封装返回结果信息
                        return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
                    }
                }
            } else {
                //不包含，根据类型筛选出作品
                List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(works, semantics2);
                //判断作品列表是否为空
                if (worksPoJos.size() <= 0) {
                    String recommendText = "您" + semantics1 + "没有体验过" + semantics2 + "类型的作品呦，试试对我说推荐"+semantics2+"类型的作品吧";
                    String recommendName = "您" + semantics1 + "没有体验过" + semantics2 + "类型的作品呦，试试对我说推荐"+semantics2+"类型的作品吧";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //将作品存到缓存中去
                    CacheUtils.cacheSave(redisTemplate, worksPoJos);
                    //跟具作品时间进行排序返回作品列表和信息
                    ReturnedMessages returnedMessages = FilterWorksUtils.historicalTimeSequence(worksPoJos);
                    String work = returnedMessages.getWorkInformation();
                    String workInformation = "您" + semantics1 + "体验过以上" + semantics2 + "类型的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                    //封装返回结果信息
                    return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
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
        public static String authorType (IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate)
        {
            //获取语义（作者）
            String semantics1 = intent.getHistoryTypeOne();
            //获取语义（类型）
            String semantics2 = intent.getHistoryTypeTwo();
            //获取渠道id
            String channelId = intent.getChannelId();
            //获取用户id
            String uid = intent.getUid();
            //String uid="119";
            //根据作者筛选作品
            //请求推荐作品接口，返回所有作品
            Map maps = GetWorksUtils.getInterfaceWorks(channelId);
            DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
            //获取指定作者的作品
            List<WorksPojo> worksPoJo = ExtractUtils.authorWorks(dataResponse, semantics1);

            //根据类型筛选作品
            //获取禁用标签
            List<String> strings = GetWorksUtils.disableLabel(channelId);
            if (strings != null) {
                List<String> list2 = new ArrayList<>(strings);
                //判断禁用标签是否包含意图
                if (list2.contains(semantics2)) {
                    //包含
                    String recommendText = "暂无" + semantics1 + "的" + semantics2 + "类型作品";
                    String recommendName = "暂无" + semantics1 + "的" + semantics2 + "类型作品";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //不包含，根据类型筛选出作品
                    List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(worksPoJo, semantics2);

                    //判断作品列表是否为空
                    if (worksPoJos.size() <= 0) {
                        String recommendText = "暂无" + semantics1 + "的" + semantics2 + "类型作品";
                        String recommendName = "暂无" + semantics1 + "的" + semantics2 + "类型作品";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    } else {
                        //将作品存到缓存中去
                        CacheUtils.cacheSave(redisTemplate, worksPoJos);
                        //跟具作品分数进行排序返回作品列表和信息
                        ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
                        String work = returnedMessages.getWorkInformation();
                        String workInformation = semantics1 + "的" + semantics2 + "类型作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                        //封装返回结果信息
                        return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
                    }
                }
            } else {

                //不包含，根据类型筛选出作品
                List<WorksPojo> worksPoJos = FilterWorksUtils.typeSelection(worksPoJo, semantics2);
                //判断作品列表是否为空
                if (worksPoJos.size() <= 0) {
                    String recommendText = "暂无" + semantics1 + "的" + semantics2 + "类型作品";
                    String recommendName = "暂无" + semantics1 + "的" + semantics2 + "类型作品";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //将作品存到缓存中去
                    CacheUtils.cacheSave(redisTemplate, worksPoJos);
                    //跟具作品分数进行排序返回作品列表和信息
                    ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
                    String work = returnedMessages.getWorkInformation();
                    String workInformation = semantics1 + "的" + semantics2 + "类型作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                    //封装返回结果信息
                    return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
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
        public static String authorSLatestWorks (IntentionRequest intent, WatchService watchService, RedisTemplate
        redisTemplate){
            //获取语义（时间）
            String semantics1 = intent.getHistoryTypeOne();
            //获取语义（作者）
            String semantics2 = intent.getHistoryTypeTwo();
            //获取渠道id
            String channelId = intent.getChannelId();
            //获取用户id
            String uid = intent.getUid();
            //String uid="119";
            //请求推荐作品接口，返回所有作品
            Map maps = GetWorksUtils.getInterfaceWorks(channelId);
            DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
            //获取指定时间范围的作品
            DataResponse dataResponses = FilterWorksUtils.latestTime(dataResponse, semantics1);
            //获取指定作者的作品
            List<WorksPojo> worksPoJos = FilterWorksUtils.authorWorks(dataResponses.getWorks(), semantics2);
            //判断作品列表是否为空
            if (worksPoJos == null||worksPoJos.size()<=0) {
                String recommendText = semantics2 + semantics1 + "没有上线新的作品";
                String recommendName = semantics2 + semantics1 + "没有上线新的作品";
                return TypeRecommendation.packageResult(recommendName, recommendText);
            } else {
                //将作品存到缓存中去
                CacheUtils.cacheSave(redisTemplate, worksPoJos);
                //跟具作品分数进行排序返回作品列表和信息
                ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
                String work = returnedMessages.getWorkInformation();
                String workInformation = semantics2 + semantics1 + "上线新的新作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                //封装返回结果信息
                return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
            }
        }

        /**
         * 手表推荐之某类型时间段最新作品推荐
         * @param intent
         * @param watchService
         * @param redisTemplate
         * @return
         */
        public static String typeLatest (IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
            //获取语义（类型）
            String semantics1 = intent.getHistoryTypeOne();
            //获取语义（时间）
            String semantics2 = intent.getHistoryTypeTwo();
            //获取渠道id
            String channelId = intent.getChannelId();
            //获取用户id
            String uid = intent.getUid();
            //String uid="119";
            DataResponse data = new DataResponse();
            //请求推荐作品接口，返回所有作品
            Map maps = GetWorksUtils.getInterfaceWorks(channelId);
            DataResponse dataResponse = JSONObject.parseObject(JSONObject.toJSONString(maps.get("data")), DataResponse.class);
            //获取所有作品
            List<WorksPojo> works = dataResponse.getWorks();

            //获取禁用标签
            List<String> strings = GetWorksUtils.disableLabel(channelId);
            //判断禁用标签是否包含意图
            if (strings != null) {
                List<String> list2 = new ArrayList<>(strings);
                if (list2.contains(semantics1)) {
                    //包含
                    String recommendText = semantics2 + "没有上线" + semantics1 + "类型的作品";
                    String recommendName = semantics2 + "没有上线" + semantics1 + "类型的作品";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //不包含，根据类型筛选出作品
                    List<WorksPojo> worksPoJo = FilterWorksUtils.typeSelection(works, semantics1);
                    data.setWorks(worksPoJo);
                    data.setLabels(dataResponse.getLabels());
                    //获取指定时间范围的作品
                    DataResponse dataResponses = FilterWorksUtils.latestTime(data, semantics2);
                    //获取所有作品
                    List<WorksPojo> worksPoJos = dataResponses.getWorks();
                    //判断作品列表是否为空
                    if (worksPoJos == null) {
                        String recommendText = semantics2 + "没有上线" + semantics1 + "类型的作品";
                        String recommendName = semantics2 + "没有上线" + semantics1 + "类型的作品";
                        return TypeRecommendation.packageResult(recommendName, recommendText);
                    } else {
                        //将作品存到缓存中去
                        CacheUtils.cacheSave(redisTemplate, worksPoJos);
                        //跟具作品分数进行排序返回作品列表和信息
                        ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
                        String work = returnedMessages.getWorkInformation();
                        String workInformation = semantics2 + "新上线的" + semantics1 + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                        //封装返回结果信息
                        return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
                    }
                }
            } else {
                //不包含，根据类型筛选出作品
                List<WorksPojo> worksPoJo = FilterWorksUtils.typeSelection(works, semantics1);
                data.setWorks(worksPoJo);
                data.setLabels(dataResponse.getLabels());
                //获取指定时间范围的作品
                DataResponse dataResponses = FilterWorksUtils.latestTime(data, semantics2);
                //获取所有作品
                List<WorksPojo> worksPoJos = dataResponses.getWorks();
                //判断作品列表是否为空
                if (worksPoJos.size()<=0) {
                    String recommendText = semantics2 + "没有上线" + semantics1 + "类型的作品";
                    String recommendName = semantics2 + "没有上线" + semantics1 + "类型的作品";
                    return TypeRecommendation.packageResult(recommendName, recommendText);
                } else {
                    //将作品存到缓存中去
                    CacheUtils.cacheSave(redisTemplate, worksPoJos);
                    //跟具作品分数进行排序返回作品列表和信息
                    ReturnedMessages returnedMessages = FilterWorksUtils.scoreScreening(worksPoJos);
                    String work = returnedMessages.getWorkInformation();
                    String workInformation = semantics2 + "新上线的" + semantics1 + "类型的作品有：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
                    //封装返回结果信息
                    return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
                }
            }
        }

    /**
     * 手表推荐之查询已购买的作品
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String purchasedWorks(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //请求数据库获取已购买作品表数据
        List<PayControl> payControls = DatabaseUtils.purchaseWorks(watchService, uid, channelId);
        //查询数据库获取已购买系列作品表数据
        List<SeriesPay> seriesPays = DatabaseUtils.purchasedSeries(watchService, uid, channelId);
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //获取已购买作品和已购买系列作品交集作品(返回并集(作品名，时间))从事已经按照时间排序
        List<Map.Entry<String, Date>> workTime = FilterWorksUtils.purchasedIntersection(payControls, seriesPays);
        //获取接口作品和已购作品交集(此时已经按照时间降序排序)
        DataResponse dataResponses = FilterWorksUtils.workResult(map, workTime);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        if (works.size() > 0) {
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate, works);
            //跟具作品时间进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.historicalTimeSequence(works);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "您已经购买了以上作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        } else {
            String recommendText = "您还没有购买过作品";
            String recommendName = "您还没有购买过作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }

    /**
     * 手表推荐之查询已购买某类型作品
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String purchaseType(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取语义
        String semantics = intent.getWorks();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //从接口中获取禁用标签
        List<String> strings = GetWorksUtils.disableLabel(channelId);
        //请求数据库获取已购买作品表数据
        List<PayControl> payControls = DatabaseUtils.purchaseWorks(watchService, uid, channelId);
        //查询数据库获取已购买系列作品表数据
        List<SeriesPay> seriesPays = DatabaseUtils.purchasedSeries(watchService, uid, channelId);
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //获取已购买作品和已购买系列作品交集作品(返回并集(作品名，时间))从事已经按照时间排序
        List<Map.Entry<String, Date>> workTime = FilterWorksUtils.purchasedIntersection(payControls, seriesPays);
        //获取接口作品和已购作品交集(此时已经按照时间降序排序)
        DataResponse dataResponses = FilterWorksUtils.workResult(map, workTime);
        //筛选不含有禁用标签的作品
        List<WorksPojo> worksPojos = FilterWorksUtils.nonProhibitedWorks(dataResponses.getWorks(), semantics, strings);
        if (worksPojos.size()>0){
            //将作品存到缓存中去
            CacheUtils.cacheSave(redisTemplate, worksPojos);
            //跟具作品时间进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.historicalTimeSequence(worksPojos);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "您已经购买了以上"+semantics+"类型的作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return ResultUtils.packageResult(workInformation, returnedMessages.getWorksList());
        }else{
            String workInformation = "您还没有购买过"+semantics+"类型的作品";
            String listOfWorks = "您还没有购买过"+semantics+"类型的作品";
            return ResultUtils.packageResult(workInformation, listOfWorks);
        }
    }

    /**
     * 手表推荐之查询时间段已购买作品
     * @param intent
     * @param watchService
     * @param redisTemplate
     * @return
     */
    public static String purchasedTimePeriod(IntentionRequest intent, WatchService watchService, RedisTemplate redisTemplate) {
        //获取渠道ID
        String channelId = intent.getChannelId();
        //获取用户id
        String uid = intent.getUid();
        //String uid="119";
        //获取语义
        String semantics = intent.getWorks();
        //请求数据库获取已购买作品表数据
        List<PayControl> payControls = FilterWorksUtils.purchaseTime(watchService, uid, channelId,semantics);
        //查询数据库获取已购买系列作品表数据
        List<SeriesPay> seriesPays = FilterWorksUtils.purchaseSeriesTimePeriod(watchService, uid, channelId,semantics);
        //请求推荐作品接口，返回所有作品
        Map map = GetWorksUtils.getInterfaceWorks(channelId);
        //获取已购买作品和已购买系列作品交集作品(返回并集(作品名，时间))从事已经按照时间排序
        List<Map.Entry<String, Date>> workTime = FilterWorksUtils.purchasedIntersection(payControls, seriesPays);
        //获取接口作品和已购作品交集(此时已经按照时间降序排序)
        DataResponse dataResponses = FilterWorksUtils.workResult(map, workTime);
        //获取所有作品
        List<WorksPojo> works = dataResponses.getWorks();
        if (works.size() > 0) {
            //将作品存到缓存中去
            ExtractUtils.cacheSave(redisTemplate, works);
            //跟具作品时间进行排序返回作品列表和信息
            ReturnedMessages returnedMessages = ExtractUtils.historicalTimeSequence(works);
            String work = returnedMessages.getWorkInformation();
            String workInformation = "您"+semantics+"购买了以上作品：" + work + "你可以说：打开" + returnedMessages.getWorksName().get(0);
            //封装返回结果信息
            return TypeRecommendation.packageResult(workInformation, returnedMessages.getWorksList());
        } else {
            String recommendText = "您"+semantics+"没有购买过作品";
            String recommendName = "您"+semantics+"没有购买过作品";
            return TypeRecommendation.packageResult(recommendName, recommendText);
        }
    }
}

