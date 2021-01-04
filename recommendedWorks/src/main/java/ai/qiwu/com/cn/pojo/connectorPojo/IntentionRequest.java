package ai.qiwu.com.cn.pojo.connectorPojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户意图pojo
 * @author hjd
 */
@Getter
@Setter
public class IntentionRequest {
    /**
     * 手表推荐之推荐意图
     */
    private String intention;
    /**
     * 用户意图的键
     */
    private String chatKey;
    /**
     * 语义
     */
    private String works;

    /**
     * 渠道ID
     */
    private String channelId;
    /**
     * 用户ID
     */
    private String uid;
    /**
     * 手表推荐之历史记录时间段和类型查询1（历史时间段）
     */
    private String historyTypeOne;
    /**
     * 手表推荐之历史记录时间段和类型查询2（类型）
     */
    private String historyTypeTwo;

    public String getIntention() {
        return intention;
    }

    public void setIntention(String intention) {
        this.intention = intention;
    }

    public String getChatKey() {
        return chatKey;
    }

    public void setChatKey(String chatKey) {
        this.chatKey = chatKey;
    }

    public String getWorks() {
        return works;
    }

    public void setWorks(String works) {
        this.works = works;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getHistoryTypeOne() {
        return historyTypeOne;
    }

    public void setHistoryTypeOne(String historyTypeOne) {
        this.historyTypeOne = historyTypeOne;
    }

    public String getHistoryTypeTwo() {
        return historyTypeTwo;
    }

    public void setHistoryTypeTwo(String historyTypeTwo) {
        this.historyTypeTwo = historyTypeTwo;
    }
}
