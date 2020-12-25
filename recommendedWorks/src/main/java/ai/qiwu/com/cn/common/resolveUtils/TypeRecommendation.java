package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.common.FixedVariable.RwConstant;
import ai.qiwu.com.cn.pojo.UserHistory;
import ai.qiwu.com.cn.pojo.Watch;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo.Data;
import ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo.Radical;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.BotConfig;
import ai.qiwu.com.cn.service.handleService.WatchService;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * 类型推荐
 * @author hjd
 */
@Slf4j
@Service
public class TypeRecommendation {
@Autowired
private WatchService watchService;

    /**
     * 获取类型推荐
     * @param request
     * @return
     */
    public static IntentionRequest getIntent(HttpServletRequest request) {
        //解析请求
        Map map = CommonlyUtils.parsingRequest(request);
        log.warn("map:{}",map);
        //获取指定件所对应的值
        String channelId = (String) map.get("channelId");
        log.warn("渠道ID:{}",channelId);
        String uid = (String) map.get("uid");
        log.warn("用户ID:{}",uid);
        String chatKey = (String) map.get("chatKey");
        Map vars = (Map) map.get("vars");
        String intention = (String) vars.get("手表推荐之推荐意图");
        log.warn("手表推荐之推荐意图:{}",intention);
        String works = (String) vars.get(intention);
        String historyTypeOne = (String) vars.get(intention + "1");
        String historyTypeTwo = (String) vars.get(intention + "2");
        log.warn("手表推荐:{}",works);
        log.warn("手表推荐1:{}",historyTypeOne);
        log.warn("手表推荐2:{}",historyTypeTwo);




        //将请求信息封装在对象中
        IntentionRequest intentionRequest=new IntentionRequest();
        intentionRequest.setWorks(works);
        intentionRequest.setIntention(intention);
        intentionRequest.setChatKey(chatKey);
        intentionRequest.setChannelId(channelId);
        intentionRequest.setUid(uid);
        intentionRequest.setHistoryTypeOne(historyTypeOne);
        intentionRequest.setHistoryTypeTwo(historyTypeTwo);

        return intentionRequest;

    }
    /**
     * 请求接口将接口返回数据转换成map
     * @return
     */
    public static Map getWorks(){
        /**
         * 1.将意图封装在意图对象中
         * 2.发送请求
         * 3.将请求返回只转换成String
         * 4.获取与关键字相匹配的数据返回
         */

        //定义一个map集合用于存储json数据
        Map map = new HashMap<>();
        Gson gson=new Gson();


        //请求路径带上参数
        String url=RwConstant.UrlInterface.QI_WU_RECOMMEND;

        //发送请求
        OkHttpClient client = new OkHttpClient();
        //MediaType.parse()解析出MediaType对象;
        Request request = new Request.Builder()
                .url(url)
                .build();
        //接口返回的消息(推荐作品)
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.warn("推荐作品接口返回数据有误:",e.toString());
            e.printStackTrace();
        }

        String responseJson = null;
        try (ResponseBody body = response.body()){
             responseJson = body.string();
            //log.info("推荐作品接口返回数据,{}", responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将String数据转换成map
        map = gson.fromJson(responseJson, map.getClass());
        //log.warn("接口返回数据转map:{}",map);
        return map;
    }

    /**
     * 封装返回结果
     *
     * @param recommendName 返回推荐的作品
     * @param recommendText 需要返回的文本
     * @return
     */
    public static String packageResult(String recommendName, String recommendText) {

        //封装返回结果
        Radical radical=new Radical();
        radical.setCode(1);
        radical.setMsg("成功");
        Data data=new Data();
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("推荐作品列表",recommendText);
        vars.put("手表推荐返回信息",recommendName);
        data.setVars(vars);
        List<Object> list = new ArrayList<>();
        data.setGroupVars(list);
        radical.setData(data);

        //将对象转换成json
        String s = JSON.toJSONString(radical);
        log.warn("返回信息:{}",s);
        return s;
    }

    /**
     * 封装返回结果
     * @param recommendName 返回推荐的作品
     * @return
     */
    public static String packageResultName(String recommendName) {

        //封装返回结果
        Radical radical=new Radical();
        radical.setCode(1);
        radical.setMsg("成功");
        Data data=new Data();
        HashMap<String, Object> vars = new HashMap<>();
        vars.put("手表推荐返回信息",recommendName);
        data.setVars(vars);
        List<Object> list = new ArrayList<>();
        data.setGroupVars(list);
        radical.setData(data);

        //将对象转换成json
        String s = JSON.toJSONString(radical);
        log.warn("返回信息:{}",s);
        return s;
    }

