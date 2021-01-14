package ai.qiwu.com.cn.common.resolveUtils;

import ai.qiwu.com.cn.pojo.connectorPojo.IntentionRequest;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 该类用于解析请求
 * @author hjd
 */
@Slf4j
public class ResolveUtil {
    public static IntentionRequest parsingRequest(HttpServletRequest request) {
        //解析请求
        Map map = CommonlyUtils.parsingRequest(request);
        //获取指定件所对应的值
        String channelId = (String) map.get("channelId");
        String uid = (String) map.get("uid");
        String chatKey = (String) map.get("chatKey");
        Map vars = (Map) map.get("vars");
        String intention = (String) vars.get("手表推荐之推荐意图");
        log.warn("手表推荐之推荐意图:{}",intention);
        //判断用户语句中是否有加号
        if(intention.contains("+")){
            //截取+号之前的数据
            String intentions =intention.substring(0, intention.indexOf("+"));
            String works = (String) vars.get(intentions);
            String historyTypeOne = (String) vars.get(intentions + "1");
            String historyTypeTwo = (String) vars.get(intentions + "2");

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
        String works = (String) vars.get(intention);
        String historyTypeOne = (String) vars.get(intention + "1");
        String historyTypeTwo = (String) vars.get(intention + "2");
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
}
