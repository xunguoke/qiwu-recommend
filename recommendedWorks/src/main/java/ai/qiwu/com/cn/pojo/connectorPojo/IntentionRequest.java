package ai.qiwu.com.cn.pojo.connectorPojo;

import lombok.Getter;
import lombok.Setter;

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
}