    /**
     * 根据渠道ID查询数据库中的作品
     * @param intent
     * @param watchService
     * @return
     */
    public static List<Watch> channelJudgment(IntentionRequest intent, WatchService watchService) {
        String channelId = intent.getChannelId();
        //数据库中查询渠道ID
        List<Watch> channelIds = watchService.findByChannelId(channelId);
        log.warn("channelIds:{}",channelIds);
        return channelIds;
    }


    /**
     * 请求bot配置接口将接口返回数据转换成map
     * @return
     */
    public static List<BotConfig> getBotConfig(){
        /**
         * 1.将意图封装在意图对象中
         * 2.发送请求
         * 3.将请求返回只转换成String
         * 4.获取与关键字相匹配的数据返回
         */

        //定义一个map集合用于存储json数据
        Map map = new HashMap<>();
        List<BotConfig> list = new ArrayList<>();
        Gson gson=new Gson();


        //请求路径带上参数
        String url=RwConstant.UrlInterface.QI_WU_BOTCONFIG;

        //发送请求
        OkHttpClient client = new OkHttpClient();
        //MediaType.parse()解析出MediaType对象;
        Request request = new Request.Builder()
                .url(url)
                .build();
        //接口返回的消息(推荐作品)
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.warn("推荐作品接口返回数据有误:",e.toString());
            e.printStackTrace();
        }

        String responseJson = null;
        try (ResponseBody body = response.body()){
            responseJson = body.string();
            //log.info("设备接口返回数据,{}", responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将String数据转换成map

        List<BotConfig> botConfigList = gson.fromJson(responseJson, new TypeToken<List<BotConfig>>(){}.getType());
        //log.warn("botConfigList:{}",botConfigList.size());
        return botConfigList;
    }

    /**
     * 禁用标签
     * @return
     * @param channelId
     */
    public static List<String> disableLabel(String channelId) {
        //定义一个map集合用于存储json数据
        Map map = new HashMap<>();
        List<BotConfig> list = new ArrayList<>();
        Gson gson=new Gson();


        //请求路径带上参数
        String url=RwConstant.UrlInterface.QI_WU_BOTCONFIG;

        //发送请求
        OkHttpClient client = new OkHttpClient();
        //MediaType.parse()解析出MediaType对象;
        Request request = new Request.Builder()
                .url(url)
                .build();
        //接口返回的消息(推荐作品)
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            log.warn("推荐作品接口返回数据有误:",e.toString());
            e.printStackTrace();
        }

        String responseJson = null;
        try (ResponseBody body = response.body()){
            responseJson = body.string();
            //log.info("设备接口返回数据,{}", responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将String数据转换成map

        List<BotConfig> botConfigList = gson.fromJson(responseJson, new TypeToken<List<BotConfig>>(){}.getType());
        //log.warn("botConfigList:{}",botConfigList.size());

        //循环渠道设备
        for (BotConfig config : botConfigList) {
            //获取渠道id
            String recommendBotAccount = config.getRecommendBotAccount();
            //判断渠道id
            if (recommendBotAccount.equals(channelId)){
                //获取禁用标签
                String labelBlacklist = config.getLabelBlacklist();
                //去除空格
                String replace = labelBlacklist.replace(" ", "");
                //根据中文或英文逗号进行分割
                String regex = ",|，";
                String[] blacklist = replace.split(regex);
                List<String> asList = Arrays.asList(blacklist);
                return asList;
            }
        }

        return null;
    }


    /**
     * 查询数据库中指定用户已经玩过的作品
     * @return
     */
    public static List<UserHistory> findByUid(String uid, WatchService watchService) {
        //数据库中查询
        List<UserHistory> userHistory = watchService.findByUid(uid);
        log.warn("userHistory:{}",userHistory);
        return userHistory;
    }

    /**
     * 根据用户id和指定时间段查询玩过的作品
     */
    public static List<UserHistory> findByUidOfDate(String uid, WatchService watchService,String semantics) {
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
        //数据库中查询
        List<UserHistory> userHistory = watchService.findByUidOfDate(uid,startingTime,endTime);
        log.warn("userHistory:{}",userHistory);
        return userHistory;

    }
}
