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
}
