package ai.qiwu.com.cn.pojo.connectorPojo.RequestPojo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author hjd
 */

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
