package ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hjd
 */
@Setter
@Getter
public class Radical {
    /**
     * 返回编号1/0
     */
    private int code;
    /**
     * 成功/失败
     */
    private String msg;
    /**
     * 返回主体
     */
    private Data data;

}
