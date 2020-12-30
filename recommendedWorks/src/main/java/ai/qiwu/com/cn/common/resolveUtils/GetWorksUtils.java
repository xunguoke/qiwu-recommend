package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.common.FixedVariable.RwConstant;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 此工具类用于获取作品
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
            log.warn("推荐作品接口返回数据有误:",e.toString());
            e.printStackTrace();
        }

        String responseJson = null;
        try (ResponseBody body = response.body()){
            responseJson = body.string();
            log.info("推荐作品接口返回数据,{}", responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将String数据转换成map
        map = gson.fromJson(responseJson, map.getClass());
        return map;
    }

}
