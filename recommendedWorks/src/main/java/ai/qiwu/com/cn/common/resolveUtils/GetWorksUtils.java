package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.common.FixedVariable.RwConstant;
import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.BotConfig;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.*;

/**
 * 此工具类用请求接口获取数据
 * @author hjd
 */
@Slf4j
public class GetWorksUtils {
    /**
     * 获取渠道接口中的所有作品
     * @param channelId 渠道ID
     * @return
     */
    public static Map getInterfaceWorks(String channelId) {

        //定义一个map集合用于存储json数据
        Map map = new HashMap<>();
        Gson gson=new Gson();

        //请求路径带上参数
        String url= RwConstant.UrlInterface.QI_WU_RECOMMEND+channelId;

        //发送请求
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        //接口返回的消息(推荐作品)
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String responseJson = null;
        try (ResponseBody body = response.body()){
            responseJson = body.string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //将String数据转换成map
        map = gson.fromJson(responseJson, map.getClass());
        return map;
    }

    /**
     * 获取接口中的禁用标签
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

}
