package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.common.FixedVariable.RwConstant;
import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo.Data;
import ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo.Radical;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类型推荐
 * @author hjd
 */
@Slf4j
public class TypeRecommendation {

    /**
     * 获取类型推荐
     * @param request
     * @return
     */
    public static IntentionRequest getIntent(HttpServletRequest request) {
        //解析请求
        Map map = CommonlyUtils.parsingRequest(request);
        //获取指定件所对应的值
        String chatKey = (String) map.get("chatKey");
        Map vars = (Map) map.get("vars");
        String intention = (String) vars.get("手表推荐之推荐意图");
        log.warn("手表推荐之推荐意图:{}",intention);
        String works = (String) vars.get(intention);
        log.warn("手表推荐:{}",works);

        //将请求信息封装在对象中
        IntentionRequest intentionRequest=new IntentionRequest();
        intentionRequest.setWorks(works);
        intentionRequest.setIntention(intention);
        intentionRequest.setChatKey(chatKey);

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
}
