package ai.qiwu.com.cn.pojo.connectorPojo;

import java.util.List;
import java.util.Map;

/**
 * 用户请求封装
 * @author hjd
 */
public class UserRequest {
    /**
     * 用户id
     */
    private String uid;
    /**
     * 组
     */
    private List<String> groupVars;
    /**
     * 用户的话
     */
    private String queryText;
    /**
     * 聊天室键
     */
    private String chatKey;
    /**
     * bot的id
     */
    private String botAccount;
    /**
     * 存储键值对
     * key类型（言情类型）,value说明
     */
    private List<Recommend> vars;
    /**
     * 渠道Id
     */
    private String channelId;

}
