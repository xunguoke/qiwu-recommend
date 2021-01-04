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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public List<String> getGroupVars() {
        return groupVars;
    }

    public void setGroupVars(List<String> groupVars) {
        this.groupVars = groupVars;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public String getChatKey() {
        return chatKey;
    }

    public void setChatKey(String chatKey) {
        this.chatKey = chatKey;
    }

    public String getBotAccount() {
        return botAccount;
    }

    public void setBotAccount(String botAccount) {
        this.botAccount = botAccount;
    }

    public List<Recommend> getVars() {
        return vars;
    }

    public void setVars(List<Recommend> vars) {
        this.vars = vars;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
