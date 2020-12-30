package ai.qiwu.com.cn.pojo.connectorPojo;

import ai.qiwu.com.cn.pojo.connectorPojo.ResponsePojo.WorksPojo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用于存储临时作品信息
 * @author hjd
 */

@Data
public class WorkInformation {
    /**
     * 作品名
     */
    private String gameName;
    /**
     * 作品编号
     */
    private String botAccount;
    /**
     * 作品分数
     */
    private Double fraction;
    /**
     * 作品同样类型数量
     */
    private Integer size;


    public WorkInformation(String gameName, String botAccount, Double fraction, Integer size) {
        this.gameName = gameName;
        this.botAccount = botAccount;
        this.fraction = fraction;
        this.size = size;
    }
}
